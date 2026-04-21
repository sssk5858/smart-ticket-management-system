package com.smartticket.system.service;

import com.smartticket.system.dto.TicketDtos;
import com.smartticket.system.exception.ApiException;
import com.smartticket.system.model.*;
import com.smartticket.system.repository.CategoryMappingRepository;
import com.smartticket.system.repository.SlaConfigRepository;
import com.smartticket.system.repository.TicketRepository;
import com.smartticket.system.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryMappingRepository categoryMappingRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository, CategoryMappingRepository categoryMappingRepository, SlaConfigRepository slaConfigRepository, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryMappingRepository = categoryMappingRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.auditService = auditService;
    }

    public TicketDtos.TicketResponse createTicket(TicketDtos.CreateTicketRequest request, Authentication auth) {
        User creator = getUserByEmail(auth.getName());
        Priority priority = request.priority() != null ? request.priority() : detectPriority(request.description());
        CategoryMapping mapping = detectCategory(request.description());
        User assignee = mapping == null ? null : userRepository.findByRoleAndIsActiveTrue(Role.ADMIN).stream()
                .findFirst().orElse(null);
        LocalDateTime now = LocalDateTime.now();
        int slaHours = slaConfigRepository.findByPriority(priority).map(SlaConfig::getDurationInHours).orElse(48);

        Ticket ticket = ticketRepository.save(Ticket.builder()
                .title(request.title())
                .description(request.description())
                .status(assignee == null ? TicketStatus.OPEN : TicketStatus.ASSIGNED)
                .priority(priority)
                .category(mapping == null ? "GENERAL" : mapping.getCategory())
                .assignedTeam(mapping == null ? "GENERAL" : mapping.getAssignedTeam())
                .createdBy(creator)
                .assignedTo(assignee)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusHours(slaHours))
                .build());
        auditService.log(ticket.getId(), "TICKET_CREATED", auth.getName());
        return map(ticket);
    }

    public List<TicketDtos.TicketResponse> getMyTickets(Authentication auth) {
        return ticketRepository.findByCreatedBy(getUserByEmail(auth.getName())).stream().map(this::markOverdueAndMap).toList();
    }

    public List<TicketDtos.TicketResponse> getAssignedTickets(Authentication auth) {
        User user = getUserByEmail(auth.getName());
        if (user.getRole() == Role.SUPER_ADMIN) {
            return ticketRepository.findAll().stream()
                    .sorted(Comparator.comparing(Ticket::getCreatedAt).reversed())
                    .map(this::markOverdueAndMap)
                    .toList();
        }
        return ticketRepository.findByAssignedTo(user).stream().map(this::markOverdueAndMap).toList();
    }

    public TicketDtos.TicketResponse getTicketForUser(Long id, Authentication auth) {
        User user = getUserByEmail(auth.getName());
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new ApiException("Ticket not found"));
        if (ticket.getCreatedBy() == null || !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException("Ticket not found");
        }
        return markOverdueAndMap(ticket);
    }

    public TicketDtos.TicketResponse getTicketForAdmin(Long id, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new ApiException("Ticket not found"));
        User user = getUserByEmail(auth.getName());
        if (user.getRole() == Role.SUPER_ADMIN) {
            return markOverdueAndMap(ticket);
        }
        if (user.getRole() == Role.ADMIN) {
            if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId())) {
                return markOverdueAndMap(ticket);
            }
            throw new ApiException("Ticket not found");
        }
        throw new ApiException("Forbidden");
    }

    public List<TicketDtos.AssigneeOption> getAssigneeOptions() {
        return userRepository.findAll().stream()
                .filter(u -> u.isActive() && (u.getRole() == Role.ADMIN || u.getRole() == Role.SUPER_ADMIN))
                .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
                .map(u -> new TicketDtos.AssigneeOption(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    public TicketDtos.TicketResponse assignTicket(Long id, TicketDtos.AssignTicketRequest request, Authentication auth) {
        User actor = getUserByEmail(auth.getName());
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new ApiException("Forbidden");
        }
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new ApiException("Ticket not found"));
        User assignee = userRepository.findById(request.assignedToUserId()).orElseThrow(() -> new ApiException("User not found"));
        if (!assignee.isActive()) {
            throw new ApiException("User is not active");
        }
        if (assignee.getRole() != Role.ADMIN && assignee.getRole() != Role.SUPER_ADMIN) {
            throw new ApiException("Tickets can only be assigned to admin accounts");
        }
        ticket.setAssignedTo(assignee);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        auditService.log(ticket.getId(), "TICKET_ASSIGNED", auth.getName());
        return markOverdueAndMap(ticket);
    }

    public TicketDtos.TicketResponse updateStatus(Long id, TicketDtos.UpdateTicketRequest request, Authentication auth) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new ApiException("Ticket not found"));
        validateTransition(ticket.getStatus(), request.status());
        ticket.setStatus(request.status());
        ticket.setResolutionNotes(request.resolutionNotes());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        auditService.log(ticket.getId(), "STATUS_CHANGED_TO_" + request.status(), auth.getName());
        return map(ticket);
    }

    public List<TicketDtos.TicketResponse> filterTickets(TicketDtos.TicketFilterRequest filter) {
        Specification<Ticket> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.status() != null) predicates.add(cb.equal(root.get("status"), filter.status()));
            if (filter.priority() != null) predicates.add(cb.equal(root.get("priority"), filter.priority()));
            if (filter.assignedTo() != null) predicates.add(cb.equal(root.get("assignedTo").get("id"), filter.assignedTo()));
            if (filter.fromDate() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.fromDate().atStartOfDay()));
            if (filter.toDate() != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.toDate().atTime(23, 59, 59)));
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return ticketRepository.findAll(specification).stream().sorted(Comparator.comparing(Ticket::getCreatedAt).reversed()).map(this::markOverdueAndMap).toList();
    }

    private TicketDtos.TicketResponse markOverdueAndMap(Ticket ticket) {
        if (ticket.getDueDate() != null && LocalDateTime.now().isAfter(ticket.getDueDate()) && ticket.getStatus() != TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.RESOLVED) {
            ticket.setStatus(TicketStatus.OVERDUE);
            ticketRepository.save(ticket);
        }
        return map(ticket);
    }

    private Priority detectPriority(String text) {
        String val = text.toLowerCase(Locale.ROOT);
        if (val.contains("urgent") || val.contains("down")) return Priority.HIGH;
        if (val.contains("slow") || val.contains("delay")) return Priority.MEDIUM;
        return Priority.LOW;
    }

    private CategoryMapping detectCategory(String text) {
        String val = text.toLowerCase(Locale.ROOT);
        return categoryMappingRepository.findAll().stream()
                .filter(m -> val.contains(m.getKeyword().toLowerCase(Locale.ROOT)))
                .findFirst().orElse(null);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ApiException("User not found"));
    }

    private void validateTransition(TicketStatus current, TicketStatus target) {
        if (current == TicketStatus.OPEN && target == TicketStatus.ASSIGNED) return;
        if (current == TicketStatus.ASSIGNED && target == TicketStatus.IN_PROGRESS) return;
        if (current == TicketStatus.IN_PROGRESS && (target == TicketStatus.RESOLVED || target == TicketStatus.OVERDUE)) return;
        if (current == TicketStatus.RESOLVED && target == TicketStatus.CLOSED) return;
        if (current == target) return;
        throw new ApiException("Invalid status transition from " + current + " to " + target);
    }

    private TicketDtos.TicketResponse map(Ticket t) {
        return new TicketDtos.TicketResponse(
                t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(), t.getCategory(), t.getAssignedTeam(),
                t.getCreatedBy() != null ? t.getCreatedBy().getEmail() : null,
                t.getAssignedTo() != null ? t.getAssignedTo().getEmail() : null,
                t.getResolutionNotes(), t.getCreatedAt(), t.getUpdatedAt(), t.getDueDate()
        );
    }
}

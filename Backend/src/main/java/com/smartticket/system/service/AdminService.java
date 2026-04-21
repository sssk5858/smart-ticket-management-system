package com.smartticket.system.service;

import com.smartticket.system.dto.AdminDtos;
import com.smartticket.system.exception.ApiException;
import com.smartticket.system.model.CategoryMapping;
import com.smartticket.system.model.Role;
import com.smartticket.system.model.SlaConfig;
import com.smartticket.system.model.Ticket;
import com.smartticket.system.model.User;
import com.smartticket.system.repository.CategoryMappingRepository;
import com.smartticket.system.repository.SlaConfigRepository;
import com.smartticket.system.repository.TicketRepository;
import com.smartticket.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final CategoryMappingRepository categoryMappingRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            SlaConfigRepository slaConfigRepository,
            CategoryMappingRepository categoryMappingRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.categoryMappingRepository = categoryMappingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminDtos.UserResponse> getUsers() {
        return userRepository.findAll().stream().map(u -> new AdminDtos.UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive())).toList();
    }

    public AdminDtos.UserResponse updateUser(Long id, AdminDtos.UserRoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User not found"));
        user.setRole(request.role());
        user.setActive(request.isActive());
        userRepository.save(user);
        return new AdminDtos.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }

    @Transactional
    public AdminDtos.UserResponse createUser(AdminDtos.UserCreateRequest request) {
        if (userRepository.findByEmail(request.email().trim()).isPresent()) {
            throw new ApiException("Email already exists");
        }
        boolean active = request.isActive() == null || request.isActive();
        User user = userRepository.save(User.builder()
                .name(request.name().trim())
                .email(request.email().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .isActive(active)
                .build());
        return new AdminDtos.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }

    @Transactional
    public AdminDtos.UserResponse updateUserDetails(Long id, AdminDtos.UserUpdateRequest request, String currentUserEmail) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User not found"));
        boolean hasChange = (request.name() != null && !request.name().isBlank())
                || (request.email() != null && !request.email().isBlank())
                || request.role() != null
                || request.isActive() != null
                || (request.password() != null && !request.password().isBlank());
        if (!hasChange) {
            throw new ApiException("No changes provided");
        }
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                userRepository.findByEmail(newEmail).ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new ApiException("Email already exists");
                    }
                });
                user.setEmail(newEmail);
            }
        }
        if (request.role() != null) {
            if (user.getRole() == Role.SUPER_ADMIN && request.role() != Role.SUPER_ADMIN
                    && userRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
                throw new ApiException("Cannot remove the last super admin role");
            }
            user.setRole(request.role());
        }
        if (request.isActive() != null) {
            if (!request.isActive() && user.getEmail().equalsIgnoreCase(currentUserEmail)) {
                throw new ApiException("Cannot deactivate your own account");
            }
            user.setActive(request.isActive());
        }
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new ApiException("Password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        userRepository.save(user);
        return new AdminDtos.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }

    @Transactional
    public void deleteUser(Long id, String currentUserEmail) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("User not found"));
        if (user.getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ApiException("Cannot delete your own account");
        }
        if (user.getRole() == Role.SUPER_ADMIN && userRepository.countByRole(Role.SUPER_ADMIN) <= 1) {
            throw new ApiException("Cannot delete the last super admin");
        }
        Set<Ticket> toUpdate = new HashSet<>();
        toUpdate.addAll(ticketRepository.findByCreatedBy(user));
        toUpdate.addAll(ticketRepository.findByAssignedTo(user));
        for (Ticket ticket : toUpdate) {
            if (ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(user.getId())) {
                ticket.setCreatedBy(null);
            }
            if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId())) {
                ticket.setAssignedTo(null);
            }
        }
        ticketRepository.saveAll(toUpdate);
        userRepository.delete(user);
    }

    public List<SlaConfig> getSlaConfigs() {
        return slaConfigRepository.findAll();
    }

    public SlaConfig upsertSla(AdminDtos.SlaConfigRequest request) {
        SlaConfig config = slaConfigRepository.findByPriority(request.priority()).orElse(SlaConfig.builder().priority(request.priority()).build());
        config.setDurationInHours(request.durationInHours());
        return slaConfigRepository.save(config);
    }

    public List<CategoryMapping> getCategoryMappings() {
        return categoryMappingRepository.findAll();
    }

    public CategoryMapping createCategoryMapping(AdminDtos.CategoryMappingRequest request) {
        return categoryMappingRepository.save(CategoryMapping.builder()
                .keyword(request.keyword())
                .category(request.category())
                .assignedTeam(request.assignedTeam())
                .build());
    }
}

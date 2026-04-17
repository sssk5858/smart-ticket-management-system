package com.smartticket.system.service;

import com.smartticket.system.dto.AdminDtos;
import com.smartticket.system.model.Role;
import com.smartticket.system.model.TicketStatus;
import com.smartticket.system.repository.TicketRepository;
import com.smartticket.system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public AdminDtos.DashboardResponse getDashboard() {
        Map<String, Long> byStatus = new HashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            byStatus.put(status.name(), ticketRepository.countByStatus(status));
        }
        Map<String, Long> byCategory = ticketRepository.findAll().stream().collect(Collectors.groupingBy(t -> t.getCategory() == null ? "GENERAL" : t.getCategory(), Collectors.counting()));
        long total = ticketRepository.count();
        long overdue = ticketRepository.countByDueDateBeforeAndStatusNot(LocalDateTime.now(), TicketStatus.CLOSED);
        long users = userRepository.count();
        long admins = userRepository.countByRole(Role.ADMIN);
        return new AdminDtos.DashboardResponse(total, overdue, byStatus, users, admins, byCategory, overdue);
    }
}

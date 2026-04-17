package com.smartticket.system.dto;

import com.smartticket.system.model.Priority;
import com.smartticket.system.model.Role;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class AdminDtos {
    public record UserResponse(Long id, String name, String email, Role role, boolean isActive) {}
    public record UserRoleUpdateRequest(@NotNull Role role, boolean isActive) {}
    public record SlaConfigRequest(@NotNull Priority priority, @NotNull Integer durationInHours) {}
    public record CategoryMappingRequest(String keyword, String category, String assignedTeam) {}
    public record DashboardResponse(
            long totalTickets,
            long overdueTickets,
            Map<String, Long> ticketsByStatus,
            long totalUsers,
            long totalAdmins,
            Map<String, Long> ticketsByCategory,
            long slaBreachCount
    ) {}
}

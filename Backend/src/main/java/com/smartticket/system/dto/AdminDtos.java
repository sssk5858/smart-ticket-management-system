package com.smartticket.system.dto;

import com.smartticket.system.model.Priority;
import com.smartticket.system.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public class AdminDtos {
    public record UserResponse(Long id, String name, String email, Role role, boolean isActive) {}
    public record UserRoleUpdateRequest(@NotNull Role role, boolean isActive) {}
    public record UserCreateRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            @NotNull Role role,
            Boolean isActive
    ) {}
    /** Partial update: null fields are left unchanged; blank password is ignored. */
    public record UserUpdateRequest(
            String name,
            @Email String email,
            Role role,
            Boolean isActive,
            String password
    ) {}
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

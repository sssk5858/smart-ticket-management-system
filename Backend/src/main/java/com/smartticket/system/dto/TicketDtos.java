package com.smartticket.system.dto;

import com.smartticket.system.model.Priority;
import com.smartticket.system.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TicketDtos {
    public record CreateTicketRequest(
            @NotBlank String title,
            @NotBlank String description
    ) {}

    public record UpdateTicketRequest(
            @NotNull TicketStatus status,
            String resolutionNotes
    ) {}

    public record TicketResponse(
            Long id,
            String title,
            String description,
            TicketStatus status,
            Priority priority,
            String category,
            String assignedTeam,
            String createdBy,
            String assignedTo,
            String resolutionNotes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime dueDate
    ) {}

    public record TicketFilterRequest(
            TicketStatus status,
            Priority priority,
            Long assignedTo,
            LocalDate fromDate,
            LocalDate toDate
    ) {}
}

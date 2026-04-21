package com.smartticket.system.controller;

import com.smartticket.system.dto.TicketDtos;
import com.smartticket.system.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/tickets")
    public TicketDtos.TicketResponse createTicket(@Valid @RequestBody TicketDtos.CreateTicketRequest request, Authentication auth) {
        return ticketService.createTicket(request, auth);
    }

    @GetMapping("/tickets")
    public List<TicketDtos.TicketResponse> getMyTickets(Authentication auth) {
        return ticketService.getMyTickets(auth);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/tickets/{id}")
    public TicketDtos.TicketResponse getTicketForUser(@PathVariable Long id, Authentication auth) {
        return ticketService.getTicketForUser(id, auth);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/tickets")
    public List<TicketDtos.TicketResponse> getAssignedTickets(Authentication auth) {
        return ticketService.getAssignedTickets(auth);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/tickets/{id}")
    public TicketDtos.TicketResponse getTicketForAdmin(@PathVariable Long id, Authentication auth) {
        return ticketService.getTicketForAdmin(id, auth);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/assignees")
    public List<TicketDtos.AssigneeOption> assignees() {
        return ticketService.getAssigneeOptions();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/admin/tickets/{id}")
    public TicketDtos.TicketResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TicketDtos.UpdateTicketRequest request, Authentication auth) {
        return ticketService.updateStatus(id, request, auth);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/admin/tickets/{id}/assign")
    public TicketDtos.TicketResponse assignTicket(@PathVariable Long id, @Valid @RequestBody TicketDtos.AssignTicketRequest request, Authentication auth) {
        return ticketService.assignTicket(id, request, auth);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("/admin/tickets/search")
    public List<TicketDtos.TicketResponse> search(@RequestBody TicketDtos.TicketFilterRequest request) {
        return ticketService.filterTickets(request);
    }
}

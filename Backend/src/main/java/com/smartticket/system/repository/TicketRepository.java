package com.smartticket.system.repository;

import com.smartticket.system.model.Priority;
import com.smartticket.system.model.Ticket;
import com.smartticket.system.model.TicketStatus;
import com.smartticket.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByCreatedBy(User user);
    List<Ticket> findByAssignedTo(User user);
    long countByStatus(TicketStatus status);
    long countByDueDateBeforeAndStatusNot(LocalDateTime now, TicketStatus status);
    long countByPriority(Priority priority);
}

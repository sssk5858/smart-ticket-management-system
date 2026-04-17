package com.smartticket.system.repository;

import com.smartticket.system.model.Priority;
import com.smartticket.system.model.SlaConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    Optional<SlaConfig> findByPriority(Priority priority);
}

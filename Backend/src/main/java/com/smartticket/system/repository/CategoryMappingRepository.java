package com.smartticket.system.repository;

import com.smartticket.system.model.CategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryMappingRepository extends JpaRepository<CategoryMapping, Long> {
    List<CategoryMapping> findByKeywordContainingIgnoreCase(String keyword);
}

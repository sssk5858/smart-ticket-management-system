package com.smartticket.system.service;

import com.smartticket.system.dto.AdminDtos;
import com.smartticket.system.exception.ApiException;
import com.smartticket.system.model.CategoryMapping;
import com.smartticket.system.model.SlaConfig;
import com.smartticket.system.model.User;
import com.smartticket.system.repository.CategoryMappingRepository;
import com.smartticket.system.repository.SlaConfigRepository;
import com.smartticket.system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final CategoryMappingRepository categoryMappingRepository;

    public AdminService(UserRepository userRepository, SlaConfigRepository slaConfigRepository, CategoryMappingRepository categoryMappingRepository) {
        this.userRepository = userRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.categoryMappingRepository = categoryMappingRepository;
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

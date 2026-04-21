package com.smartticket.system.controller;

import com.smartticket.system.dto.AdminDtos;
import com.smartticket.system.model.CategoryMapping;
import com.smartticket.system.model.SlaConfig;
import com.smartticket.system.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {
    private final AdminService adminService;

    public SuperAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AdminDtos.UserResponse> users() {
        return adminService.getUsers();
    }

    @PatchMapping("/users/{id}")
    public AdminDtos.UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody AdminDtos.UserRoleUpdateRequest request) {
        return adminService.updateUser(id, request);
    }

    @PostMapping("/users")
    public AdminDtos.UserResponse createUser(@Valid @RequestBody AdminDtos.UserCreateRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public AdminDtos.UserResponse updateUserDetails(@PathVariable Long id, @RequestBody AdminDtos.UserUpdateRequest request, Authentication authentication) {
        return adminService.updateUserDetails(id, request, authentication.getName());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        adminService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sla")
    public List<SlaConfig> sla() {
        return adminService.getSlaConfigs();
    }

    @PostMapping("/sla")
    public SlaConfig upsertSla(@Valid @RequestBody AdminDtos.SlaConfigRequest request) {
        return adminService.upsertSla(request);
    }

    @GetMapping("/category-mappings")
    public List<CategoryMapping> mappings() {
        return adminService.getCategoryMappings();
    }

    @PostMapping("/category-mappings")
    public CategoryMapping createMapping(@RequestBody AdminDtos.CategoryMappingRequest request) {
        return adminService.createCategoryMapping(request);
    }
}

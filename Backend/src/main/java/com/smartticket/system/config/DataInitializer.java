package com.smartticket.system.config;

import com.smartticket.system.model.*;
import com.smartticket.system.repository.CategoryMappingRepository;
import com.smartticket.system.repository.SlaConfigRepository;
import com.smartticket.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final CategoryMappingRepository categoryMappingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, SlaConfigRepository slaConfigRepository, CategoryMappingRepository categoryMappingRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.slaConfigRepository = slaConfigRepository;
        this.categoryMappingRepository = categoryMappingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder().name("Default User").email("user@smart.com").password(passwordEncoder.encode("password")).role(Role.USER).isActive(true).build());
            userRepository.save(User.builder().name("Backend Admin").email("admin@smart.com").password(passwordEncoder.encode("password")).role(Role.ADMIN).isActive(true).build());
            userRepository.save(User.builder().name("Super Admin").email("super@smart.com").password(passwordEncoder.encode("password")).role(Role.SUPER_ADMIN).isActive(true).build());
        }

        if (slaConfigRepository.count() == 0) {
            slaConfigRepository.save(SlaConfig.builder().priority(Priority.HIGH).durationInHours(4).build());
            slaConfigRepository.save(SlaConfig.builder().priority(Priority.MEDIUM).durationInHours(12).build());
            slaConfigRepository.save(SlaConfig.builder().priority(Priority.LOW).durationInHours(24).build());
        }

        if (categoryMappingRepository.count() == 0) {
            categoryMappingRepository.save(CategoryMapping.builder().keyword("sql").category("DATABASE").assignedTeam("DB Team").build());
            categoryMappingRepository.save(CategoryMapping.builder().keyword("database").category("DATABASE").assignedTeam("DB Team").build());
            categoryMappingRepository.save(CategoryMapping.builder().keyword("ui").category("FRONTEND").assignedTeam("Frontend Team").build());
            categoryMappingRepository.save(CategoryMapping.builder().keyword("button").category("FRONTEND").assignedTeam("Frontend Team").build());
            categoryMappingRepository.save(CategoryMapping.builder().keyword("api").category("BACKEND").assignedTeam("Backend Team").build());
            categoryMappingRepository.save(CategoryMapping.builder().keyword("server").category("BACKEND").assignedTeam("Backend Team").build());
        }
    }
}

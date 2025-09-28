package org.example.cv.configuration;

import org.example.cv.constants.RoleEnum;
import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.repositories.RoleRepository;
import org.example.cv.repositories.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(prefix = "spring", value = "datasource.driver-class-name", havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.save(RoleEntity.builder()
                        .name(RoleEnum.USER.name())
                        .description("User role")
                        .build());

                RoleEntity adminRole = roleRepository.save(RoleEntity.builder()
                        .name(RoleEnum.ADMIN.name())
                        .description("Admin role")
                        .build());

                var roles = new java.util.HashSet<RoleEntity>();
                roles.add(adminRole);
                userRepository.save(UserEntity.builder()
                        .username(ADMIN_USER_NAME)
                                .email("phucchuot37@gmail.com")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build());

                log.info("Created default admin user: {}, password: {}", ADMIN_USER_NAME, ADMIN_PASSWORD);
            }
            log.info("Application initialized");
        };
    }
}

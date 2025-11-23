package org.example.cv.utils.security;

import java.util.HashMap;
import java.util.Map;

import org.example.cv.utils.annotation.OwnableRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RepositoryFactoryConfig {

    private final ApplicationContext context;

    @Bean
    public Map<Class<?>, JpaRepository<?, Long>> repositoryMap() {
        Map<Class<?>, JpaRepository<?, Long>> repositories = new HashMap<>();

        Map<String, Object> repos = context.getBeansWithAnnotation(OwnableRepository.class);

        repos.values().forEach(bean -> {
            Class<?> repoClass = bean.getClass().getInterfaces()[0]; // lấy interface gốc
            OwnableRepository annotation = repoClass.getAnnotation(OwnableRepository.class);
            if (annotation != null && bean instanceof JpaRepository<?, ?> repo) {
                repositories.put(annotation.entity(), (JpaRepository<?, Long>) repo);
            }
        });
        return repositories;
    }
}

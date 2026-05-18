package com.company.ragknowledgebase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * JPA Auditing configuration: provides "created_by" / "updated_by" values.
 * In a real app, replace with Spring Security context user extraction.
 */
@Configuration
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // TODO: replace with SecurityContextHolder.getContext().getAuthentication().getName()
        return () -> Optional.of("system");
    }
}

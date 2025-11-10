package com.task.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@RequiredArgsConstructor
@EnableJpaAuditing
@Configuration
public class JpaAuditingConfig {
    private static final String USER_NAME_HEADER = "Username";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try{
                // 현재 요청 정보 가져오기
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                // 요청이 없는 경우(예: 테스트 등)
                if (attributes == null) {
                    return Optional.of("SYSTEM");
                }

                HttpServletRequest request = attributes.getRequest();
                String userName = request.getHeader(USER_NAME_HEADER);

                if (userName == null || userName.isBlank()) {
                    userName = "NON_USER";
                }

                return Optional.of(userName);
            }catch (Exception e){
                return Optional.of("SYSTEM");
            }
        };
    }
}

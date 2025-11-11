package com.task.config;

import com.task.entity.Member;
import com.task.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
@RequiredArgsConstructor
public class JpaAuditingConfig {
    private static final String MEMBER_ID_HEADER = "Member-Id";

    private final MemberRepository memberRepository;

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
                String memberId  = request.getHeader(MEMBER_ID_HEADER);

                //헤더가 없으면 비회원
                if (memberId  == null || memberId .isBlank()) {
                    return Optional.of("NON_USER");
                }

                if(memberId.equals("SYSTEM")){
                    return Optional.of("SYSTEM");
                }

                // memberId를 Long으로 변환 후 DB 조회
                Long id = Long.parseLong(memberId);
                Member member = memberRepository.findById(id).orElse(null);
                String username;

                if(member == null){
                    username = "NON_USER";
                }else{
                    username = member.getName();
                }

                return Optional.of(username);
            }catch (Exception e){
                return Optional.of("SYSTEM");
            }
        };
    }
}

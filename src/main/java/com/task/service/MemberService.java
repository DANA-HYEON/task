package com.task.service;

import com.task.dto.MemberDto;
import com.task.entity.Member;
import com.task.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long join(MemberDto memberDto){
        Member member = Member.builder()
                .name(memberDto.getName())
                .build();

        memberRepository.save(member);
        return member.getId();
    }
}

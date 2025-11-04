package com.task.service;

import com.task.dto.MemberDto;
import com.task.entity.Member;
import com.task.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //회원등록
    public Long join(MemberDto memberDto){
        Member member = Member.builder()
                        .name(memberDto.getName())
                        .build();

        memberRepository.save(member);
        return member.getId();
    }

    //회원조회
    public MemberDto findMember(Long id){
        Member findMember = memberRepository.findById(id).orElseThrow();
        return new MemberDto(findMember.getId(), findMember.getName());
    }

    //모든회원조회
    public List<MemberDto> findMembers(){
        List<Member> findMembers = memberRepository.findAll();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getId(), m.getName()))
                .collect(Collectors.toList());

        return collect;
    }
}

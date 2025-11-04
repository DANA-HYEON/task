package com.task.controller;

import com.task.dto.MemberDto;
import com.task.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/member")
    public Long joinMember(@RequestBody MemberDto memberDto){
       return memberService.join(memberDto);
    }

    @GetMapping("/member/{id}")
    public MemberDto getMember(@PathVariable Long id){
        return memberService.findMember(id);
    }

    @GetMapping("/members")
    public List<MemberDto> getMembers(){
        return memberService.findMembers();
    }
}

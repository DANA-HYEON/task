package com.task.dto;

import com.task.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String name;

    public MemberDto(Member member){
        this.id = member.getId();
        this.name = member.getName();
    }
}

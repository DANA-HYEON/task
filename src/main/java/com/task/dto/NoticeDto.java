package com.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeDto {

    @NotNull(message = "작성자 id 는 필수값입니다.")
    private Long memberId;
    private String title;
    private String content;
}

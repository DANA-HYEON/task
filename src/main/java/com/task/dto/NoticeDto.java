package com.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeDto {

    private String title;
    private String content;
    private Long memberId;
}

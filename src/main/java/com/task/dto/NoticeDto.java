package com.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NoticeDto {

    private Long memberId;
    private String title;
    private String content;
}

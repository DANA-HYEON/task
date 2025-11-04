package com.task.dto;

import com.task.entity.File;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class NoticeDto {

    private String title;
    private String content;
    private Long memberId;
    private List<File> files;
}

package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class ResNoticeDto {
    private Long noticeId;
    private String title;
    private LocalDateTime createdDate;
    private Long viewCount;

    @QueryProjection
    public ResNoticeDto(Long noticeId, String title, LocalDateTime createdDate, Long viewCount) {
        this.noticeId = noticeId;
        this.title = title;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
    }
}

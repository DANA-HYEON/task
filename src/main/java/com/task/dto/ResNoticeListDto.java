package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 공지사항 전체 조회 응답 DTO
 */
@Getter @Setter
@NoArgsConstructor
public class ResNoticeListDto {
    private Long noticeId;
    private String title;
    private LocalDateTime createdDate;
    private Long viewCount;

    @QueryProjection
    public ResNoticeListDto(Long noticeId, String title, LocalDateTime createdDate, Long viewCount) {
        this.noticeId = noticeId;
        this.title = title;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
    }
}

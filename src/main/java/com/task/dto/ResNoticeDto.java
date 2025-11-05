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
    private String content;
    private String uploadFileName;
    private String modifiedBy;
    private LocalDateTime lastModifiedDate;
    private Long viewCount;

    @QueryProjection
    public ResNoticeDto(Long noticeId, String title, String content, String uploadFileName, String modifiedBy, LocalDateTime lastModifiedDate, Long viewCount) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.uploadFileName = uploadFileName;
        this.modifiedBy = modifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.viewCount = viewCount;
    }
}

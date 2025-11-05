package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResNoticeDetailDto {
    private Long id;
    private String title;
    private String content;
    private List<UploadFileCdnDto> uploadFilePathCdnList;
    private String modifiedBy;
    private LocalDateTime lastModifiedDate;
    private Long viewCount;

    @QueryProjection
    public ResNoticeDetailDto(Long id, String title, String content, String modifiedBy, LocalDateTime lastModifiedDate, Long viewCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.modifiedBy = modifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.viewCount = viewCount;
    }
}

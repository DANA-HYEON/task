package com.task.dto;

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
}

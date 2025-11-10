package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

/**
 * ResNoticeDetailDto-> 포함되는 첨부파일 DTO
 */
@Getter @Setter
public class UploadFileCdnDto {
    private Long id;
    private String filename; //첨부파일 이름

    @QueryProjection
    public UploadFileCdnDto(Long id, String filename) {
        this.id = id;
        this.filename = filename;
    }
}

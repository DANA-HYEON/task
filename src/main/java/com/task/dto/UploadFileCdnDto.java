package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UploadFileCdnDto {
    private Long id;
    private String fileCdn;
    private String filename;

    @QueryProjection
    public UploadFileCdnDto(Long id, String fileCdn, String filename) {
        this.id = id;
        this.fileCdn = fileCdn;
        this.filename = filename;
    }
}

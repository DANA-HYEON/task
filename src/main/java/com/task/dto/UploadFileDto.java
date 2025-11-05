package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadFileDto {
    Long id;
    String uploadFileName;

    @QueryProjection
    public UploadFileDto(Long id, String uploadFileName) {
        this.id = id;
        this.uploadFileName = uploadFileName;
    }
}

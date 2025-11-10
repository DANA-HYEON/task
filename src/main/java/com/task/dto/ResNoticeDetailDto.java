package com.task.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 상세 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ResNoticeDetailDto {
    private Long id; //번호
    private String title; //제목
    private String content; //내용
    private List<UploadFileCdnDto> uploadFilePathCdnList; //첨부파일
    private String createdBy; //작성자
    private LocalDateTime createdDate; //작성일
    private Long viewCount; //조회수

    @QueryProjection
    public ResNoticeDetailDto(Long id, String title, String content, String createdBy, LocalDateTime createdDate, Long viewCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
    }
}

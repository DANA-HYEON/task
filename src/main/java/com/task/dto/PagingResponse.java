package com.task.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PagingResponse<T> {

    private List<T> content;
    private int totalPages; //전체 페이지 수
    private long totalElements; //전체 갯수
    private int currentPage; //현재 페이지

    //편의 메서드
    public static <T> PagingResponse<T> createPagingResponse(Page<T> page){
        return new PagingResponse<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber());
    }
}

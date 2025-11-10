package com.task.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.task.dto.*;
import com.task.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;

    //공지사항 + 회원 fetchJoin 가져오기
    public List<Notice> getNoticeWIthMember(){
       return queryFactory
                .selectFrom(QNotice.notice)
                .join(QNotice.notice.member, QMember.member)
                .fetchJoin()
                .fetch();
    }

    //공지사항 전체 리스트 가져오기
    public Page<ResNoticeDto> getResNoticeDto(Pageable pageRequest){
        long offset = pageRequest.getOffset(); //몇개 건너뛸지
        int pageSize = pageRequest.getPageSize(); //한 페이지에 노출할 데이터 건수

        //[ Sort.Order(property="createdDate", direction=ASC) ]
        boolean isAsc = pageRequest.getSort().stream()
                .findFirst()
                .map(order -> order.isAscending())
                .orElse(false);

        //전체 갯수 조회
        JPAQuery<Long> totalCount = queryFactory.select(QNotice.notice.count()).from(QNotice.notice);

        //페이징 처리
        List<ResNoticeDto> list = queryFactory
                .select(new QResNoticeDto(
                        QNotice.notice.id,
                        QNotice.notice.title,
                        QNotice.notice.createdDate,
                        QNotice.notice.viewCount
                ))
                .from(QNotice.notice)
                .offset(offset)
                .limit(pageSize)
                .orderBy(isAsc ? QNotice.notice.createdDate.asc() : QNotice.notice.createdDate.desc())
                .fetch();

        //스프링 데이터 라이브러리가 제공
        //count쿼리가 생략 간으한 경우 생략해서 처리
        //페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
        //마지막 페이지일 때
        return PageableExecutionUtils.getPage(list, pageRequest, totalCount::fetchOne);
    }

    public ResNoticeDetailDto getResNoticeDetailDto(Long noticeId) {
        //공지사항 가져오기
        ResNoticeDetailDto resNoticeDetailDto = queryFactory
                .select(new QResNoticeDetailDto(
                        QNotice.notice.id,
                        QNotice.notice.title,
                        QNotice.notice.content,
                        QNotice.notice.modifiedBy,
                        QNotice.notice.lastModifiedDate,
                        QNotice.notice.viewCount
                ))
                .from(QNotice.notice)
                .where(QNotice.notice.id.eq(noticeId))
                .fetchOne();

        if(resNoticeDetailDto == null){
            throw new IllegalStateException("공지사항이 존재하지 않습니다.");
        }

        //파일 가져오기
        List<UploadFileCdnDto> uploadFileCdnDtoList = queryFactory
                .select(new QUploadFileCdnDto(
                        QUploadFile.uploadFile.id,
                        QUploadFile.uploadFile.uploadFileName,
                        QUploadFile.uploadFile.storeFileName
                ))
                .from(QUploadFile.uploadFile)
                .where(QUploadFile.uploadFile.notice.id.eq(noticeId))
                .fetch();

        //공지사항 상세에 파일리스트 저장
        if(uploadFileCdnDtoList != null && !uploadFileCdnDtoList.isEmpty()){
            resNoticeDetailDto.setUploadFilePathCdnList(uploadFileCdnDtoList);
        }

        return resNoticeDetailDto;
    }

}

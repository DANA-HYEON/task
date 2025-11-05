package com.task.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.task.dto.*;
import com.task.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public List<ResNoticeDto> getResNoticeDto(){
        QUploadFile fMax = new QUploadFile("fMax");

        Expression<String> getLastFileName = JPAExpressions
                .select(QUploadFile.uploadFile.uploadFileName)
                .from(QUploadFile.uploadFile)
                .where(QUploadFile.uploadFile.notice.eq(QNotice.notice), QUploadFile.uploadFile.createdDate.eq(
                        JPAExpressions
                                .select(fMax.createdDate.max())
                                .from(fMax)
                                .where(fMax.notice.eq(QNotice.notice))
                ));

        //공지사항과 연결된 uploadFiles중 id가 가장 큰 파일의 파일명
        return queryFactory
                .select(new QResNoticeDto(
                        QNotice.notice.id,
                        QNotice.notice.title,
                        QNotice.notice.content,
                        getLastFileName,
                        QNotice.notice.modifiedBy,
                        QNotice.notice.lastModifiedDate,
                        QNotice.notice.viewCount
                ))
                .from(QNotice.notice)
                .orderBy(QNotice.notice.lastModifiedDate.desc())
                .fetch();
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
                .orderBy(QUploadFile.uploadFile.lastModifiedDate.desc())
                .fetch();

        //공지사항 상세에 파일리스트 저장
        if(uploadFileCdnDtoList != null && !uploadFileCdnDtoList.isEmpty()){
            resNoticeDetailDto.setUploadFilePathCdnList(uploadFileCdnDtoList);
        }

        return resNoticeDetailDto;
    }

}

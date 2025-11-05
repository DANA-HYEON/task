package com.task.repository;

import com.task.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

//    @Modifying(clearAutomatically = true, flushAutomatically = true)
//    @Query("delete from UploadFile uf where uf.notice.id = :noticeId")
//    int deleteAllByNoticeId(@Param("noticeId") Long noticeId);

    void deleteAllByNoticeId(Long noticeId);
}

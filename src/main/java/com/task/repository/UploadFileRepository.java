package com.task.repository;

import com.task.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    void deleteAllByNoticeId(Long noticeId);
    List<UploadFile> findAllByNoticeId(Long noticeId);
}

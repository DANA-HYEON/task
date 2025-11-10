package com.task.repository;

import com.task.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    List<UploadFile> findAllByNoticeId(Long noticeId);
}

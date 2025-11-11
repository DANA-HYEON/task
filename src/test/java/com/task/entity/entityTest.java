package com.task.entity;

import com.task.repository.MemberRepository;
import com.task.repository.NoticeRepository;
import com.task.repository.UploadFileRepository;
import com.task.util.UploadFileUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class entityTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    UploadFileRepository uploadFileRepository;

    @Autowired
    UploadFileUtil uploadFileUtil;
    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    public void batchSize() throws Exception {
        //given
        Member member = memberRepository.save(Member.builder().name("꽃분이").build());

        List<UploadFile> files = new ArrayList<>();
        for(int i=0; i<5; i++){
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    i + "test.txt",
                    "text/plain",
                    "테스트 파일 내용입니다.".getBytes()
            );

            UploadFile uploadFile = uploadFileUtil.storeFile(file);
            files.add(uploadFile);
        }

        Notice notice = Notice.builder()
                .title("공지 제목")
                .content("공지 내용")
                .viewCount(0L)
                .member(member)
                .build();

        for (UploadFile file : files) {
            notice.addFile(file);
        }

        noticeRepository.save(notice);
        uploadFileRepository.saveAll(files);

        em.flush();
        em.clear();

        //when
        Notice findNotice = noticeRepository.findById(notice.getId()).orElseThrow();
        List<UploadFile> uploadFiles = findNotice.getUploadFiles();
        for (UploadFile uploadFile : uploadFiles) {
            System.out.println("uploadFile.getUploadFileName() = " + uploadFile.getUploadFileName());
        }
        //then
    }
}

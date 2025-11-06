package com.task.service;

import com.task.entity.Notice;
import com.task.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class noticeServiceTest {

    @Autowired
    NoticeService noticeService;

    @Test
    public void 공지사항리스트() throws Exception {
        //given
        List<Notice> test = noticeService.test();

        //when
        for (Notice notice : test) {
            System.out.println("============notice========= = " + notice);
        }
        

        //then
    }

}

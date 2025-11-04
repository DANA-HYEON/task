package com.task.service;

import com.task.dto.MemberDto;
import com.task.dto.NoticeDto;
import com.task.entity.File;
import com.task.entity.Member;
import com.task.entity.Notice;
import com.task.repository.MemberRepository;
import com.task.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    public Long post(NoticeDto noticeDto){
        Member findMember = memberRepository.findById(noticeDto.getMemberId()).orElseThrow();

        Notice notice = Notice.builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .member(findMember)
                .build();

        noticeRepository.save(notice);
        return notice.getId();
    }

//    public Long post(NoticeDto noticeDto, MultipartFile file){
//        log.info("file {}", file);
//
//        Notice notice = Notice.builder()
//                .title(noticeDto.getTitle())
//                .content(noticeDto.getContent())
//                .member(new Member("이다현"))
////                .files(files)
//                .build();
//
//        noticeRepository.save(notice);
//        return notice.getId();
//    }
}

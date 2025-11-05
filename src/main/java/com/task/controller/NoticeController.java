package com.task.controller;

import com.task.dto.NoticeDto;
import com.task.dto.ResNoticeDetailDto;
import com.task.dto.ResNoticeDto;
import com.task.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    //TODO: ResponseEntity 만들기

    @Value("${file.dir}")
    private String fileDir;

    private final NoticeService noticeService;

    //공지사항 리스트
    @GetMapping
    public List<ResNoticeDto> noticeList(){
        return noticeService.getNoticeList();
    }

    //공지사항 상세
    @GetMapping("/{noticeId}")
    public ResNoticeDetailDto getNoticeDetail(@PathVariable Long noticeId){
        return noticeService.getNoticeDetail(noticeId);
    }

    //공지사항 등록
    @PostMapping
    public Long saveNoticePost(HttpServletRequest request,
                         @RequestPart("notice") NoticeDto noticeDto, //reqNoticeDto따로 만들기
                         @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) throws IOException {

        return noticeService.saveNoticePost(noticeDto, files);
    }

    //공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public Boolean deleteNotice(HttpServletRequest request,
                                @PathVariable Long noticeId){
        //TODO:삭제 요청한 유저와 공지사항 등록한 유저가 같으면 or 권한이 있으면 삭제 로직 추가
        return noticeService.deleteNotice(noticeId);
    }
}

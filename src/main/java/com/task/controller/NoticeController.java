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

    @Value("${file.dir}")
    private String fileDir;

    private final NoticeService noticeService;

    @GetMapping
    public List<ResNoticeDto> noticeList(){
        return noticeService.getNoticeList();
    }

    @GetMapping("/{id}")
    public ResNoticeDetailDto getNoticeDetail(   @PathVariable Long noticeId){
        return noticeService.getNoticeDetail(noticeId);
    }

    @PostMapping
    public Long saveNoticePost(HttpServletRequest request,
                         @RequestPart("notice") NoticeDto noticeDto, //reqNoticeDto따로 만들기
                         @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) throws IOException {

        return noticeService.saveNoticePost(noticeDto, files);
    }
}

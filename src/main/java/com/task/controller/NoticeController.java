package com.task.controller;

import com.task.dto.MemberDto;
import com.task.dto.NoticeDto;
import com.task.service.NoticeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    @Value("${file.dir}")
    private String fileDir;

    private final NoticeService noticeService;

//    @GetMapping
//    public List<Notice> noticeList(){
//        return "notice list";
//    }

//    @PostMapping
//    public String notice(HttpServletRequest request,
//                         @RequestPart("notice") NoticeDto noticeDto,
//                         @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
////        log.info("request={}",request);
////        log.info("MultipartFile={}",file);
//
//        if (!file.isEmpty()) {
//            String fullPath = fileDir + file.getOriginalFilename();
//            log.info("파일 저장 fullPath={}", fullPath);
//            file.transferTo(new File(fullPath));
//        }
//
//        noticeService.post(noticeDto, file);
//        return "update=form";
//    }

    @PostMapping
    public String notice(@RequestBody NoticeDto noticeDto
                         ) throws IOException {
        noticeService.post(noticeDto);
        return "update=form";
    }


}

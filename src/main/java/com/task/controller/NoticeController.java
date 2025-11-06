package com.task.controller;

import com.task.dto.NoticeDto;
import com.task.dto.ResNoticeDetailDto;
import com.task.dto.ResNoticeDto;
import com.task.entity.UploadFile;
import com.task.repository.UploadFileRepository;
import com.task.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

    //공지사항 리스트
    @GetMapping
    //TODO: 페이징 적용
    public ResponseEntity<List<ResNoticeDto>> noticeList(){
        return noticeService.getNoticeList();
    }

    //공지사항 상세
    @GetMapping("/{noticeId}")
    public ResponseEntity<ResNoticeDetailDto> getNoticeDetail(@PathVariable Long noticeId){
        return noticeService.getNoticeDetail(noticeId);
    }

    //공지사항 등록
    @PostMapping
    public ResponseEntity<Long> saveNoticePost(HttpServletRequest request,
                         @RequestPart("notice") NoticeDto noticeDto, //reqNoticeDto따로 만들기
                         @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        //TODO: 등록 이미지, 갯수 용량 제한하기
        return noticeService.saveNoticePost(noticeDto, files);
    }

    //공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public Boolean deleteNotice(HttpServletRequest request,
                                @PathVariable Long noticeId){
        //TODO:삭제 요청한 유저와 공지사항 등록한 유저가 같으면 or 권한이 있으면 삭제 로직 추가
        //TODO: 디스크에 저장된 이미지도 삭제하기
        return noticeService.deleteNotice(noticeId);
    }

    //첨부파일 다운로드
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        return noticeService.downloadFile(fileId);
    }

    //공지사항 수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<ResNoticeDetailDto> updateNotice(HttpServletRequest request,
                                               @PathVariable Long noticeId,
                                               @RequestPart("notice") NoticeDto noticeDto, //reqNoticeDto따로 만들기
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        //TODO: 등록 이미지, 갯수 용량 제한하기
        return noticeService.updateNotice(noticeId, noticeDto, files);
    }
    
    //공지사항 조회수 등록(중복 방지 체크)
}

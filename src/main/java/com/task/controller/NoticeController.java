package com.task.controller;

import com.task.dto.*;
import com.task.service.NoticeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private static final String MEMBER_ID_HEADER = "Member-Id";
    private static final String VIEW_COOKIE_NAME = "noticeView";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24; // 1일
    
    private final NoticeService noticeService;

    //공지사항 리스트
    @GetMapping
    public ResponseEntity<ResponseDto> noticeList(@RequestParam(defaultValue = "desc")String direction,
                                                  @PageableDefault(size = 10) Pageable pageable){
        //공지사항 DTO 가져오기
        Page<ResNoticeListDto> noticeList = noticeService.getNoticeList(direction, pageable);
        
        //페이징 DTO 감싸기
        PagingDto<ResNoticeListDto> pagingDto = PagingDto.createPaging(noticeList);
        
        //최종 응답 DTO 감싸기
        ResponseDto response = new ResponseDto("정상적으로 조회되었습니다.", pagingDto);
        return ResponseEntity.ok()
                .body(response);
    }

    //공지사항 상세
    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseDto> getNoticeDetail(@PathVariable Long noticeId){
        ResNoticeDetailDto noticeDetail = noticeService.getNoticeDetail(noticeId);

        //최종 응답 DTO 감싸기
        ResponseDto response = new ResponseDto("정상적으로 조회되었습니다.", noticeDetail);
        return ResponseEntity.ok()
                .body(response);
    }

    //공지사항 등록
    @PostMapping
    public ResponseEntity<ResponseDto> saveNoticePost(HttpServletRequest request,
                                                      @RequestPart("notice") NoticeDto noticeDto,
                                                      @RequestPart(value = "files", required = false) List<MultipartFile> files){
        String memberIdHeader = request.getHeader(MEMBER_ID_HEADER);

        if(memberIdHeader == null || memberIdHeader.isBlank()){
            ResponseDto response = new ResponseDto("헤더에 Member-Id가 없습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(response);
        }

        Long savedNoticeId = noticeService.saveNoticePost(memberIdHeader, noticeDto, files);
        ResponseDto response = new ResponseDto("정상적으로 저장되었습니다.", savedNoticeId);
        return ResponseEntity.ok()
                .body(response);
    }
    //공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ResponseDto> deleteNotice(HttpServletRequest request,
                                                    @PathVariable Long noticeId){
        String memberIdHeader = request.getHeader(MEMBER_ID_HEADER);

        if(memberIdHeader == null || memberIdHeader.isBlank()){
            ResponseDto response = new ResponseDto("헤더에 Member-Id가 없습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(response);
        }

        Boolean result = noticeService.deleteNotice(memberIdHeader, noticeId);
        ResponseDto response = new ResponseDto("정상적으로 삭제되었습니다.", result);
        return ResponseEntity.ok()
                .body(response);
    }

    //첨부파일 다운로드
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        return noticeService.downloadFile(fileId);
    }

    //공지사항 수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<ResponseDto> updateNotice(
                                               HttpServletRequest request,
                                               @PathVariable Long noticeId,
                                               @RequestPart("notice") NoticeDto noticeDto,
                                               //유지할 기존 첨부파일 ids
                                               @RequestPart(value = "originFileIds", required = false) List<Long> originFileIds,
                                               //새로추가할 파일들
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        String memberIdHeader = request.getHeader(MEMBER_ID_HEADER);

        if(memberIdHeader == null || memberIdHeader.isBlank()){
            ResponseDto response = new ResponseDto("헤더에 Member-Id가 없습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(response);
        }

        Long updatedNoticeId = noticeService.updateNotice(memberIdHeader, noticeId, noticeDto, originFileIds, files);
        ResponseDto responseDto = new ResponseDto("정상적으로 수정되었습니다.", updatedNoticeId);
        return ResponseEntity.ok()
                .body(responseDto);
    }
    
    //공지사항 조회수 등록
    @PostMapping("/{noticeId}/view")
    public ResponseEntity<ResponseDto> updateViewCount(
                                                HttpServletRequest request,
                                                HttpServletResponse response,
                                                @PathVariable Long noticeId){
        Cookie oldCookie = null;

        //쿠키 가져오기
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(VIEW_COOKIE_NAME)){
                    oldCookie = cookie;
                }
            }
        }

        String message; // 응답 메시지
        boolean result = false; // 조회수 증가 여부
        
        if(oldCookie != null){
            //이미 본 공지사항인지 확인
            if(!oldCookie.getValue().contains(noticeId.toString())){
                //조회수 증가
                noticeService.updateViewCount(noticeId);
                result = true;
                message = "조회수가 증가했습니다.";

                oldCookie.setValue(oldCookie.getValue() + "_" + noticeId.toString());
                oldCookie.setPath("/");
                oldCookie.setMaxAge(COOKIE_MAX_AGE);
                oldCookie.setHttpOnly(true);
                response.addCookie(oldCookie);
            }else{
                //이미 조회한 게시글
                message = "이미 조회 처리된 게시글입니다.";
            }
        }else{
            //처음 조회한 게시글
            noticeService.updateViewCount(noticeId);
            result = true;
            message = "조회수가 증가했습니다.";

            Cookie newCookie = new Cookie(VIEW_COOKIE_NAME, noticeId.toString());
            newCookie.setPath("/");
            newCookie.setMaxAge(COOKIE_MAX_AGE);
            newCookie.setHttpOnly(true); //JavaScript에서 접근할 수 없게 막는 설정
            response.addCookie(newCookie);
        }

        ResponseDto responseDto = new ResponseDto(message, result);
        return ResponseEntity.ok()
                .body(responseDto);
    }
}

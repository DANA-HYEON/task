package com.task.service;

import com.task.dto.NoticeDto;
import com.task.dto.ResNoticeDetailDto;
import com.task.dto.ResNoticeListDto;
import com.task.entity.Member;
import com.task.entity.Notice;
import com.task.entity.UploadFile;
import com.task.repository.MemberRepository;
import com.task.repository.NoticeQueryRepository;
import com.task.repository.NoticeRepository;
import com.task.repository.UploadFileRepository;
import com.task.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_FILE_COUNT = 10;

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final UploadFileRepository uploadFileRepository;
    private final UploadFileUtil uploadFileUtil;
    private final NoticeQueryRepository noticeQueryRepository;

    @Transactional(readOnly = true)
    public Page<ResNoticeListDto> getNoticeList(@RequestParam(defaultValue = "desc") String direction,
                                                @PageableDefault(size = 10) Pageable pageable){
        //asc,desc 허용
        Sort.Direction sort = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        //사이즈 제한
        if(pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalStateException("한 페이지 최대 " + MAX_PAGE_SIZE + "건까지만 허용됩니다.");
        }

        //패이징 조건 생성
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort, "createdDate"));
        return noticeQueryRepository.getResNoticeDto(pageRequest);
    }


    public Long saveNoticePost(String memberIdHeader, NoticeDto noticeDto, List<MultipartFile> files) {
        //회원 조회
        long memberId = Long.parseLong(memberIdHeader);
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("존재하지 않는 유저입니다."));

        //첨부파일 갯수 제한
        if(files.size() > MAX_FILE_COUNT){
            throw new IllegalStateException("파일을 " + MAX_FILE_COUNT + "개 이상 저장할 수 없습니다.");
        }

        if(!StringUtils.hasText(noticeDto.getTitle()) || !StringUtils.hasText(noticeDto.getContent())){
            throw new IllegalStateException("제목과 내용을 모두 입력해주세요.");
        }
        
        //notice 객체 생성
        Notice saveNotice = Notice.builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .viewCount(0L)
                .member(findMember)
                .build();

        //notice db 저장
        Notice notice = noticeRepository.save(saveNotice); //부모 저장

        List<UploadFile> uploadImageFiles = List.of();
        try {
            //파일 디스크에 저장
            uploadImageFiles = uploadFileUtil.storeFiles(files);
        }catch (IOException ie){
            log.error("파일 저장 시 오류가 발생했습니다.", ie);
        }

        //연관관계 연결
        for (UploadFile uploadImageFile : uploadImageFiles) {
            notice.addFile(uploadImageFile);
        }

        //db저장
        if(!uploadImageFiles.isEmpty()){
            uploadFileRepository.saveAll(uploadImageFiles);
        }

        return notice.getId();
    }

    @Transactional(readOnly = true)
    public ResNoticeDetailDto getNoticeDetail(Long noticeId) {
        return noticeQueryRepository.getResNoticeDetailDto(noticeId);
    }

    public Boolean deleteNotice(String memberIdHeader, Long noticeId){
        //해당 공지사항이 있는지 확인
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다."));

        if(notice.getMember().getId() != Long.parseLong(memberIdHeader)){
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        List<UploadFile> uploadFiles = notice.getUploadFiles();

        try {
            for (UploadFile uploadFile : uploadFiles) {
                uploadFileRepository.delete(uploadFile); //db삭제
                uploadFileUtil.deleteUploadFile(uploadFile); //디스크 삭제
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }

        //공지사항 삭제
        noticeRepository.deleteById(noticeId);
        return !noticeRepository.existsById(noticeId);
    }

    public ResponseEntity<Resource> downloadFile(Long fileId) {
        //첨부파일 찾기
        UploadFile uploadFile = uploadFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 파일입니다."));

        String storeFileName = uploadFile.getStoreFileName(); //서버 내부에 저장된 실제 파일명
        String uploadFileName = uploadFile.getUploadFileName(); //사용자가 업로드한 원본 파일명

        //디스크 실제 파일경로 생성
        String fullPath = uploadFileUtil.getFullPath(storeFileName);
        try{
            UrlResource resource = new UrlResource("file:" + fullPath);
            //파일명 UTF-8 인코딩(한글/공백 깨짐 방지)
            String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
            //다운로드로 강제 + 파일명 헤더 구성
            String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

            return ResponseEntity.ok()
                    //HTTP Response Body에 오는 컨텐츠의 기질/성향을 알려주는 속성
                    //default는 inline (web에 전달되는 data)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition) // 브라우저에 다운로드하라고 지시하는 헤더
                    .body(resource);
        }catch (MalformedURLException e){
            throw  new IllegalStateException("잘못된 파일 경로입니다.");
        }
    }

    public Long updateNotice(String memberIdHeader, Long noticeId, NoticeDto noticeDto, List<Long> originFileIds, List<MultipartFile> files){
        //수정할 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(()-> new IllegalStateException("존재하지 않는 공지사항입니다."));

        if(notice.getMember().getId() != Long.parseLong(memberIdHeader)){
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
        
        if(!StringUtils.hasText(noticeDto.getTitle()) || !StringUtils.hasText(noticeDto.getContent())){
            throw new IllegalStateException("제목과 내용을 모두 입력해주세요.");
        }

        //공지사항 본문 db 수정
        notice.update(noticeDto.getTitle(), noticeDto.getContent());

        //수정할 공지사항의 첨부파일 리스트 가져오기
        List<UploadFile> findAllUploadFiles = uploadFileRepository.findAllByNoticeId(noticeId);

        //삭제할 대상 리스트
        List<UploadFile> toDelete = new ArrayList<>();
        //기존 첨부파일 리스트를 순회하며 요청값에 존재하지 않으면 삭제할 대상 리스트에 추가
        for (UploadFile findUploadFile : findAllUploadFiles) {
            if (!originFileIds.contains(findUploadFile.getId())) {
                toDelete.add(findUploadFile);
            }
        }

        //삭제 대상 db + 디스크에서 제거
        for (UploadFile deleteFile : toDelete) {
            notice.removeFile(deleteFile); //연관관계 삭제
            try{
                uploadFileUtil.deleteUploadFile(deleteFile); //디스크 삭제
            }catch (IOException e){
                throw new IllegalStateException("파일 삭제 중 오류가 발생했습니다.", e);
            }
            uploadFileRepository.delete(deleteFile); //db 삭제
        }
        
        //첨부파일 갯수 제한
        int remainFiles = files.size() + originFileIds.size();
        if(remainFiles > MAX_FILE_COUNT){
            throw new IllegalStateException("파일을 " + MAX_FILE_COUNT + "개 이상 저장할 수 없습니다.");
        }

        List<UploadFile> uploadImageFiles = List.of();
        try {
            uploadImageFiles = uploadFileUtil.storeFiles(files); //디스크 저장
        }catch (IOException e){
            throw new IllegalStateException("새 파일 저장 중 오류가 발생했습니다.", e);
        }

        //연관관계 연결
        for (UploadFile uploadFile : uploadImageFiles) {
            notice.addFile(uploadFile); 
        }

        //db저장
        if(!uploadImageFiles.isEmpty()){
            uploadFileRepository.saveAll(uploadImageFiles);
        }

        return notice.getId();
    }

    //조회수 증가
    public void updateViewCount(Long noticeId) {
        boolean updated = noticeQueryRepository.updateViewCount(noticeId);
        if (!updated) {
            throw new IllegalStateException("존재하지 않는 공지사항입니다.");
        }
    }
}

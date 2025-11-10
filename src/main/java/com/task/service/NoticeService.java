package com.task.service;

import com.task.dto.NoticeDto;
import com.task.dto.PagingResponse;
import com.task.dto.ResNoticeDetailDto;
import com.task.dto.ResNoticeDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final UploadFileRepository uploadFileRepository;
    private final UploadFileUtil uploadFileUtil;
    private final NoticeQueryRepository noticeQueryRepository;

    //queryDsl
    @Transactional(readOnly = true)
    public Page<ResNoticeDto> getNoticeList(@RequestParam(defaultValue = "desc") String direction,
                                            @PageableDefault(size = 10) Pageable pageable){
        //asc,desc만 허용
        Sort.Direction sort = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        //사이즈 제한
        if(pageable.getPageSize() > 50) {
            throw new IllegalStateException("한 페이지 최대 50건까지만 허용됩니다.");
        }

        //패이징 조건 생성
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sort, "createdDate"));
        return noticeQueryRepository.getResNoticeDto(pageRequest);
    }


    public ResponseEntity<Long> saveNoticePost(NoticeDto noticeDto, List<MultipartFile> files) throws IOException {
        //회원 조회
        Long memberId = noticeDto.getMemberId();
        Member findMember = memberRepository.findById(memberId).orElseThrow();

        //notice 객체 생성
        Notice saveNotice = Notice.builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .viewCount(0L)
                .member(findMember)
                .build();

        //notice db 저장
        Notice notice = noticeRepository.save(saveNotice); //부모 저장
   
        //파일 디스크에 저장
        List<UploadFile> uploadImageFiles = uploadFileUtil.storeFiles(files);

        for (UploadFile uploadImageFile : uploadImageFiles) {
            notice.addFile(uploadImageFile); //연관관계 연결
            //db 저장
            uploadFileRepository.save(uploadImageFile);
        }

            //TODO: bulk insert 교체 필요
//        if (!uploadImageFiles.isEmpty()) {
//            uploadFileRepository.saveAll(uploadImageFiles); //cascade 없으니 자식 직접 저장 필수..왜cascade안쓰지
//        }

        return ResponseEntity.ok()
                .body(notice.getId());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ResNoticeDetailDto> getNoticeDetail(Long noticeId) {
        ResNoticeDetailDto resNoticeDetailDto = noticeQueryRepository.getResNoticeDetailDto(noticeId);

        return ResponseEntity.ok()
                .body(resNoticeDetailDto);
    }

    public Boolean deleteNotice(Long noticeId){
        //해당 공지사항이 있는지 확인
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다."));

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

    public ResponseEntity<Resource> downloadFile(Long fileId) throws MalformedURLException {
        UploadFile uploadFile = uploadFileRepository.findById(fileId).orElse(null);

        if(uploadFile == null){
            throw new IllegalStateException("존재하지 않는 파일입니다.");
        }

        String storeFileName = uploadFile.getStoreFileName();
        String uploadFileName = uploadFile.getUploadFileName();

        UrlResource resource = new UrlResource("file:" + uploadFileUtil.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    public List<Notice> test(){
        return noticeRepository.findAll();
    }

    //TODO: 수정요청자와 작성자가 같은지 체크해야함.
    public void updateNotice(Long noticeId, NoticeDto noticeDto, List<Long> originFileIds, List<MultipartFile> files) throws IOException {
        //수정할 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(()-> new IllegalStateException("존재하지 않는 공지사항입니다."));
        //공지사항 db 수정
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
            uploadFileUtil.deleteUploadFile(deleteFile); //디스크 삭제
            uploadFileRepository.delete(deleteFile); //db 삭제
        }

        //연관관계 연결(추가)
        //새로 추가한 파일 디스크에 저장
        List<UploadFile> uploadImageFiles = uploadFileUtil.storeFiles(files); //디스크 저장
        for (UploadFile uploadFile : uploadImageFiles) {
            notice.addFile(uploadFile); //연관관계 추가
            uploadFileRepository.save(uploadFile); //db 저장
        }
    }

    //조회수 증가
    //TODO:notFoundException 추가하기
    public void updateViewCount(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new IllegalStateException("존재하는 공지사항이 아닙니다."));

        //조회수 증가
        notice.updateViewCount();
    }
}

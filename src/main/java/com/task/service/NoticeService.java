package com.task.service;

import com.task.dto.NoticeDto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<List<ResNoticeDto>> getNoticeList(){
        List<ResNoticeDto> resNoticeDto = noticeQueryRepository.getResNoticeDto();
        return ResponseEntity.ok()
                .body(resNoticeDto);
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
            //TODO: bulk insert 교체 필요
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

    public Boolean deleteNotice(Long noticeId) {
        //해당 공지사항이 있는지 확인
        if(!noticeRepository.existsById(noticeId)){
            throw new IllegalStateException("존재하지 않는 공지사항 입니다.");
        }

        //공지사항과 연결된 첨부파일들 먼저 삭제
        uploadFileRepository.deleteAllByNoticeId(noticeId);
        //공지사항 삭제
        noticeRepository.deleteById(noticeId);
        
        //실제로 삭제되었는지 확인
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

    public ResponseEntity<ResNoticeDetailDto> updateNotice(Long noticeId, NoticeDto noticeDto, List<MultipartFile> files) throws IOException {
        //수정할 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(()-> new IllegalStateException("존재하지 않는 공지사항입니다."));

        //TODO: 수정요청자와 작성자가 같은지 체크해야함.
        //공지사항 db 수정
        notice.update(noticeDto.getTitle(), noticeDto.getContent());

        //첨부파일 디스크에서 삭제
        boolean isExistFiles = files != null && files.stream().anyMatch(Objects::nonNull);
        if(isExistFiles){

        }
        List<UploadFile> uploadFiles = notice.getUploadFiles();
        List<String> fileNamelist = uploadFiles.stream().map(UploadFile::getStoreFileName).toList();
        boolean fileDeleteResult = uploadFileUtil.deleteFiles(fileNamelist);

        //첨부파일 db 수정
        if(fileDeleteResult){
            uploadFileRepository.deleteAllByNoticeId(noticeId);
            for (UploadFile uploadFile : uploadFiles) {
                notice.removeFile(uploadFile);
            }

            //파일 디스크에 저장
            List<UploadFile> uploadImageFiles = uploadFileUtil.storeFiles(files);

            for (UploadFile uploadImageFile : uploadImageFiles) {
                notice.addFile(uploadImageFile); //연관관계 연결
                //db 저장
                //TODO: bulk insert 교체 필요
                uploadFileRepository.save(uploadImageFile);
            }

        }

        ResNoticeDetailDto dto = noticeQueryRepository.getResNoticeDetailDto(noticeId);
        return ResponseEntity.ok(dto);
    }

    @Transactional
    public ResponseEntity<ResNoticeDetailDto> updateNotice(Long noticeId, NoticeDto noticeDto, List<MultipartFile> files) throws IOException {
        // 0) 조회/검증
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다."));

        // 1) 본문 수정
        notice.update(noticeDto.getTitle(), noticeDto.getContent());

        // 2) 새 파일이 실제로 존재할 때만 교체 (files가 비어있거나 전부 empty면 스킵)
        boolean hasNewFiles = files != null && files.stream().anyMatch(f -> f != null && !f.isEmpty());
        if (hasNewFiles) {
            // (a) 구파일 스냅샷(디스크 삭제 용도) - 여기서 "복사본"을 만들어야 CME 안 남
            List<UploadFile> oldFilesSnapshot = new java.util.ArrayList<>(notice.getUploadFiles());
            List<String> oldStoreNames = oldFilesSnapshot.stream()
                    .map(UploadFile::getStoreFileName)
                    .toList();

            // (b) 새 파일 디스크 저장 (중간 실패 시 예외 -> 트랜잭션 롤백)
            List<UploadFile> newUploadFiles = uploadFileUtil.storeFiles(files);

            // (c) DB에서 구첨부 삭제 (연관관계 orphanRemoval 미사용 구조라면 필요)
            uploadFileRepository.deleteAllByNoticeId(noticeId);

            // (d) 컬렉션은 반드시 for-each remove 대신 한 번에 clear()
            notice.getUploadFiles().clear();

            // (e) 신규 첨부 연결 + DB 저장(cascade 미사용 구조이므로 save 필요)
            for (UploadFile uf : newUploadFiles) {
                notice.addFile(uf);           // fk 세팅(uf.setNotice(this))
                uploadFileRepository.save(uf);
            }

            // (f) 커밋 확정 후에만 디스크 구파일 삭제 (롤백 대비)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        uploadFileUtil.deleteFiles(oldStoreNames); // List<String> 지원 메서드
                    } catch (Exception e) {
                        log.error("커밋 후 구파일 삭제 실패: {}", oldStoreNames, e);
                    }
                }
            });
        }

        // 3) 최신 상태 재조회하여 응답
        ResNoticeDetailDto dto = noticeQueryRepository.getResNoticeDetailDto(noticeId);
        return ResponseEntity.ok(dto);
    }
}

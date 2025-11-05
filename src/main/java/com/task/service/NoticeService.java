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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public List<ResNoticeDto> getNoticeList(){
        return noticeQueryRepository.getResNoticeDto();
    }


    public Long saveNoticePost(NoticeDto noticeDto, List<MultipartFile> files) throws IOException {
        //회원 조회
        Long memberId = noticeDto.getMemberId();
        Member findMember = memberRepository.findById(memberId).orElseThrow();

        Notice saveNotice = Notice.builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .viewCount(0L)
                .member(findMember)
                .build();

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
        return notice.getId();
    }

    public ResNoticeDetailDto getNoticeDetail(Long noticeId) {
        return noticeQueryRepository.getResNoticeDetailDto(noticeId);
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
}

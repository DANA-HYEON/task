package com.task.service;

import com.task.dto.NoticeDto;
import com.task.dto.ResNoticeDetailDto;
import com.task.dto.ResNoticeListDto;
import com.task.entity.Member;
import com.task.entity.Notice;
import com.task.entity.UploadFile;
import com.task.repository.MemberRepository;
import com.task.repository.NoticeRepository;
import com.task.repository.UploadFileRepository;
import com.task.util.UploadFileUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
public class NoticeServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    NoticeService noticeService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private UploadFileRepository uploadFileRepository;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    @BeforeEach
    void init() throws Exception {
        // 테스트용 회원
        Member member = Member.builder()
                        .name("테스트용 회원")
                        .build();

        memberRepository.save(member);

        for(int i=0; i<50; i++){
            Notice notice = Notice.builder()
                    .title(i + "번째 공지사항")
                    .content(i + "번째 공지사항 내용")
                    .viewCount(0L)
                    .member(member)
                    .build();

            noticeRepository.save(notice);
            Thread.sleep(10);
        }
    }

    @AfterEach
    void reset() {
        RequestContextHolder.resetRequestAttributes(); // ThreadLocal 누수 방지
    }
    
    @Test
    public void 공지사항_전체조회_ASC() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<ResNoticeListDto> asc = noticeService.getNoticeList("asc", pageRequest);

        //then
        assertThat(asc.getTotalElements()).isEqualTo(50);
        List<ResNoticeListDto> contents = asc.getContent();
        assertThat(contents).hasSize(10);

        assertThat(contents.get(0).getTitle()).isEqualTo("0번째 공지사항");
        assertThat(contents.get(1).getTitle()).isEqualTo("1번째 공지사항");
        assertThat(contents.get(2).getTitle()).isEqualTo("2번째 공지사항");
    }

    @Test
    public void 공지사항_전체조회_DESC() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        Page<ResNoticeListDto> asc = noticeService.getNoticeList("desc", pageRequest);

        //then
        assertThat(asc.getTotalElements()).isEqualTo(50);
        List<ResNoticeListDto> contents = asc.getContent();
        assertThat(contents).hasSize(10);

        assertThat(contents.get(0).getTitle()).isEqualTo("49번째 공지사항");
        assertThat(contents.get(1).getTitle()).isEqualTo("48번째 공지사항");
        assertThat(contents.get(2).getTitle()).isEqualTo("47번째 공지사항");
    }
    
    @Test
    public void 공지사항_상세조회() throws Exception {
        //given
        Member member = Member.builder()
                .name("김은혜")
                .build();
        memberRepository.save(member);

        //저장 전에 가짜 요청 + 헤더 세팅
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Member-Id", String.valueOf(member.getId()));

        //스프링이 현재 실행 중인 요청(HttpServletRequest) 을 ThreadLocal 에 저장해두는 도우미 클래스
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        Notice notice = Notice.builder()
                .title("상세 조회용 공지")
                .content("상세 테스트 내용")
                .viewCount(0L)
                .member(member)
                .build();
        noticeRepository.save(notice);

        Long noticeId = notice.getId();

        //when
        ResNoticeDetailDto noticeDetail = noticeService.getNoticeDetail(noticeId);

        //then
        assertThat(noticeDetail).isNotNull();
        assertThat(noticeDetail.getTitle()).isEqualTo("상세 조회용 공지");
        assertThat(noticeDetail.getContent()).isEqualTo("상세 테스트 내용");
        assertThat(noticeDetail.getCreatedBy()).isEqualTo("김은혜");
    }
    
    @Test
    public void 공지사항_저장() throws Exception {
        //given
        Member member = Member.builder()
                .name("이다현")
                .build();
        
        memberRepository.save(member);
        Long memberId = member.getId();

        NoticeDto noticeDto = new NoticeDto("제목","내용");

        //메모리 상에서 파일처럼 동작하는 가짜 파일 객체
        MockMultipartFile file = new MockMultipartFile(
                "files",// @RequestPart("files")
                "test.txt", // 업로드된 원본 파일명
                "text/plain",// MIME 타입
                "테스트 파일 내용입니다.".getBytes() // 파일 내용 (바이트 배열)
        );

        List<MultipartFile> files = List.of(file);

        //when
        Long savedNoticeId = noticeService.saveNoticePost(String.valueOf(memberId), noticeDto, files);

        //then
        Notice notice = noticeRepository.findById(savedNoticeId).orElseThrow();
        assertThat(notice.getTitle()).isEqualTo("제목");
        assertThat(notice.getContent()).isEqualTo("내용");
        assertThat(notice.getMember().getId()).isEqualTo(memberId);
        assertThat(notice.getUploadFiles().size()).isEqualTo(1);

        //존재하지 않는 유저ID 사용
        Assertions.assertThrows(IllegalStateException.class,
                ()-> noticeService.saveNoticePost("999", noticeDto, files));
    }
    
    @Test
    public void 공지사항_삭제() throws Exception {
        //given
        Member member = Member.builder()
                .name("김은혜")
                .build();
        memberRepository.save(member);
        Long memberId = member.getId();

        //저장 전에 가짜 요청 + 헤더 세팅
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Member-Id", String.valueOf(memberId));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        Notice notice = Notice.builder()
                .title("상세 조회용 공지")
                .content("상세 테스트 내용")
                .viewCount(0L)
                .member(member)
                .build();

        Notice notice2 = Notice.builder()
                .title("상세 조회용 공지2")
                .content("상세 테스트 내용2")
                .viewCount(0L)
                .member(member)
                .build();

        noticeRepository.save(notice);
        noticeRepository.save(notice2);

        Long noticeId = notice.getId();
        Long notice2Id = notice2.getId();

        //when
        Boolean deleted = noticeService.deleteNotice(String.valueOf(memberId), noticeId);

        //then
        assertThat(deleted).isTrue();

        boolean isExist = noticeRepository.existsById(noticeId);
        assertThat(isExist).isFalse();

        Optional<Notice> byId = noticeRepository.findById(noticeId);
        assertThat(byId).isEmpty();

        //삭제 권한이 없습니다.
        Assertions.assertThrows(IllegalStateException.class, () -> noticeService.deleteNotice("999", notice2Id));
    }
    
    @Test
    public void 공지사항_수정() throws Exception {
        //given
        Member member = memberRepository.save(Member.builder().name("꽃분이").build());
        Long memberId = member.getId();

        //공지사항 저장 전에 가짜 요청 + 헤더 세팅
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Member-Id", String.valueOf(memberId));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        //메모리 상에서 파일처럼 동작하는 가짜 파일 객체
        MockMultipartFile oldFile = new MockMultipartFile(
                "files",// @RequestPart("files")
                "test.txt", // 업로드된 원본 파일명
                "text/plain",// MIME 타입
                "테스트 파일 내용입니다.".getBytes() // 파일 내용 (바이트 배열)
        );

        List<MultipartFile> files = List.of(oldFile);
        List<UploadFile> oldUploadFiles = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            UploadFile uploadFile = uploadFileUtil.storeFile(multipartFile);
            oldUploadFiles.add(uploadFile);
        }

        uploadFileRepository.saveAll(oldUploadFiles);

        Notice notice = Notice.builder()
                .title("수정전 공지")
                .content("수정전 내용")
                .viewCount(0L)
                .member(member)
                .build();

        //연관관계 연결
        for (UploadFile uploadFile : oldUploadFiles) {
            notice.addFile(uploadFile);
        }

        noticeRepository.save(notice);
        Long noticeId = notice.getId();

        //when
        NoticeDto noticeDto = new NoticeDto("수정 후 제목", "수정 후 내용");
        List<Long> originFileIds = List.of();
        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new test.txt",
                "text/plain",
                "수정 후테스트 파일 내용입니다.".getBytes()
        );
        Long updatedNoticeId = noticeService.updateNotice(String.valueOf(memberId), noticeId, noticeDto, originFileIds, List.of(newFile));

        em.flush();
        em.clear();

        //then
        Notice updatedNotice = noticeRepository.findById(updatedNoticeId).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo("수정 후 제목");
        assertThat(updatedNotice.getContent()).isEqualTo("수정 후 내용");

        // 새 파일로 변경되었는지 확인
        UploadFile updatedFile = updatedNotice.getUploadFiles().get(0);
        assertThat(updatedFile.getUploadFileName()).isEqualTo("new test.txt");

        // 기존 파일 DB에서 사라졌는지 확인
        assertThat(uploadFileRepository.findById(oldUploadFiles.get(0).getId())).isEmpty();
    }
    
    @Test
    public void 조회수_증가() throws Exception {
        // given
        Member member = memberRepository.save(Member.builder().name("꽃미녀").build());
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .title("제목")
                        .content("내용")
                        .viewCount(0L)
                        .member(member)
                        .build()
        );

        Long id = notice.getId();

        // when
        noticeService.updateViewCount(id);

        em.flush();
        em.clear();

        Notice findNotice = noticeRepository.findById(id).orElseThrow();
        assertThat(findNotice.getViewCount()).isEqualTo(1L);
        //존재하지 않는 공지사항
        Assertions.assertThrows(IllegalStateException.class, () -> noticeService.updateViewCount(999L));
    }
}

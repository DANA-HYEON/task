package com.task.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTICE_ID")
    private Long id;

    private String title;
    private String content;

    @Column(name = "VIEW_COUNT", nullable = false)
    @ColumnDefault("0")
    private Long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToMany(mappedBy = "notice", fetch = FetchType.LAZY)
    @Builder.Default
    List<UploadFile> uploadFiles = new ArrayList<>();

    //연관관계 메서드
    public void addFile(UploadFile uploadFile){
        if(uploadFiles == null) return;

        this.uploadFiles.add(uploadFile);
        uploadFile.setNotice(this);
    }

    //연관관계 메서드
    public void removeFile(UploadFile file){
        if(file == null) return;
        this.uploadFiles.remove(file);
        file.setNotice(null);
    }
}

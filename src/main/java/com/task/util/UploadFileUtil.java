package com.task.util;

import com.task.entity.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UploadFileUtil {

    @Value("${file.dir}")
    private String fileDir;

    /**
     * 저장할 파일의 전체 경로를 반환
     * @return file.dir + filename -> /Users/이다현/workspace/file/a.png
     */
    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    /**
     * 다중 파일 업로드를 처리
     */
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile)); //파일 디스크 저장
            }
        }
        return storeFileResult;
    }

    /**
     * 단일 파일을 실제 디스크에 저장
     */
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        
        //디스크 저장
        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName);
    }

    //첨부파일 삭제
    public void deleteUploadFile(UploadFile uploadFile) throws IOException {
        String fullPath = getFullPath(uploadFile.getStoreFileName());
        Path filePath = Paths.get(fullPath);

        //디스크 파일 삭제
        Files.deleteIfExists(filePath);
    }

    /**
     * 파일 이름 중복을 방지 위해, 랜덤 UUID + 원래 확장자 형식의 새 이름 생성
     */
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 파일의 확장자만 추출
     * "cat.png" → "png"
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}

package com.green.Schedule.result.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// 결과보고 처리사진 업로드 전용 유틸 (오늘 배운 방식대로 컨트롤러에서 분리)
@Component
public class UploadUtil {

  // application.yaml에 선언된 file.upload.dir 값을 uploadPath에 주입
  @Value("${file.upload.dir}")
  private String uploadPath;

  // 사진 1장을 uploadPath 폴더에 저장하고, 화면에서 쓸 경로("/uploads/result/파일명")를 돌려줌
  // 첨부한 파일이 없으면 null을 돌려줌
  public String fileUpload(MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return null;
    }

    // 전달받은 파일의 원본 파일명
    String originFileName = imageFile.getOriginalFilename();

    // 같은 이름 파일끼리 안 겹치게, UUID로 새 파일명을 만듦
    String uuid = UUID.randomUUID().toString();

    // 확장자 추출 (예: "사진.jpg" -> ".jpg")
    String extension = "";
    if (originFileName != null && originFileName.contains(".")) {
      int dotIndex = originFileName.lastIndexOf(".");
      extension = originFileName.substring(dotIndex);
    }

    // 업로드 폴더가 없으면 새로 만들어줌
    // 절대경로로 안 만들면, 업로드 파일이 (우리가 만든 폴더가 아니라)
    // 톰캣이 멀티파트 파일을 임시로 다루는 폴더 기준으로 저장되려다 실패함
    File uploadFolder = new File(uploadPath).getAbsoluteFile();
    if (!uploadFolder.exists()) {
      uploadFolder.mkdirs();
    }

    // 업로드 파일명 지정 (예: 3f2a5b90-....jpg)
    String attachedFileName = uuid + extension;

    // 정해진 경로에 파일을 생성(껍데기 파일 생성)
    File file = new File(uploadFolder, attachedFileName);
    try {
      // 업로드하기 위해 가져온 실제 이미지 파일을 위에서 만든 껍데기 파일로 변환
      imageFile.transferTo(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // DB에는 화면에서 <img src="..."> 로 바로 쓸 수 있는 경로만 저장
    return "/uploads/result/" + attachedFileName;
  }

}

package com.green.Schedule.engineer.mapper;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EngineerMapper {

  //기사 목록 조회
  List<EngineerDTO> engineerList();

  //기사 상세 조회 (수정 폼 진입 시)
  EngineerDTO engineerDetail(int engineerNo);

  //MEMBER 등록 (기사 계정)
  void insertMember(EngineerDTO engineerDTO);

  //ENGINEER 등록 (상세 정보)
  void insertEngineer(EngineerDTO engineerDTO);

  //기사 정보 수정
  void updateEngineer(EngineerDTO engineerDTO);

  //기사 삭제
  void deleteEngineer(int engineerNo);
}

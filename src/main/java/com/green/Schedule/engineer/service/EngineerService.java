package com.green.Schedule.engineer.service;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.engineer.mapper.EngineerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineerService  {
  private final EngineerMapper engineerMapper;

  //기사 목록 조회
  public List<EngineerDTO> engineerList(){
    return engineerMapper.engineerList();
  }

  //기사 상세 조회
  public EngineerDTO engineerDetail(int engineerNo){
    return engineerMapper.engineerDetail(engineerNo);
  }

  //기사 등록 (MEMBER + ENGINEER 두 테이블 동시 저장)
  public void insertEngineer(EngineerDTO engineerDTO){
    //1. MEMBER 먼저 저장 -> memNo가 DTO에 자동으로 채워짐
    engineerMapper.insertMember(engineerDTO);
    //2. 채워진 memNo로 ENGINEER 저장
    engineerMapper.insertEngineer(engineerDTO);
  }

  //기사 정보 수정
  public void updateEngineer(EngineerDTO engineerDTO){
    engineerMapper.updateEngineer(engineerDTO);
  }

  //기사 삭제
  public void deleteEngineer(int engineerNo){
    engineerMapper.deleteEngineer(engineerNo);
  }
}

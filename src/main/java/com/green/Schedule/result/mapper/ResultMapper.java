package com.green.Schedule.result.mapper;

import com.green.Schedule.result.dto.ResultDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ResultMapper {
//오늘 일정 조회


//달력조회


  //기사 결과보고
  void insertResult(ResultDTO resultDTO);

}

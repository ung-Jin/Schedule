package com.green.Schedule.result.service;

import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResultService {
  private final ResultMapper resultMapper;

  public void insertResult(ResultDTO resultDTO){
    resultMapper.insertResult(resultDTO);
  }

}

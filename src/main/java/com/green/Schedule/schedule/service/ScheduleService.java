package com.green.Schedule.schedule.service;

import com.green.Schedule.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
  private final ScheduleMapper scheduleMapper;


}

package com.green.Schedule.dashboard.DTO;

import lombok.Data;

//대시보드 상단 카드용 상태별 건수 DTO
@Data
public class DashboardStatDTO {
  private int receiptCount;     //접수중 (RECEIVED)
  private int assignedCount;    //배정완료 (ASSIGNED)
  private int progressCount;    //진행중 (IN_PROGRESS)
  private int completeCount;    //완료 (COMPLETED)
}

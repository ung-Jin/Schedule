package com.green.Schedule.satisfaction.dto;

import lombok.Data;

/**
 * 고객이 만족도 조사 페이지(/survey)에서 제출하는 값을 담는 DTO입니다.
 * 항목 3개(방문/시간준수, 친절도/응대, A/S 처리결과) 각각 1~5점을 따로 받습니다.
 * 대시보드에 보여주는 "만족도 점수"는 이 세 값의 평균입니다 (SatisfactionMapper의 AVG 쿼리 참고).
 *
 * scheduleNo     -> SATISFACTION.SCHEDULE_NO (어떤 방문 건에 대한 평가인지, 문자 링크의 scheduleNo)
 * engineerNo     -> SATISFACTION.ENGINEER_NO (어떤 기사에 대한 평가인지, 문자 링크의 engineerNo)
 * scoreVisit     -> SATISFACTION.SCORE_VISIT     (기사 방문 및 시간 준수 만족도, 1~5)
 * scoreKindness  -> SATISFACTION.SCORE_KINDNESS  (기사의 친절도 및 응대 만족도, 1~5)
 * scoreResult    -> SATISFACTION.SCORE_RESULT    (A/S 처리 결과에 대한 전반적인 만족도, 1~5)
 * commentText    -> SATISFACTION.COMMENT_TEXT (선택 입력 후기)
 */
@Data
public class SatisfactionDTO {
    private Long scheduleNo;
    private int engineerNo;
    private int scoreVisit;
    private int scoreKindness;
    private int scoreResult;
    private String commentText;
}

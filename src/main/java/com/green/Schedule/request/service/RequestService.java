package com.green.Schedule.request.service;

import java.time.LocalDate;
import java.util.List;

import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.request.mapper.RequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class RequestService {

    @Autowired
    private RequestMapper requestMapper;

    /**
     * A/S 요청 저장
     * 접수 날짜(requestDate)는 사용자가 입력하는 값이 아니라
     * 서버에서 오늘 날짜로 자동으로 채워서 저장합니다.
     */
    public void save(RequestDTO requestDTO) {
        requestDTO.setRequestDate(LocalDate.now().toString()); // 예: 2026-09-09
        requestMapper.insertRequest(requestDTO);
    }

    /**
     * 중복 신청 확인
     * 같은 연락처로 같은 희망 날짜에 이미 접수된 요청이 있으면 true
     */
    public boolean isDuplicate(String customerTel, String wishDate) {
        int count = requestMapper.countDuplicate(customerTel, wishDate);
        return count > 0;
    }

    // 로그인한 회원이 신청한 전체 내역 조회
    public List<RequestDTO> getMyRequestList(int memNo) {
        return requestMapper.selectMyRequestList(memNo);
    }

    // 신청 상세 조회 (본인이 신청한 것만 - requestNo+memNo가 둘 다 맞아야 조회됨)
    public RequestDTO getRequestDetail(int requestNo, int memNo) {
        return requestMapper.selectRequestDetail(requestNo, memNo);
    }

}

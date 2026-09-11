package com.green.Schedule.request.mapper;

import java.util.List;

import com.green.Schedule.request.dto.RequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SQL 내용은 여기가 아니라
 * resources/mappers/request/RequestMapper.xml 에 작성합니다.
 */
@Mapper
public interface RequestMapper {

    // A/S 요청 등록
    void insertRequest(RequestDTO requestDTO);

    // 같은 연락처 + 같은 희망 날짜로 이미 접수된 요청이 몇 건인지 확인 (중복 체크용)
    int countDuplicate(@Param("customerTel") String customerTel,
                       @Param("wishDate") String wishDate);

    // [신규] 로그인한 회원(memNo)이 신청한 전체 내역 조회 (신청내역 조회 화면용)
    List<RequestDTO> selectMyRequestList(@Param("memNo") int memNo);
    // [신규] 신청 상세 조회 - requestNo와 memNo가 둘 다 맞아야 조회됨

    // (URL의 번호만 바꿔서 남의 신청 내역을 보는 것을 막기 위해 memNo도 같이 확인합니다)
    RequestDTO selectRequestDetail(@Param("requestNo") int requestNo, @Param("memNo") int memNo);

}

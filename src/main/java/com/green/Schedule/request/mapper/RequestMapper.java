package com.green.Schedule.request.mapper;

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

}

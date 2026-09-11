package com.green.Schedule.member.mapper;

import com.green.Schedule.member.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    // 아이디로 회원 한 명 조회 (로그인할 때 사용)
    MemberDTO selectMemberById(String memId);

    // 아이디 중복 확인 (회원가입 시 사용) - 0이면 사용 가능한 아이디
    int countById(String memId);

    // 회원가입 (ROLE은 SQL에서 다루지 않고 DB의 DEFAULT 'user'를 그대로 씁니다)
    void insertMember(MemberDTO memberDTO);

}

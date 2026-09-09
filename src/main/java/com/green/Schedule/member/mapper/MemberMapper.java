package com.green.Schedule.member.mapper;

import com.green.Schedule.member.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis 매퍼 인터페이스입니다.
 * 실제 SQL 내용은 여기가 아니라
 * resources/mappers/member/MemberMapper.xml 파일에 작성합니다.
 *
 * 메서드 이름과 XML의 id="..." 값이 반드시 똑같아야 연결됩니다.
 */
@Mapper
public interface MemberMapper {

    // 아이디로 회원 한 명 조회 (로그인할 때 사용)
    MemberDTO selectMemberById(String memId);

}

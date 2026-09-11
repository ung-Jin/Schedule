package com.green.Schedule.member.service;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 로그인 처리
     * @param memId 입력한 아이디
     * @param memPw 입력한 비밀번호
     * @return 로그인 성공 시 회원 정보(MemberDTO), 실패 시 null
     */
    public MemberDTO login(String memId, String memPw) {

        // 1. 아이디로 회원 조회
        MemberDTO member = memberMapper.selectMemberById(memId);

        // 2. 아이디가 존재하지 않는 경우
        if (member == null) {
            return null;
        }
        // 3. 비밀번호가 일치하지 않는 경우
        if (!member.getMemPw().equals(memPw)) {
            return null;
        }
        // 4. 아이디, 비밀번호 모두 일치 -> 로그인 성공
        return member;
    }

    // 아이디 중복 확인 (회원가입 시 사용)
    public boolean isIdDuplicate(String memId) {
        return memberMapper.countById(memId) > 0;
    }

    // 회원가입 처리
    public void join(MemberDTO memberDTO) {
        memberMapper.insertMember(memberDTO);
    }



}

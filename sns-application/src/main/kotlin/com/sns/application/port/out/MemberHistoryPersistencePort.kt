package com.sns.application.port.out

import com.sns.domain.member.MemberNicknameHistory

interface MemberHistoryPersistencePort {
    fun saveMemberHistory(memberNicknameHistory: MemberNicknameHistory)

    fun getMemberNicknameHistoriesByMemberId(memberId: Long): List<MemberNicknameHistory>
}

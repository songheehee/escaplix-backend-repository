package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** MEMBER_GENRE (D) */
public interface MemberGenreRepository extends JpaRepository<MemberGenre, Long> {

    List<MemberGenre> findByMember_MemberId(Long memberId);

    /** 선호 장르 통째 교체 (D-8) */
    void deleteByMember_MemberId(Long memberId);
}

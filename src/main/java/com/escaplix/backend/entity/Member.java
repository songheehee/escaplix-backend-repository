package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** MEMBER 회원 (A). 사장은 bizNo·bizFilePath 필수 (CHECK member_owner_ck) */
@Entity
@Table(name = "member")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_member")
    @SequenceGenerator(name = "sq_member", sequenceName = "sq_member", allocationSize = 1)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "login_id", nullable = false, length = 30)
    private String loginId;

    /** BCrypt 해시. 응답 DTO 에 절대 넣지 않는다 */
    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 60)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "biz_no", length = 12)
    private String bizNo;

    /** 비공개 폴더 경로 (FileStorage.savePrivate) */
    @Column(name = "biz_file_path", length = 500)
    private String bizFilePath;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

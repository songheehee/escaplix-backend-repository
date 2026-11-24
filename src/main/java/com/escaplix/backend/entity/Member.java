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

    /** 이메일 (소문자로 저장, UNIQUE). 이메일 칸이 생기기 전에 가입한 회원은 null */
    @Column(length = 100)
    private String email;

    @Column(name = "biz_no", length = 12)
    private String bizNo;

    /** 비공개 폴더 경로 (FileStorage.savePrivate) */
    @Column(name = "biz_file_path", length = 500)
    private String bizFilePath;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 탈퇴한 때. null = 탈퇴 안 함. 탈퇴하면 줄은 남기고 아이디·닉네임·이메일을 익명으로 바꾼다 */
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    /** 탈퇴한 회원의 닉네임. 후기·채팅·파티에 이 이름으로 남는다 (가입·닉네임 변경에선 못 씀) */
    public static final String WITHDRAWN_NICKNAME = "탈퇴한 회원";

    public boolean isWithdrawn() { return withdrawnAt != null; }
}

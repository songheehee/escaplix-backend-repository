package com.escaplix.backend.common.mail;

import com.escaplix.backend.common.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메일 보내기 (글자만). 이메일 인증번호에 쓴다.
 * - spring.mail.username·password 가 있으면 SMTP(Gmail)로 보낸다
 * - 비어 있으면 보내지 않고 서버 콘솔에 내용을 찍는다 (개발 모드: SMTP 설정이 없는 팀원도 인증번호를 콘솔에서 보고 테스트)
 * 개발 모드에서는 받는 사람별 마지막 메일을 기억해 둔다 (테스트가 인증번호를 꺼내 쓴다)
 */
@Slf4j
@Component
public class Mailer {

    private final ObjectProvider<JavaMailSender> senderProvider;
    private final String username;
    private final boolean smtp;
    private final Map<String, String> lastByTo = new ConcurrentHashMap<>();

    public Mailer(ObjectProvider<JavaMailSender> senderProvider,
                  @Value("${spring.mail.username:}") String username,
                  @Value("${spring.mail.password:}") String password) {
        this.senderProvider = senderProvider;
        this.username = username == null ? "" : username.trim();
        this.smtp = !this.username.isEmpty() && password != null && !password.isBlank();
        if (!smtp) log.info("[메일] spring.mail.username/password 가 없어서 개발 모드: 메일 대신 콘솔에 찍습니다");
    }

    /** 실제로 메일을 보내는지 (false 면 개발 모드) */
    public boolean isSmtp() { return smtp; }

    /** 보내기. 실패하면 503 (메일 서버 문제는 사용자가 고칠 수 없으니 잠시 후 다시) */
    public void send(String to, String subject, String text) {
        if (!smtp) {
            lastByTo.put(to, text);
            log.info("[메일 · 개발 모드] 받는 사람={} 제목={}\n{}", to, subject, text);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("Escaplix <" + username + ">");
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            senderProvider.getObject().send(msg);
        } catch (MailException e) {
            log.warn("[메일] 보내기 실패 to={}: {}", to, e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "메일을 보내지 못했어요. 잠시 후 다시 시도해 주세요.");
        }
    }

    /**
     * 기다리지 않고 보내기 (응답은 바로). 비밀번호 재설정처럼 "회원이 맞는지"를 응답 시간으로도 알려 주면 안 될 때 쓴다:
     * 맞으면 메일 보내느라 1~2초, 아니면 바로 → 시간 차이로 아이디·이메일이 맞는지 알 수 있어서.
     * 실패해도 사용자에게 알리지 않고 로그만 (화면은 "맞으면 보냈어요"라서 다시 받기를 누르면 됨).
     * 개발 모드는 콘솔에 찍기만 해서 바로 처리 (테스트가 바로 본문을 꺼낼 수 있게)
     */
    public void sendLater(String to, String subject, String text) {
        if (!smtp) { send(to, subject, text); return; }
        CompletableFuture.runAsync(() -> {
            try {
                send(to, subject, text);
            } catch (RuntimeException e) {
                log.warn("[메일] 나중에 보내기 실패 to={}: {}", to, e.getMessage());
            }
        });
    }

    /** 개발 모드에서 그 사람에게 마지막으로 "보낸" 메일 본문 (테스트용). SMTP 모드면 null */
    public String lastTextTo(String to) {
        return lastByTo.get(to);
    }
}

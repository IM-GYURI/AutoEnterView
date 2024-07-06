package com.ctrls.auto_enter_view.component;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailComponent {

  private final JavaMailSender mailSender;

  public void sendMail(String to, String subject, String text) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  public void sendVerificationCode(String to, String verificationCode) {

    String subject = "AutoEnterView 회원가입 인증 코드입니다.";
    String text = "인증 코드 : " + verificationCode;
    sendMail(to, subject, text);
  }

  public void sendTemporaryPassword(String to, String temporaryPassword) {

    String subject = "AutoEnterView 임시 비밀번호입니다.";
    String text = "임시 비밀번호 : " + temporaryPassword;
    sendMail(to, subject, text);
  }
}
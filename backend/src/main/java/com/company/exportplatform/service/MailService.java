package com.company.exportplatform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends transactional email when MAIL_HOST is configured; in local dev
 * without SMTP it logs the message so flows stay testable end-to-end.
 */
@Service
@Slf4j
public class MailService {

    private final String fromUser;
    private final boolean mailEnabled;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public MailService(@Value("${spring.mail.host:}") String mailHost,
                       @Value("${MAIL_FROM:no-reply@localhost}") String fromUser,
                       ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.fromUser = fromUser;
        this.mailEnabled = mailHost != null && !mailHost.isBlank();
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * @return true if the email was really sent, false if it was only logged.
     */
    public boolean sendHtml(String to, String subject, String htmlBody) {
        return deliver(to, subject, htmlBody, null, null, null);
    }

    /**
     * Same guarded behavior as {@link #sendHtml} but with a single binary
     * attachment (e.g. quotation/PI PDF). Never fails the caller's flow.
     *
     * @return true if the email was really sent, false if it was only logged.
     */
    public boolean sendHtmlWithAttachment(String to, String subject, String htmlBody,
                                          String attachmentFilename, byte[] attachmentData, String contentType) {
        return deliver(to, subject, htmlBody, attachmentFilename, attachmentData, contentType);
    }

    private boolean deliver(String to, String subject, String htmlBody,
                            String attachmentFilename, byte[] attachmentData, String contentType) {
        if (!mailEnabled) {
            log.info("[DEV MAIL] host not configured - would send to {} subject={} body:\n{}", to, subject, htmlBody);
            if (attachmentData != null) {
                log.info("[DEV MAIL] attachment {} ({} bytes) skipped", attachmentFilename, attachmentData.length);
            }
            return false;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("[DEV MAIL] host configured but no JavaMailSender bean - logging instead");
            log.info("[DEV MAIL] would send to {} subject={} body:\n{}", to, subject, htmlBody);
            return false;
        }
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    attachmentData != null, "UTF-8");
            helper.setFrom(fromUser);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachmentData != null) {
                helper.addAttachment(attachmentFilename,
                        new org.springframework.core.io.ByteArrayResource(attachmentData),
                        contentType != null ? contentType : "application/octet-stream");
            }
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            // Never leak or fail the business flow because of SMTP issues.
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            return false;
        }
    }
}

package com.iotmining.services.notification.handler;

import com.iotmining.common.base.notifications.dto.NotificationResponse;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.EmailPayload;
import com.iotmining.common.base.notifications.enums.NotificationType;
import com.iotmining.common.data.notifications.NotificationStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Delivers EMAIL notifications (including auth OTP codes) over SMTP via {@link JavaMailSender}.
 *
 * <p>Recovered on 2026-11-18 from the compiled class in the 2026-04-13 notification-service build:
 * the original source was never committed and was lost to iCloud sync. Behaviour is identical to the
 * version that delivered OTP emails in production (see notification-service.log, 2026-08-19).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationHandler implements NotificationHandler<EmailPayload> {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public Class<EmailPayload> payloadType() {
        return EmailPayload.class;
    }

    @Override
    public NotificationResponse handle(NotificationWrapper<EmailPayload> request) {
        UUID correlationId = request.getCorrelationId();
        EmailPayload payload = request.getPayload();
        log.info("[EMAIL] Handling notification for Correlation-ID: {}. To: {}", correlationId, payload.getTo());
        try {
            sendEmail(payload);
            log.info("Email sent successfully for Correlation-ID: {}", correlationId);
            return NotificationResponse.builder()
                    .status(NotificationStatus.SUCCESS)
                    .message("Email sent successfully")
                    .delivered(true)
                    .channel("EMAIL")
                    .correlationId(correlationId)
                    .build();
        } catch (Exception e) {
            log.error("Failed to send email for Correlation-ID: {}. Error: {}", correlationId, e.getMessage(), e);
            return NotificationResponse.builder()
                    .status(NotificationStatus.FAILURE)
                    .message("Email failed: " + e.getMessage())
                    .delivered(false)
                    .channel("EMAIL")
                    .correlationId(correlationId)
                    .build();
        }
    }

    private void sendEmail(EmailPayload payload) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        helper.setFrom(fromEmail);
        helper.setTo(payload.getTo());
        helper.setSubject(payload.getSubject());
        helper.setText(payload.getBody(), payload.isHtml());
        if (payload.getCc() != null && !payload.getCc().isEmpty()) {
            helper.setCc(payload.getCc());
        }
        if (payload.getBcc() != null && !payload.getBcc().isEmpty()) {
            helper.setBcc(payload.getBcc());
        }
        javaMailSender.send(mimeMessage);
    }
}

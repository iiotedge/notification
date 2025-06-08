//package com.iotmining.services.notification.services.sender;
//
//
//import com.iotmining.common.base.notifications.NotificationDtoImpl;
//import com.iotmining.common.base.notifications.dto.NotificationResponse;
//import com.iotmining.common.data.notifications.NotificationChannel;
//
//import com.iotmining.common.data.notifications.NotificationStatus;
//import com.iotmining.common.interfaces.notification.NotificationSender;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class EmailSender implements NotificationSender {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String from;
//
//    @Override
//    public boolean supports(NotificationChannel channel) {
//        return channel == NotificationChannel.EMAIL;
//    }
//
//    @Override
//    public NotificationResponse send(NotificationDtoImpl dto) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom(from);
//            helper.setTo(dto.getMeta().get("email"));
//            helper.setSubject("Alert Notification");
//            helper.setText(dto.getMessage(), true);
//            mailSender.send(message);
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.SUCCESS)
//                    .message("Email sent successfully")
//                    .providerMessageId(null) // If you're using Mailgun/SendGrid, you can fetch this
//                    .metadata(Map.of(
//                            "to", dto.getMeta().get("email"),
//                            "from", from,
//                            "subject", "Alert Notification"
//                    ))
//                    .build();
//
//        } catch (Exception e) {
//            return NotificationResponse.builder()
//                    .status(NotificationStatus.FAILURE)
//                    .message("Email failed: " + e.getMessage())
//                    .metadata(Map.of(
//                            "to", dto.getMeta().get("email"),
//                            "from", from,
//                            "error", e.getMessage()
//                    ))
//                    .build();
//        }
//    }
//}

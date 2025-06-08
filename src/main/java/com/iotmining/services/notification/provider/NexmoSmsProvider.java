package com.iotmining.services.notification.provider;

//import com.iotmining.common.base.notifications.NotificationDtoImpl;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
import com.iotmining.common.interfaces.notification.SmsProvider;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;

public class NexmoSmsProvider implements SmsProvider {

    private final VonageClient vonageClient;
    private final String from;

    public NexmoSmsProvider(
            String apiKey,
            String apiSecret,
            String from
    ) {
        this.vonageClient = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
        this.from = from;
    }

//    @Override
//    public void send(NotificationDtoImpl dto) {
//        String toPhone = dto.getMeta().get("phone");
//        String text = dto.getMessage();
//
//        TextMessage message = new TextMessage(from, toPhone, text);
//
//        System.out.println("[Inside NEXMO SMS PROVIDER]");
//        try {
//            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);
//            var result = response.getMessages().get(0);
//            if (result.getStatus() == MessageStatus.OK) {
//                System.out.println("Vonage SMS sent successfully to "+toPhone);
////                log.info("Vonage SMS sent successfully to {}", toPhone);
//                //return result.getMessageId();
//            } else {
//                System.out.println("Vonage SMS error: "+result.getErrorText());
////                log.error("Failed to send SMS: {}", result.getErrorText());
//                throw new RuntimeException("Vonage SMS error: " + result.getErrorText());
//            }
//        } catch (Exception e) {
////            log.error("Exception while sending Vonage SMS", e);
//            throw new RuntimeException("Failed to send SMS via Vonage", e);
//        }
//    }

    @Override
    public void send(NotificationWrapper<SmsPayload> request) {
        String toPhone = request.getPayload().getPhoneNumber();
        String text =  request.getPayload().getContent();

        TextMessage message = new TextMessage(from, toPhone, text);

        System.out.println("[Inside NEXMO SMS PROVIDER]");
        try {
            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);
            var result = response.getMessages().get(0);
            if (result.getStatus() == MessageStatus.OK) {
                System.out.println("Vonage SMS sent successfully to "+toPhone);
//                log.info("Vonage SMS sent successfully to {}", toPhone);
                //return result.getMessageId();
            } else {
                System.out.println("Vonage SMS error: "+result.getErrorText());
//                log.error("Failed to send SMS: {}", result.getErrorText());
                throw new RuntimeException("Vonage SMS error: " + result.getErrorText());
            }
        } catch (Exception e) {
//            log.error("Exception while sending Vonage SMS", e);
            throw new RuntimeException("Failed to send SMS via Vonage", e);
        }
    }
}

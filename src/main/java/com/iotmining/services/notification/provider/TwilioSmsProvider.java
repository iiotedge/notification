package com.iotmining.services.notification.provider;

//import com.iotmining.common.base.notifications.NotificationDtoImpl;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
import com.iotmining.common.interfaces.notification.SmsProvider;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;

public class TwilioSmsProvider implements SmsProvider {

    private final String accountSid;

    private final String authToken;

    private final String fromPhone;

    @PostConstruct
    public void init() {
        com.twilio.Twilio.init(accountSid, authToken);
    }

    public TwilioSmsProvider(String accountSid, String authToken, String fromPhone) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromPhone = fromPhone;
    }

//    @Override
//    public void send(NotificationDtoImpl smsRequest) {
//        System.out.println("[Inside Twilio SMS PROVIDER]");
//        Message.creator(
//                new PhoneNumber(smsRequest.getMeta().get("phone")),
//                new PhoneNumber(fromPhone),
//                smsRequest.getMessage()
//        ).create();
//    }

    @Override
    public void send(NotificationWrapper<SmsPayload> request) {
        System.out.println("[Inside Twilio SMS PROVIDER]");
        Message.creator(
                new PhoneNumber(request.getPayload().getPhoneNumber()),
                new PhoneNumber(fromPhone),
                request.getPayload().getContent()
        ).create();
    }
}

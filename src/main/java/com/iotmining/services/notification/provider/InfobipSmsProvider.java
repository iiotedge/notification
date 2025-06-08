package com.iotmining.services.notification.provider;

//import com.iotmining.common.base.notifications.NotificationDtoImpl;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
import com.iotmining.common.interfaces.notification.SmsProvider;
import okhttp3.*;
import java.io.IOException;

public class InfobipSmsProvider implements SmsProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String sender;
    private final OkHttpClient client = new OkHttpClient();

    public InfobipSmsProvider(String apiKey, String baseUrl, String sender) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.sender = sender;
    }

//    @Override
//    public void send(NotificationDtoImpl smsRequest) {
//        String phone = smsRequest.getMeta().get("phone");
//        String message = smsRequest.getMessage();
//        System.out.println("[Inside Infobip SMS PROVIDER]");
//        String json = String.format("""
//                    {
//                      "messages": [
//                        {
//                          "from": "%s",
//                          "destinations": [{"to": "%s"}],
//                          "text": "%s"
//                        }
//                      ]
//                    }
//                """, sender, phone, message);
//
//        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
//
//        Request request = new Request.Builder()
//                .url(baseUrl + "/sms/2/text/advanced")
//                .addHeader("Authorization", "App " + apiKey)
//                .addHeader("Content-Type", "application/json")
//                .post(body)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                throw new IOException("Infobip SMS failed: " + response.body().string());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to send Infobip SMS: " + e.getMessage(), e);
//        }
//    }

    public void send(NotificationWrapper<SmsPayload> request) {
        String phone = request.getPayload().getPhoneNumber();
        String message = request.getPayload().getContent();
        System.out.println("[Inside Infobip SMS PROVIDER]");
        String json = String.format("""
                    {
                      "messages": [
                        {
                          "from": "%s",
                          "destinations": [{"to": "%s"}],
                          "text": "%s"
                        }
                      ]
                    }
                """, sender, phone, message);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request httpRequest = new Request.Builder()
                .url(baseUrl + "/sms/2/text/advanced")
                .addHeader("Authorization", "App " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Infobip SMS failed: " + response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send Infobip SMS: " + e.getMessage(), e);
        }
    }
}


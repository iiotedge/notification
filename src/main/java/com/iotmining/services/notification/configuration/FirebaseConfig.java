//package com.iotmining.services.notification.configuration;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import java.io.IOException;
//import org.springframework.core.io.ClassPathResource;
//
//@Configuration
//public class FirebaseConfig {
//
//    @Value("${firebase.credentials.path}")
//    private String firebaseCredentialsPath;
//
//    @Bean
//    public FirebaseApp firebaseApp() throws IOException {
//        // Load the Firebase service account key from the classpath using ClassPathResource
//        ClassPathResource serviceAccountResource = new ClassPathResource(firebaseCredentialsPath);
//
//        if (!serviceAccountResource.exists()) {
//            throw new RuntimeException("Firebase service account key not found in classpath: " + serviceAccountResource.getPath());
//        }
//
//        // Use try-with-resources to automatically close the input stream
//        try (var inputStream = serviceAccountResource.getInputStream()) {
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(inputStream))
//                    .build();
//            return FirebaseApp.initializeApp(options);
//        } catch (IOException e) {
//            // Handle the exception and rethrow it if needed
//            throw new RuntimeException("Error initializing Firebase with service account", e);
//        }
//    }
//
//    @Bean
//    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
//        return FirebaseMessaging.getInstance(firebaseApp);
//    }
//}
//
//
//

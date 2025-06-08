//package com.iotmining.services.notification.configuration;
//
//
//import com.iotmining.services.notification.grpc.CriticalAlertServiceImpl;
//import io.grpc.Server;
//import io.grpc.ServerBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GrpcServerConfig {
//
//    @Bean
//    public Server grpcServer(CriticalAlertServiceImpl criticalAlertService) {
//        return ServerBuilder.forPort(9090)
//                .addService(criticalAlertService)
//                .build();
//    }
//}

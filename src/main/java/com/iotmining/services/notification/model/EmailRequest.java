package com.iotmining.services.notification.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "To phone email is required")
    private String to;

    @NotBlank(message = "Message body is required")
    private String body;

    @NotNull(message = "Priority is required")
    private Priority priority;
}

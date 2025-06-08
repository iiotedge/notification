package com.iotmining.services.notification.controller;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;

@RestController
@RequestMapping("/api/debug")
public class TracingDebugController {

    @Autowired
    private Tracer tracer;

    @GetMapping("/trace-id")
    public ResponseEntity<String> traceInfo() {
        String traceId = tracer.currentTraceContext().context() != null
                ? tracer.currentTraceContext().context().traceId()
                : "no-trace";
        String spanId = tracer.currentTraceContext().context() != null
                ? tracer.currentTraceContext().context().spanId()
                : "no-span";

        return ResponseEntity.ok("TraceId: " + traceId + " | SpanId: " + spanId);
    }

    @GetMapping("/mdc")
    public ResponseEntity<String> mdcInfo() {
        return ResponseEntity.ok("MDC TraceId: " + MDC.get("traceId") + ", SpanId: " + MDC.get("spanId"));
    }
}

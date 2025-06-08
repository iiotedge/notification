# 📢 Notification Service - API Documentation

Welcome to the **IoTMining Notification Service**. This service allows sending real-time alerts and notifications to users across multiple delivery channels:

- 🌐 WebSocket (Dashboard Notifications)
- 📲 SMS
- 🔔 Push Notifications (e.g., FCM)
- 💬 Telegram

---

## 🔧 Core Features

- [x] WebSocket-based notification delivery
- [x] SMS delivery via external gateway
- [x] Push notification (FCM) integration
- [x] Telegram bot support
- [x] Header validation: `Correlation-ID`, `Tenant-ID`, `Authorization`
- [ ] Update URI from `/api/notifications/send` to `/api/notify`
- [ ] Add email notification support
- [ ] Add WhatsApp integration (future phase)

---

## 🧪 Testing & Validation

- [x] Unit tests for JSON schema validation
- [ ] Integration tests for retry mechanism
- [ ] Load testing for high-volume notifications
- [ ] Validate message ordering by timestamp

---

## 📦 DevOps / Deployment

- [x] Dockerize service
- [ ] Helm chart creation for Kubernetes deployment
- [ ] CI/CD pipeline for staging and production environments
- [ ] Centralized logging and alerting setup

---

## 📚 Documentation

- [x] Create README with API usage and samples
- [x] Markdown file with examples per notification type
- [x] OpenAPI / Swagger documentation
- [ ] Postman collection for quick testing

---

## 📈 Future Enhancements

- [ ] Notification tracking status via API (`delivered`, `read`, `failed`)
- [ ] WebSocket acknowledgment and receipt storage
- [ ] User preferences (e.g., opt-in/out per channel)
- [ ] Admin dashboard to monitor notifications in real-time

---

## 🧾 Required HTTP Headers

All requests to the Notification API **must** include the following headers:

| Header           | Required | Description                                           | Example Value                        |
|------------------|----------|-------------------------------------------------------|--------------------------------------|
| `Authorization`  | ✅        | Bearer token for authentication                      | `Bearer eyJhbGciOiJIUzI1NiIs...`     |
| `Correlation-ID` | ✅        | Unique identifier for tracing requests across systems| `notif-req-001`                      |
| `Tenant-ID`      | ✅        | Identifier for the tenant/org sending the request    | `tenant-iot-mining`                 |

---

## 📋 Common JSON Structure

Each notification request has a common structure regardless of delivery type:

```json
{
  "type": "<WEB|SMS|PUSH|TELEGRAM>",
  "userId": "<UUID of the user>",
  "sourceApp": "<name of the source application>",
  "priority": "<LOW|MEDIUM|HIGH>",
  "retryCount": 0,
  "timestamp": <optional epoch milliseconds>,
  "correlationId": "<optional, unique request id>",
  "payload": { ... }
}
```
---

## Examples by Notification Type

## 1. 🌐 WebSocket Notification

Used to deliver UI-based notifications on the web dashboard.

**Request Example:**

```
POST /api/notifications/send
Authorization: Bearer <your-jwt-token>
Correlation-ID: notif-web-001
Tenant-ID: tenant-iot-mining
```

```json
{
  "type": "WEB",
  "userId": "3053e368-e82b-4d49-9983-eb46e9b77c31",
  "sourceApp": "iot-dashboard",
  "priority": "MEDIUM",
  "retryCount": 0,
  "payload": {
    "title": "Information",
    "message": "Your device came back online.",
    "type": "INFO",
    "url": "/devices/1001",
    "metadata": {
      "deviceId": "1001",
      "severity": "ALERT",
      "online": true,
      "source": "monitoring-service"
    }
  }
}
```

---

## 2. 📲 SMS Notification

Used to send text-based alerts to registered mobile numbers.

**Request Example:**

```
POST /api/notifications/send
Authorization: Bearer <your-jwt-token>
Correlation-ID: notif-sms-002
Tenant-ID: tenant-iot-mining
```

```json
{
  "type": "SMS",
  "userId": "3053e368-e82b-4d49-9983-eb46e9b77c3",
  "sourceApp": "iot-dashboard",
  "priority": "HIGH",
  "retryCount": 0,
  "timestamp": 1720351872000,
  "payload": {
    "phoneNumber": "+918501507",
    "content": "Alert! Your sensor has exceeded the threshold."
  }
}
```

---

## 3. 🔔 Push Notification

Used for mobile push alerts via FCM or compatible services.

**Request Example:**

```
POST /api/notifications/send
Authorization: Bearer <your-jwt-token>
Correlation-ID: push-def-003
Tenant-ID: tenant-iot-mining
```

```json
{
  "type": "PUSH",
  "userId": "3053e368-e82b-4d49-9983-eb46e9b77c3",
  "correlationId": "push-def-003",
  "sourceApp": "iot-control",
  "priority": "HIGH",
  "retryCount": 0,
  "timestamp": 1720351872000,
  "payload": {
    "deviceToken": "fcm_device_token_ABC123",
    "title": "Critical Alert",
    "message": "Battery voltage low!",
    "clickActionUrl": "https://dashboard.iotmining.com/devices/103"
  }
}
```

---

## 4. 💬 Telegram Notification

Used to notify users through Telegram bots.

**Request Example:**

```
POST /api/notifications/send
Authorization: Bearer <your-jwt-token>
Correlation-ID: telegram-xyz-002
Tenant-ID: tenant-fleet-manager
```

```json
{
  "type": "TELEGRAM",
  "userId": "3053e368-e82b-4d49-9983-eb46e9b77c3",
  "correlationId": "telegram-xyz-002",
  "sourceApp": "fleet-manager",
  "priority": "MEDIUM",
  "retryCount": 0,
  "timestamp": 1720351872000,
  "payload": {
    "chatId": "123456789",
    "message": "Your delivery vehicle has arrived at its destination."
  }
}
```

---

## 🚦 Priority Levels

| Level   | Description                          |
|---------|--------------------------------------|
| LOW     | Informational or optional messages   |
| MEDIUM  | Standard alerts or system updates    |
| HIGH    | Critical alerts or emergency events  |

---

## 🛠 Error Responses

| Status Code | Meaning                                |
|-------------|----------------------------------------|
| 200         | ✅ Notification accepted               |
| 400         | ❌ Invalid or malformed request         |
| 401         | ❌ Unauthorized (invalid token)         |
| 403         | ❌ Forbidden (wrong tenant or no access)|
| 500         | ❌ Internal server error                |

---

## 📎 Notes

- Use unique `Correlation-ID` per request for tracking.
- `retryCount` should start from `0`; the system will increment it on failure.
- `timestamp` is optional but recommended for message ordering and audits.
- For additional integration help or webhook delivery logs, contact the **IoTMining platform team**.
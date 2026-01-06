# Push Notification System

This backend implements push notifications for Android and iOS devices when a pill is dispensed from a MedBox.

## Features

- Firebase Cloud Messaging (FCM) integration for push notifications
- Device token management for multiple devices per user
- Automatic notification sending when pills are dispensed
- Automatic cleanup of invalid/unregistered tokens

## Setup

### 1. Firebase Project Setup

1. Create a Firebase project at https://console.firebase.google.com/
2. Enable Cloud Messaging (FCM) for your project
3. Download the service account credentials JSON file:
   - Go to Project Settings > Service Accounts
   - Click "Generate New Private Key"
   - Save the JSON file to a secure location on your server

### 2. Backend Configuration

Set the following environment variable to point to your Firebase credentials file:

```bash
FIREBASE_CREDENTIALS_PATH=/path/to/your/firebase-credentials.json
```

Or add it to your `application.properties`:

```properties
firebase.credentials.path=/path/to/your/firebase-credentials.json
```

### 3. Mobile App Integration

Mobile apps need to:

1. Obtain an FCM token from Firebase Cloud Messaging SDK
2. Register the token with the backend using the registration endpoint

## API Endpoints

### Register Device Token

Register a device to receive push notifications.

**Endpoint:** `POST /api/v1/notifications/register-token`

**Authentication:** Required (Bearer token)

**Request Body:**
```json
{
  "fcmToken": "string",
  "deviceType": "ANDROID" or "IOS"
}
```

**Response:**
```json
{
  "id": 1,
  "userId": "user@example.com",
  "fcmToken": "device-fcm-token",
  "deviceType": "ANDROID",
  "lastUpdated": 1234567890000
}
```

### Unregister Device Token

Remove a device token from receiving notifications.

**Endpoint:** `DELETE /api/v1/notifications/unregister-token?fcmToken={token}`

**Authentication:** Required (Bearer token)

**Query Parameters:**
- `fcmToken`: The FCM token to unregister

## Notification Flow

1. When a pill is scheduled to be dispensed, the `MedBoxDispenseSchedulerService` triggers the dispense
2. The `DeviceWebSocketService` sends the dispense command to the physical device via WebSocket
3. Simultaneously, the backend looks up the user associated with the device stack
4. For each registered device token for that user, a push notification is sent via FCM
5. Invalid or unregistered tokens are automatically removed from the database

## Notification Payload

Push notifications include:

**Title:** "Pill Dispensed"

**Body:** "{count} pill(s) dispensed from {boxName}, compartment {compartmentNumber}"

**Data:**
- `boxName`: Name of the MedBox device
- `compartmentNumber`: Zero-based compartment index
- `pillsDispensed`: Number of pills dispensed

## Error Handling

- If Firebase is not configured, notifications will be skipped with a warning log
- Invalid or unregistered tokens are automatically cleaned up
- Network errors are logged but don't block the dispense operation

## Security

- All endpoints require authentication via OAuth2/Keycloak
- Users can only register tokens for their own account
- Firebase credentials should be stored securely and not committed to version control

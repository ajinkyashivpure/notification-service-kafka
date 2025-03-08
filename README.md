So the complete flow is:

Your curl command sends a request to the email service API
The email service publishes a message to Kafka
The email service's Kafka listener picks up the message
The email service processes the message and attempts to send an email
Instead of connecting to a real SMTP server, it connects to MailHog
MailHog accepts the email and displays it in its web interface

# Notification System - Email Service Setup Guide

This guide provides step-by-step instructions for setting up and running the email service component of the microservices-based notification system.

## Project Structure

Ensure your project follows this structure:

```
notification-system/
├── pom.xml (parent pom)
├── docker-compose.yml
├── email-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/email/
│           │   ├── EmailServiceApplication.java
│           │   ├── config/
│           │   │   ├── KafkaConfig.java
│           │   │   └── MailConfig.java
│           │   ├── controller/
│           │   │   ├── HealthController.java
│           │   │   └── TestController.java
│           │   ├── model/
│           │   │   └── EmailNotification.java
│           │   └── service/
│           │       └── EmailService.java
│           └── resources/
│               └── application.yml
```

## Setting Up and Running the Email Service

### Prerequisites

- Docker and Docker Compose
- Java 17
- Maven (for local development)

### Step 1: Build and Start the Services

1. Navigate to the project root directory:
   ```bash
   cd notification-system
   ```

2. Start all services with Docker Compose:
   ```bash
   docker-compose up -d
   ```

   This will start:
    - Zookeeper
    - Kafka
    - MailHog (mock SMTP server)
    - Email Service

3. Check if all services are up and running:
   ```bash
   docker-compose ps
   ```

### Step 2: Create Kafka Topics

If topics are not created automatically, create them manually:

```bash
# Create the main topic for email notifications
docker exec -it kafka kafka-topics \
  --bootstrap-server kafka:9092 \
  --create \
  --if-not-exists \
  --topic email-notifications \
  --partitions 3 \
  --replication-factor 1

# Create the dead letter topic for failed messages
docker exec -it kafka kafka-topics \
  --bootstrap-server kafka:9092 \
  --create \
  --if-not-exists \
  --topic email-notifications-dlt \
  --partitions 1 \
  --replication-factor 1
```

Verify that the topics were created:
```bash
docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --list
```

### Step 3: Test the Email Service

#### Using the Test Endpoint

Send a test email using curl:
```bash
curl -X POST http://localhost:8081/api/test/send-email \
  -H 'Content-Type: application/json' \
  -d '{
    "to": "recipient@example.com",
    "from": "sender@example.com",
    "subject": "Test Email",
    "body": "This is a test email",
    "isHtml": false
  }'
```
curl -X POST http://localhost:8080/api/notifications \
-H 'Content-Type: application/json' \
-d '{
"recipient": "recipient@example.com",
"subject": "Test Notification via Email",
"content": "This is a test notification sent through the notification service to the email channel.",
"channels": ["EMAIL"]
}'
```

SMS Service 
```
curl -X POST http://localhost:8080/api/notifications \
-H 'Content-Type: application/json' \
-d '{
"recipient": "+1234567890",
"subject": "Test Notification",
"content": "This is a test notification with SMS channel.",
"channels": ["SMS"]
}'
```



Check the MailHog UI at http://localhost:8025 to see the received email.

#### Checking the Health Endpoint

To verify the service is running correctly:
```bash
curl http://localhost:8081/api/health
```

### Step 4: Monitoring and Debugging

To view the logs of a specific service:
```bash
docker-compose logs email-service
```

To follow the logs in real-time:
```bash
docker-compose logs -f email-service
```

## Configuration

### Email Service Configuration

The email service configuration is in `application.yml`. For development, it uses MailHog (configured in docker-compose.yml):

- SMTP host: mailhog
- SMTP port: 1025
- No authentication required

For production, update the environment variables:
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD

### Kafka Configuration

The Kafka configuration is in `application.yml` and `KafkaConfig.java`:

- Bootstrap servers: localhost:29092 (for local) or kafka:9092 (for within Docker)
- Consumer group: email-service-group

## Shutdown

To stop all services:
```bash
docker-compose down
```

To remove all containers and volumes:
```bash
docker-compose down -v
```


containers are not removed , so just do docker compose start:
1. in case you dont see the containers,
   docker compose build --no-cache
2. docker compose up -d
3. use the following curl command to test the email service 
      "THE FOLLOWING COMMAND IS SENT TO THE EMAIL SERVICE ENDPOINT DIRECTLY(DO NOT  PREFER)"
      ```bash
curl -X POST http://localhost:8081/api/test/send-email \
-H 'Content-Type: application/json' \
-d '{
"to": "recipient@example.com",
"from": "sender@example.com",
"subject": "Test Email",
"body": "This is a test email",
"isHtml": false
}'

   "THE FOLLOWING COMMAND GOES THROUGH THE NOTIFICATION SERVICE , THAT IS THE MAIN APPROACH "

```
curl -X POST http://localhost:8080/api/notifications \
-H 'Content-Type: application/json' \
-d '{
"recipient": "recipient@example.com",
"subject": "Test Notification via Email",
"content": "This is a test notification sent through the notification service to the email channel.",
"channels": ["EMAIL"]
}'
```
4. for checking the email service , go to local host 8085 (from docker:)
5. now ,check the sms services by following curl commands
   "THROUGH NOTIFICATION SERVICE"
   curl -X POST http://localhost:8080/api/notifications \
   -H 'Content-Type: application/json' \
   -d '{
   "recipient": "+1234567890",
   "subject": "Test Notification",
   "content": "This is a test notification with SMS channel.",
   "channels": ["SMS"]
   }'


```
   curl -X POST http://localhost:8082/api/test/send-sms \
   -H 'Content-Type: application/json' \
   -d '{
   "phoneNumber": "+1234567890",
   "message": "Direct test SMS"
   }'
```   
6. check the sms service on docker logs sms-service 

THE BASIC FLOW IS :
1. CLIENT SEND REQUEST TO THE NOTIFICATION SERVICE TROUGH CURL COMMANDS./PUBLISHER
2. NOTIFICATION SERVICE(PRODUCER) CHANNELIZES IT TO THE CORRECT KAFKA TOPIC , EITHER EMAIL OR SMS 
3. EMAIL SERVICE AND THE SMS SERVICE BOTH ARE KAFKA LISTENERS/CONSUMERS.
4. EMAIL SERVICE LISTENS ARE FURTHER SEND IT TO THE MAIL HOG WHICH STORES IT , INSTEAD OF 
   USING A ACTUAL SMTP SERVER 
5. SMS SERVICE JUST CONSUMES THE MESSAGES !
# Bajaj Finserv Health | Qualifier 1 | JAVA

## Overview
This Spring Boot app automates the workflow as per the problem statement:
- On startup, sends a POST request to generate a webhook and accessToken.
- Determines the assigned SQL problem based on regNo.
- Reads the final SQL query from `src/main/resources/final-query.txt`.
- Submits the solution to the webhook URL using JWT (accessToken) in the Authorization header.
- No controller/endpoint triggers the flow; it runs on startup only.

## How to Use
1. **Configure your details** in `src/main/resources/application.properties`:
   ```properties
   bajaj.hiring.name=YOUR_NAME
   bajaj.hiring.regNo=YOUR_REGNO
   bajaj.hiring.email=YOUR_EMAIL
   bajaj.hiring.generate.url=https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA
   bajaj.hiring.submit.url=https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA
   ```
2. **Place your final SQL query** in `src/main/resources/final-query.txt`.
3. **Build and run**:
   ```shell
   mvn clean package
   java -jar target/webhook-1.0.0.jar
   ```

## Requirements
- Uses `RestTemplate` for HTTP calls.
- Uses JWT (accessToken) in Authorization header for submission.
- No REST controller or endpoint is exposed.

## Submission Checklist
- Code (this repo)
- Final JAR output
- Public JAR file link (downloadable)

## Author
- [Your Name]

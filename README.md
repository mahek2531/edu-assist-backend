# Edu Assist Backend

Spring Boot backend for Edu Assist.

## Local Setup

1. Install Java and MySQL.
2. Create a local MySQL database, or let the default JDBC URL create `projectdb`.
3. Set the environment variables from `src/main/resources/application-example.properties`.
4. Start the API:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs locally on `http://localhost:8080` by default.

## Important Config

- `APP_CORS_ALLOWED_ORIGINS` controls which frontend URLs can call the backend.
- For local Vite development, use `http://localhost:5173`.
- For deployment later, add the deployed frontend URL, for example:

```text
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://your-frontend-domain.com
```

Do not commit real database passwords, mail app passwords, or Google client credentials.

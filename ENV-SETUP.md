# Environment Configuration Setup

## Overview
This project uses external configuration files to manage sensitive data like API keys and database credentials.

## Setup Instructions

### 1. Create Environment File
Copy the example file and fill in your actual values:
```bash
cp env.properties.example env.properties
```

### 2. Edit env.properties
Open `env.properties` and replace the placeholder values with your actual credentials:

```properties
# JWT Secret (generate a strong secret for production)
jwt.secret=your-actual-jwt-secret-here

# MongoDB Atlas Connection
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/database?retryWrites=true&w=majority&appName=app-name

# Deepgram API Key
api.deepgram.apiKey=your-actual-deepgram-key

# Gemini API Key
api.gemini.apiKey=your-actual-gemini-key
```

### 3. Security Notes
- ✅ `env.properties` is already added to `.gitignore`
- ✅ Never commit `env.properties` to version control
- ✅ Use strong, unique secrets for production
- ✅ Keep your API keys secure

### 4. Running the Application
```bash
# Development
mvn spring-boot:run

# Production
java -jar target/minutes-api-0.0.1-SNAPSHOT.jar
```

## File Structure
```
project/
├── env.properties          # Your actual secrets (DO NOT COMMIT)
├── env.properties.example  # Template file (safe to commit)
├── src/main/resources/
│   └── application.properties  # Main config (imports env.properties)
└── .gitignore              # Excludes env.properties
```

## How It Works
1. Spring Boot loads `application.properties`
2. `application.properties` imports `env.properties` using `spring.config.import`
3. Properties from `env.properties` are available throughout the application
4. Sensitive data stays out of version control

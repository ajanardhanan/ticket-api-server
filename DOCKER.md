# Docker Deployment Guide for Ticket API Server

## Overview

This guide explains how to build and deploy the Ticket Management API using Docker. The application is containerized using a multi-stage Dockerfile for optimal image size and security.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+ (optional, for easier deployment)
- GCP Service Account Key (for Firestore access)
- Port 8080 available

## Quick Start

### Option 1: Using Docker Compose (Recommended)

```bash
# 1. Set your GCP service account key path
export GCP_KEY_PATH=/path/to/your/service-account-key.json

# 2. Start the application
docker-compose up -d

# 3. Check logs
docker-compose logs -f

# 4. Stop the application
docker-compose down
```

### Option 2: Using Docker Directly

```bash
# 1. Build the image
docker build -t ticket-api-server:latest .

# 2. Run the container
docker run -d \
  --name ticket-api-server \
  -p 8080:8080 \
  -v /path/to/service-account-key.json:/app/gcp/service-account-key.json:ro \
  -e GOOGLE_APPLICATION_CREDENTIALS=/app/gcp/service-account-key.json \
  -e GCP_PROJECT_ID=aihealthcare-449603 \
  ticket-api-server:latest

# 3. Check logs
docker logs -f ticket-api-server

# 4. Stop the container
docker stop ticket-api-server
docker rm ticket-api-server
```

## Dockerfile Architecture

### Multi-Stage Build

The Dockerfile uses a multi-stage build for efficiency:

**Stage 1: Build (Maven)**
- Base Image: `maven:3.9.5-eclipse-temurin-17-alpine`
- Downloads dependencies
- Compiles source code
- Packages JAR file

**Stage 2: Runtime (JRE)**
- Base Image: `eclipse-temurin:17-jre-alpine`
- Lightweight runtime environment
- Non-root user for security
- Only includes compiled JAR

### Image Size Comparison

```
Build stage: ~500 MB (includes Maven + build tools)
Runtime stage: ~200 MB (JRE + application JAR)
Final image: ~200 MB ✅ (70% smaller!)
```

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `GCP_PROJECT_ID` | Google Cloud Project ID | `aihealthcare-449603` | Yes |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to service account key | `/app/gcp/service-account-key.json` | Yes |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m` | No |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `default` | No |

### Docker Compose Configuration

Edit `docker-compose.yml` to customize:

```yaml
environment:
  - GCP_PROJECT_ID=your-project-id
  - JAVA_OPTS=-Xms512m -Xmx1024m  # Increase memory
  
volumes:
  - ./your-key.json:/app/gcp/service-account-key.json:ro
```

## GCP Service Account Setup

### 1. Create Service Account

```bash
gcloud iam service-accounts create ticket-api-sa \
  --description="Service account for Ticket API" \
  --display-name="Ticket API Service Account" \
  --project=aihealthcare-449603
```

### 2. Grant Firestore Permissions

```bash
gcloud projects add-iam-policy-binding aihealthcare-449603 \
  --member="serviceAccount:ticket-api-sa@aihealthcare-449603.iam.gserviceaccount.com" \
  --role="roles/datastore.user"
```

### 3. Download Service Account Key

```bash
gcloud iam service-accounts keys create ./gcp-key.json \
  --iam-account=ticket-api-sa@aihealthcare-449603.iam.gserviceaccount.com
```

### 4. Secure the Key

```bash
# Set proper permissions
chmod 600 gcp-key.json

# Add to .gitignore
echo "gcp-key.json" >> .gitignore
```

## Building the Image

### Standard Build

```bash
docker build -t ticket-api-server:latest .
```

### Build with Tag

```bash
docker build -t ticket-api-server:v1.0.0 .
```

### Build with No Cache

```bash
docker build --no-cache -t ticket-api-server:latest .
```

### Build for Multiple Platforms

```bash
docker buildx build --platform linux/amd64,linux/arm64 \
  -t ticket-api-server:latest .
```

## Running the Container

### Basic Run

```bash
docker run -d \
  --name ticket-api-server \
  -p 8080:8080 \
  -v $(pwd)/gcp-key.json:/app/gcp/service-account-key.json:ro \
  -e GOOGLE_APPLICATION_CREDENTIALS=/app/gcp/service-account-key.json \
  ticket-api-server:latest
```

### Run with Custom Memory

```bash
docker run -d \
  --name ticket-api-server \
  -p 8080:8080 \
  -v $(pwd)/gcp-key.json:/app/gcp/service-account-key.json:ro \
  -e GOOGLE_APPLICATION_CREDENTIALS=/app/gcp/service-account-key.json \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  ticket-api-server:latest
```

### Run with Custom Network

```bash
# Create network
docker network create ticket-network

# Run container
docker run -d \
  --name ticket-api-server \
  --network ticket-network \
  -p 8080:8080 \
  -v $(pwd)/gcp-key.json:/app/gcp/service-account-key.json:ro \
  -e GOOGLE_APPLICATION_CREDENTIALS=/app/gcp/service-account-key.json \
  ticket-api-server:latest
```

## Health Checks

### Docker Health Check

The Dockerfile includes automatic health checks:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

### Manual Health Check

```bash
# Check container health
docker ps --filter name=ticket-api-server

# Check actuator endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Health Check Status

```bash
# View detailed health status
docker inspect --format='{{.State.Health.Status}}' ticket-api-server

# Possible values:
# - starting: Container is starting
# - healthy: Health check passing
# - unhealthy: Health check failing
```

## Logging

### View Logs

```bash
# Follow logs
docker logs -f ticket-api-server

# Last 100 lines
docker logs --tail 100 ticket-api-server

# Since specific time
docker logs --since 10m ticket-api-server

# With timestamps
docker logs -t ticket-api-server
```

### Docker Compose Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f ticket-api

# Last 50 lines
docker-compose logs --tail 50
```

## Monitoring

### Container Stats

```bash
# Real-time stats
docker stats ticket-api-server

# One-time stats
docker stats --no-stream ticket-api-server
```

### Resource Usage

```bash
# CPU and Memory
docker inspect ticket-api-server | grep -A 20 Resources

# Disk usage
docker system df
```

## Troubleshooting

### Container Won't Start

**Problem:** Container exits immediately

**Solution:**
```bash
# Check logs for errors
docker logs ticket-api-server

# Check if port is in use
lsof -i :8080

# Verify GCP credentials path
docker exec ticket-api-server ls -la /app/gcp/
```

### Firestore Connection Errors

**Problem:** "UNAUTHENTICATED" errors

**Solution:**
```bash
# Verify credentials are mounted
docker exec ticket-api-server cat /app/gcp/service-account-key.json

# Check environment variable
docker exec ticket-api-server env | grep GOOGLE_APPLICATION_CREDENTIALS

# Verify service account permissions
gcloud projects get-iam-policy aihealthcare-449603 \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:ticket-api-sa@*"
```

### Out of Memory Errors

**Problem:** Container crashes with OOM

**Solution:**
```bash
# Increase memory limits
docker run -d \
  --name ticket-api-server \
  -m 1g \
  -e JAVA_OPTS="-Xms512m -Xmx896m" \
  ...

# Or update docker-compose.yml:
# services:
#   ticket-api:
#     mem_limit: 1g
```

### Health Check Failing

**Problem:** Container shows as "unhealthy"

**Solution:**
```bash
# Test health endpoint manually
docker exec ticket-api-server wget -qO- http://localhost:8080/actuator/health

# Check application logs
docker logs ticket-api-server | grep -i error

# Increase health check timeout
docker run -d \
  --health-timeout=10s \
  --health-start-period=60s \
  ...
```

## Production Deployment

### Docker Compose Production Config

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  ticket-api:
    image: ticket-api-server:v1.0.0
    container_name: ticket-api-server
    restart: always
    ports:
      - "8080:8080"
    environment:
      - GCP_PROJECT_ID=aihealthcare-449603
      - JAVA_OPTS=-Xms512m -Xmx1024m
      - SPRING_PROFILES_ACTIVE=production
    volumes:
      - /secure/path/to/gcp-key.json:/app/gcp/service-account-key.json:ro
    mem_limit: 1200m
    cpus: 1.0
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "5"
    networks:
      - ticket-network

networks:
  ticket-network:
    driver: bridge
```

Deploy:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

For Kubernetes deployment, see `k8s/` directory (to be created separately).

## Pushing to Container Registry

### Docker Hub

```bash
# Tag image
docker tag ticket-api-server:latest yourusername/ticket-api-server:latest

# Login
docker login

# Push
docker push yourusername/ticket-api-server:latest
```

### Google Container Registry (GCR)

```bash
# Configure Docker for GCR
gcloud auth configure-docker

# Tag image
docker tag ticket-api-server:latest \
  gcr.io/aihealthcare-449603/ticket-api-server:latest

# Push to GCR
docker push gcr.io/aihealthcare-449603/ticket-api-server:latest
```

### Google Artifact Registry

```bash
# Configure Docker for Artifact Registry
gcloud auth configure-docker us-central1-docker.pkg.dev

# Tag image
docker tag ticket-api-server:latest \
  us-central1-docker.pkg.dev/aihealthcare-449603/ticket-api/ticket-api-server:latest

# Push to Artifact Registry
docker push us-central1-docker.pkg.dev/aihealthcare-449603/ticket-api/ticket-api-server:latest
```

## Security Best Practices

### 1. Non-Root User
✅ Already implemented - application runs as `appuser` (UID 1001)

### 2. Read-Only File System
```bash
docker run -d \
  --read-only \
  --tmpfs /tmp \
  -v $(pwd)/gcp-key.json:/app/gcp/service-account-key.json:ro \
  ticket-api-server:latest
```

### 3. Security Scanning
```bash
# Scan with Docker Scout
docker scout cves ticket-api-server:latest

# Scan with Trivy
trivy image ticket-api-server:latest
```

### 4. Secrets Management
Use Docker secrets or environment variables from secret managers:
```bash
# Using Docker secrets (Swarm)
docker secret create gcp_key gcp-key.json
docker service create --secret gcp_key ...
```

## Performance Tuning

### JVM Options

```bash
# Recommended for 1GB container
JAVA_OPTS="-Xms512m -Xmx896m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Recommended for 2GB container
JAVA_OPTS="-Xms1g -Xmx1792m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Resource Limits

```yaml
# docker-compose.yml
services:
  ticket-api:
    mem_limit: 1g
    mem_reservation: 512m
    cpus: 1.0
    cpu_shares: 1024
```

## Cleanup

### Remove Container

```bash
# Stop and remove
docker stop ticket-api-server
docker rm ticket-api-server

# Force remove
docker rm -f ticket-api-server
```

### Remove Images

```bash
# Remove specific image
docker rmi ticket-api-server:latest

# Remove all unused images
docker image prune -a

# Remove everything
docker system prune -a --volumes
```

### Docker Compose Cleanup

```bash
# Stop and remove containers
docker-compose down

# Remove volumes too
docker-compose down -v

# Remove images too
docker-compose down --rmi all
```

## Testing the Deployment

### 1. Verify Container is Running

```bash
docker ps | grep ticket-api-server
```

### 2. Test Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### 3. Test API Endpoints

```bash
# Create an agent
curl -X POST http://localhost:8080/api/agents \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Agent","email":"test@example.com"}'

# Get all agents
curl http://localhost:8080/api/agents
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: docker build -t ticket-api-server:${{ github.sha }} .
      
      - name: Push to registry
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker push ticket-api-server:${{ github.sha }}
```

## Summary

**Image Details:**
- Base: Alpine Linux (lightweight)
- JRE: Eclipse Temurin 17
- Size: ~200 MB
- User: Non-root (appuser)
- Health Check: Enabled
- Logging: JSON format with rotation

**Key Files:**
- `Dockerfile` - Multi-stage build configuration
- `docker-compose.yml` - Easy deployment configuration
- `.dockerignore` - Build optimization
- `DOCKER.md` - This documentation

**Next Steps:**
1. Obtain GCP service account key
2. Update `docker-compose.yml` with key path
3. Run `docker-compose up -d`
4. Test APIs at http://localhost:8080

For Firestore setup, see `FIRESTORE_MIGRATION.md`.

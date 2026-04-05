# Firestore Migration Guide

## Overview

The ticket-api-server has been migrated from H2 in-memory database with JPA to **Google Cloud Firestore** as the persistence layer.

## Changes Summary

### 1. Dependencies Updated

**Removed:**
- `spring-boot-starter-data-jpa`
- `h2` database

**Added:**
- `google-cloud-firestore` (v3.14.5)
- `spring-cloud-gcp-starter-firestore` (v4.8.0)

### 2. Model Changes

All entity models have been updated to work with Firestore:

**Agent.java:**
- Removed JPA annotations (`@Entity`, `@Table`, `@Column`, etc.)
- Changed `id` type from `Long` to `String`
- Added `@DocumentId` annotation for Firestore
- Removed bidirectional relationship with Tickets

**Ticket.java:**
- Changed `id` type from `Long` to `String`
- Changed `assignedAgent` (Agent object) to `assignedAgentId` (String)
- Removed relationship with Rating
- Removed JPA lifecycle callbacks

**Rating.java:**
- Changed `id` type from `Long` to `String`
- Changed `ticket` (Ticket object) to `ticketId` (String)
- Changed `agent` (Agent object) to `agentId` (String)

### 3. Repository Changes

All repositories have been rewritten as concrete classes using Firestore SDK:

**AgentRepository:**
- Changed from `JpaRepository` interface to concrete class
- Implements CRUD operations using Firestore `CollectionReference`
- Collection name: `agents`

**TicketRepository:**
- Changed from `JpaRepository` interface to concrete class
- Implements CRUD operations using Firestore `CollectionReference`
- Collection name: `tickets` ✅
- Methods use query by `assignedAgentId` instead of Agent object

**RatingRepository:**
- Changed from `JpaRepository` interface to concrete class
- Implements aggregation logic in-memory for agent ratings
- Collection name: `ratings`
- `findAgentAverageRatings()` groups and calculates averages in application code

### 4. Service Layer Updates

**TicketService:**
- Updated all methods to use `String` IDs instead of `Long`
- Removed `@Transactional` annotations (Firestore doesn't use JPA transactions)
- Updated to work with `assignedAgentId` instead of Agent object
- Changed exception handling from `EntityNotFoundException` to `RuntimeException`

### 5. Controller Updates

Both controllers updated to use `String` for all ID path variables:
- `@PathVariable String ticketId` (was Long)
- `@PathVariable String agentId` (was Long)

### 6. DTO Updates

- `AssignTicketRequest`: Changed `agentId` from `Long` to `String`
- `AgentRatingResponse`: Changed `agentId` from `Long` to `String`

## GCP Configuration

### Project Details

- **GCP Project ID:** `aihealthcare-449603`
- **Database:** Google Cloud Firestore
- **Collections:**
  - `tickets` - Stores all ticket documents
  - `agents` - Stores all agent documents
  - `ratings` - Stores all rating documents

### Setup Instructions

#### 1. Create GCP Project (if not exists)

```bash
gcloud projects create aihealthcare-449603 --name="AI Healthcare"
```

#### 2. Enable Firestore API

```bash
gcloud services enable firestore.googleapis.com --project=aihealthcare-449603
```

#### 3. Create Firestore Database

Go to [Google Cloud Console](https://console.cloud.google.com/firestore) and:
1. Select project `aihealthcare-449603`
2. Click "Create Database"
3. Choose "Native Mode"
4. Select a location (e.g., `us-central`)
5. Click "Create Database"

#### 4. Authentication Setup

**Option A: Service Account Key (for local development)**

```bash
# Create service account
gcloud iam service-accounts create ticket-api-sa \
    --description="Service account for Ticket API" \
    --display-name="Ticket API Service Account" \
    --project=aihealthcare-449603

# Grant Firestore permissions
gcloud projects add-iam-policy-binding aihealthcare-449603 \
    --member="serviceAccount:ticket-api-sa@aihealthcare-449603.iam.gserviceaccount.com" \
    --role="roles/datastore.user"

# Create and download key
gcloud iam service-accounts keys create ~/ticket-api-key.json \
    --iam-account=ticket-api-sa@aihealthcare-449603.iam.gserviceaccount.com

# Set environment variable
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/ticket-api-key.json"
```

**Option B: Application Default Credentials (recommended)**

```bash
# Authenticate with your GCP account
gcloud auth application-default login --project=aihealthcare-449603
```

## Running the Application

### Prerequisites

- Java 17+
- Maven 3.6+
- GCP account with Firestore enabled
- Authentication configured (see above)

### Start the Application

```bash
# Ensure GOOGLE_APPLICATION_CREDENTIALS is set (Option A) OR
# gcloud auth application-default login has been run (Option B)

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080` and connect to Firestore automatically.

## Firestore Collections Structure

### Collection: `tickets`

```json
{
  "id": "auto-generated-id",
  "title": "Fix login bug",
  "description": "Users unable to login",
  "status": "OPEN",
  "priority": "HIGH",
  "assignedAgentId": "agent-document-id",
  "createdAt": "2026-04-04T10:30:00",
  "updatedAt": "2026-04-04T10:30:00",
  "closedAt": null
}
```

### Collection: `agents`

```json
{
  "id": "auto-generated-id",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": "2026-04-04T10:00:00"
}
```

### Collection: `ratings`

```json
{
  "id": "auto-generated-id",
  "ticketId": "ticket-document-id",
  "agentId": "agent-document-id",
  "score": 5,
  "feedback": "Excellent service!",
  "createdAt": "2026-04-04T11:00:00"
}
```

## API Endpoints (No Changes)

All API endpoints remain the same, but now use String IDs:

```bash
# Create agent
POST /api/agents
{
  "name": "Jane Smith",
  "email": "jane@example.com"
}

# Create ticket
POST /api/tickets
{
  "title": "Password reset",
  "description": "User needs password reset",
  "priority": "MEDIUM"
}

# Assign ticket (use Firestore document IDs from responses)
PUT /api/tickets/{firestore-ticket-id}/assign
{
  "agentId": "{firestore-agent-id}"
}

# Close ticket
PUT /api/tickets/{firestore-ticket-id}/close

# Rate ticket
POST /api/tickets/{firestore-ticket-id}/rate
{
  "score": 5,
  "feedback": "Great support!"
}

# Get agent ratings (sorted by average score DESC)
GET /api/tickets/agents/ratings
```

## Testing with Firestore

### Using Firestore Emulator (Optional for Local Testing)

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Start Firestore emulator
firebase emulators:start --only firestore --project=aihealthcare-449603

# Update application.properties to use emulator
# Add: FIRESTORE_EMULATOR_HOST=localhost:8080
```

### Viewing Data in GCP Console

1. Go to https://console.cloud.google.com/firestore
2. Select project `aihealthcare-449603`
3. View collections: `tickets`, `agents`, `ratings`
4. Browse documents and see real-time updates

## Key Differences from JPA

| Feature | JPA/H2 | Firestore |
|---------|--------|-----------|
| **ID Type** | Long (auto-increment) | String (auto-generated UUID) |
| **Relationships** | Bidirectional (@OneToMany, @ManyToOne) | Denormalized (store IDs only) |
| **Transactions** | @Transactional annotation | Not applicable (eventual consistency) |
| **Queries** | JPQL/HQL | Firestore Query API |
| **Aggregations** | Database-level (AVG, COUNT, GROUP BY) | Application-level (in-memory) |
| **Persistence** | In-memory (H2) | Cloud-persisted |

## Performance Considerations

1. **Agent Ratings Aggregation:** 
   - Calculated in-memory by fetching all ratings
   - For large datasets, consider caching or pre-computing

2. **Document Reads:**
   - Firestore charges per document read
   - Optimize queries to minimize reads

3. **Indexing:**
   - Firestore automatically indexes fields
   - Composite indexes may be needed for complex queries

## Troubleshooting

### Error: "Permission denied"

**Solution:** Ensure service account has `roles/datastore.user` role or use `gcloud auth application-default login`

### Error: "UNAUTHENTICATED: Request had invalid authentication credentials"

**Solution:** Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable or run `gcloud auth application-default login`

### Error: "Firestore API has not been enabled"

**Solution:**
```bash
gcloud services enable firestore.googleapis.com --project=aihealthcare-449603
```

### Error: "Collection not found"

**Cause:** Firestore collections are created automatically on first write.
**Solution:** Create a document through the API and the collection will be created.

## Migration Checklist

- [x] Update pom.xml dependencies
- [x] Remove JPA annotations from models
- [x] Change ID types from Long to String
- [x] Rewrite repositories for Firestore
- [x] Update service layer methods
- [x] Update controller path variables
- [x] Update DTOs
- [x] Remove @Transactional annotations
- [x] Update application.properties
- [x] Create Firestore configuration class
- [x] Test CRUD operations
- [x] Test agent ratings calculation

## Next Steps

1. **Update Tests:** Modify integration tests to work with Firestore emulator
2. **Add Caching:** Consider adding Redis for agent ratings
3. **Add Indexes:** Create composite indexes for complex queries
4. **Monitor Costs:** Set up billing alerts for Firestore usage
5. **Backup Strategy:** Configure Firestore scheduled exports

## Support

For issues or questions:
- Google Cloud Firestore Docs: https://cloud.google.com/firestore/docs
- Spring Cloud GCP Docs: https://spring.io/projects/spring-cloud-gcp

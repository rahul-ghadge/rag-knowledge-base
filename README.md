# 🤖 RAG Knowledge Base

> **AI-Powered Enterprise Knowledge Assistant** — Upload PDFs/docs, ask questions, semantic search, internal company assistant.

Built with **Spring Boot 3.5**, **Spring AI**, **LangChain4j**, **PostgreSQL pgvector**, and an interactive web UI.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📁 Document Upload | PDF, DOCX, DOC, TXT, MD (up to 50MB) |
| 🧠 AI Ingestion | Auto-chunk + embed via OpenAI `text-embedding-3-small` |
| 💬 Conversational Q&A | Multi-turn chat with GPT-4o + RAG context injection |
| 🔍 Semantic Search | pgvector HNSW cosine similarity search |
| 📊 Interactive UI | Built-in Thymeleaf + Vanilla JS UI (no separate frontend needed) |
| 📖 API Docs | Swagger UI at `/swagger-ui.html` |
| 📈 Observability | Actuator + Prometheus metrics at `/actuator/prometheus` |
| 🐳 Docker Ready | Multi-stage Dockerfile + full docker-compose stack |
| 🔄 DB Migrations | Flyway versioned migrations |
| ✅ Tests | Unit + Integration tests (Testcontainers + pgvector) |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    RAG Knowledge Base                   │
├─────────────┬──────────────┬──────────────┬────────────┤
│  Web UI     │  REST API    │  Spring AI   │ LangChain4j│
│ (Thymeleaf) │  (Spring MVC)│  ChatClient  │  Splitter  │
├─────────────┴──────────────┴──────┬───────┴────────────┤
│              Service Layer         │                    │
│  DocumentService | RagService      │   AsyncIngestion   │
├───────────────────────────────────┴────────────────────┤
│                  Repository Layer (JPA)                 │
├────────────────────────┬───────────────────────────────┤
│   PostgreSQL + pgvector│    OpenAI API (embeddings+LLM) │
└────────────────────────┴───────────────────────────────┘
```

### Package Structure

```
src/main/java/com/company/ragknowledgebase/
├── config/          # Spring config: VectorStore, CORS, Async, Swagger, Audit
├── constant/        # AppConstants (API paths, status values)
├── controller/      # REST controllers + UI controller
├── exception/       # Custom exceptions + GlobalExceptionHandler
│   └── handler/
├── model/
│   ├── dto/
│   │   ├── request/     # ChatRequest, SearchRequest, DocumentUploadRequest
│   │   └── response/    # ApiResponse<T>, ResponseDtos (Document, Chat, Search)
│   └── entity/          # Document, DocumentChunk, ChatSession, ChatMessage
├── repository/      # JPA repositories
├── service/         # Service interfaces
│   └── impl/        # DocumentServiceImpl, RagServiceImpl, DocumentIngestionServiceImpl
└── util/            # FileUtils
```

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- OpenAI API key

### 1. Clone & configure

```bash
git clone https://github.com/your-org/rag-knowledge-base.git
cd rag-knowledge-base

cp .env.example .env
# Edit .env and set OPENAI_API_KEY=sk-...
```

### 2. Run with Docker Compose

```bash
docker compose up -d
```

App starts at → **http://localhost:8080**

### 3. Run locally (dev)

```bash
# Start only PostgreSQL
docker compose up postgres -d

# Run Spring Boot
./gradlew bootRun
```

---

## 🌐 API Endpoints

### Documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/documents/upload` | Upload file (multipart) |
| `GET`  | `/api/v1/documents` | List all documents (paginated) |
| `GET`  | `/api/v1/documents/{id}` | Get document by ID |
| `DELETE` | `/api/v1/documents/{id}` | Soft-delete document |
| `GET`  | `/api/v1/documents/search?name=` | Search by name |

### Chat & Search

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/chat` | Ask a question (RAG) |
| `POST` | `/api/v1/search` | Semantic similarity search |
| `GET`  | `/api/v1/chat/sessions?userIdentifier=` | List user sessions |
| `GET`  | `/api/v1/chat/sessions/{id}/history` | Get session history |
| `DELETE` | `/api/v1/chat/sessions/{id}` | Close session |

### System

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Interactive API docs |
| `/api-docs` | OpenAPI JSON |
| `/actuator/health` | Health check |
| `/actuator/prometheus` | Prometheus metrics |

---

## 📝 Example API Calls

### Upload a document
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@company-handbook.pdf" \
  -F "description=Company handbook" \
  -F "tags=hr,onboarding"
```

### Ask a question
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is our vacation policy?",
    "userIdentifier": "john.doe",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

### Semantic search
```bash
curl -X POST http://localhost:8080/api/v1/search \
  -H "Content-Type: application/json" \
  -d '{"query": "parental leave", "topK": 3, "similarityThreshold": 0.75}'
```

---

## ⚙️ Configuration

Key settings in `application.yml`:

```yaml
app:
  rag:
    chunk-size: 1000          # tokens per chunk
    chunk-overlap: 200        # overlap between chunks
    top-k-results: 5          # default results for RAG retrieval
    similarity-threshold: 0.7 # min cosine similarity

  upload:
    allowed-extensions: pdf,docx,doc,txt,md
    storage-path: ./uploads

spring:
  ai:
    openai:
      chat.options.model: gpt-4o
      embedding.options.model: text-embedding-3-small
    vectorstore:
      pgvector:
        dimensions: 1536        # matches text-embedding-3-small
        index-type: HNSW
        distance-type: COSINE_DISTANCE
```

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# With coverage report
./gradlew test jacocoTestReport

# Integration tests (requires Docker for Testcontainers)
./gradlew integrationTest
```

Coverage report at `build/reports/jacoco/test/html/index.html`

---

## 🐳 Docker

```bash
# Build image
docker build -t rag-knowledge-base:latest .

# Full stack (app + postgres + pgadmin)
docker compose --profile dev up -d

# Production (app + postgres only)
docker compose up -d

# View logs
docker compose logs -f app

# Stop
docker compose down
```

---

## 🔧 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.0 | Application framework |
| Spring AI | 1.0.0 | LLM + embedding integration |
| LangChain4j | 0.36.2 | Document splitting + LLM chains |
| PostgreSQL | 16 | Primary database |
| pgvector | latest | Vector similarity search |
| Flyway | latest | DB schema migrations |
| OpenAI | GPT-4o / text-embedding-3-small | LLM + embeddings |
| Apache Tika | 2.9.2 | Multi-format doc parsing |
| Lombok | 1.18.36 | Boilerplate reduction |
| MapStruct | 1.6.3 | DTO mapping |
| Springdoc OpenAPI | 2.8.4 | Swagger UI |
| Testcontainers | 1.20.4 | Integration testing |
| Gradle | 8.12 | Build tool |
| Docker | — | Containerization |

---

## 📁 Project Structure

```
rag-knowledge-base/
├── src/
│   ├── main/
│   │   ├── java/com/company/ragknowledgebase/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       ├── db/migration/         # Flyway SQL migrations
│   │       ├── static/css/           # UI styles
│   │       ├── static/js/            # UI JavaScript
│   │       └── templates/            # Thymeleaf HTML
│   └── test/
├── docker/
├── scripts/
│   └── init-db.sql
├── .env.example
├── .gitignore
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit with conventional commits: `git commit -m "feat: add streaming chat support"`
4. Push and open a PR

---

## 📜 License

Apache 2.0 — see [LICENSE](LICENSE).

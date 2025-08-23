""" Proximity Filtering & Collaboration Service

Stack: FastAPI, SQLAlchemy 2.0, Postgres + pgvector, Sentence-Transformers Security: JWT auth, rate limiting, CORS Performance: vector index (IVFFLAT), batching, caching hooks

How to run (dev):

1. Install Postgres 15+ and enable pgvector extension.


2. Create DB and set env vars (see .env section below).


3. pip install -r requirements.txt


4. uvicorn app:app --host 0.0.0.0 --port 8000 --workers 2



Production: use Gunicorn + Uvicorn workers and a reverse proxy (e.g., Nginx).


---

SQL bootstrap (run once in your Postgres database)

CREATE EXTENSION IF NOT EXISTS vector;

-- Developers table (hybrid-friendly schema) CREATE TABLE IF NOT EXISTS developers ( id UUID PRIMARY KEY, name TEXT NOT NULL, bio TEXT, skills TEXT[] NOT NULL DEFAULT '{}', interests TEXT[] NOT NULL DEFAULT '{}', location TEXT, years_exp INT DEFAULT 0, rating DOUBLE PRECISION DEFAULT 0.0,  -- 0..5 hourly_rate INT,                      -- in your currency units availability TEXT,                    -- e.g., 'full-time','part-time','weekends' embedding vector(384)                 -- all-MiniLM-L6-v2 outputs 384 dims );

-- For exact/AND skill filters CREATE INDEX IF NOT EXISTS idx_developers_skills ON developers USING GIN (skills); -- For location filter (if string equality). For geospatial, keep this service agnostic and feed candidates. CREATE INDEX IF NOT EXISTS idx_developers_location ON developers (location); -- For vector search. Use IVFFLAT for large data; requires ANALYZE after creation. CREATE INDEX IF NOT EXISTS idx_developers_embedding_ivfflat ON developers USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100); ANALYZE developers;

-- Optional: materialized view for hot cohorts you frequently query (not shown here).


---

.env (example)

DATABASE_URL=postgresql+psycopg://user:password@localhost:5432/devmatch 
JWT_SECRET=replace_me_with_long_random 
EMBEDDING_MODEL=sentence-transformers/all-MiniLM-L6-v2 
CORS_ORIGINS=http://localhost:3000,https://your-frontend.app 
MAX_CANDIDATES=200 
TOP_K=20 RATE_LIMIT=60/minute


---

# Notes on Security & Reliability

# - Use a proper identity provider for tokens, rotate JWT secret, enforce HTTPS.

# - Add structured logging, tracing (OTel), and metrics (Prometheus).

# - Consider Redis caching for hot queries; cache key can include hash of request filters + geohash bucket.

# - Protect vector endpoints with quotas and abuse detection.

# - Back-pressure: cap MAX_CANDIDATES and TOP_K per tenant.

# - Database: tune ivfflat lists and probes: SET ivfflat.probes = 10; per session if needed.

# - Create read replicas for heavy read traffic; this microservice can be read-heavy.
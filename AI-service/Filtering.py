import os 
import uuid 
import json 
import math 
from datetime import datetime, timedelta 
from typing import List, Optional, Annotated
from fastapi import FastAPI, Depends, HTTPException, status, Request 
from fastapi.responses import JSONResponse 
from fastapi.middleware.cors import CORSMiddleware 
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, text 
from sqlalchemy.engine import Engine 
from sqlalchemy.pool import QueuePool 
from jose import jwt, JWTError 
from slowapi import Limiter 
from slowapi.util import get_remote_address 
from slowapi.errors import RateLimitExceeded 
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer 
import numpy as np
from fastapi import Header
import uvicorn

# Environment & Globals
load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql+psycopg://user:password@localhost:5432/devmatch")
JWT_SECRET = os.getenv("JWT_SECRET", "dev_secret_change_me")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "sentence-transformers/all-MiniLM-L6-v2")
CORS_ORIGINS = [o.strip() for o in os.getenv("CORS_ORIGINS", "*").split(",")]
MAX_CANDIDATES = int(os.getenv("MAX_CANDIDATES", "200"))
TOP_K = int(os.getenv("TOP_K", "20"))
RATE_LIMIT = os.getenv("RATE_LIMIT", "60/minute")

# App & Middleware
limiter = Limiter(key_func=get_remote_address, default_limits=[RATE_LIMIT])
app = FastAPI(title="Proximity Filtering & Collaboration Service", version="1.0.0")
app.state.limiter = limiter
app.add_middleware( CORSMiddleware, allow_origins=[""] if CORS_ORIGINS == [""] else CORS_ORIGINS, allow_credentials=True, allow_methods=[""], allow_headers=[""], )

@app.exception_handler(RateLimitExceeded)
async def rate_limit_handler(request: Request, exc: RateLimitExceeded):
    return JSONResponse(
        status_code=429,
        content={"detail": "Rate limit exceeded. Try again later."},
    )


# DB Engine (pooled)
engine: Engine = create_engine(
    DATABASE_URL,
    poolclass=QueuePool,
    pool_size=10,
    max_overflow=20,
    pool_pre_ping=True,
    future=True,
)

# Auth (JWT tokens)

ALGO = "HS256"
class UserToken(BaseModel):
    sub: str
    exp: int

def create_token(user_id: str, minutes: int = 60) -> str:
    payload = {
        "sub": user_id,
        "exp": int((datetime.utcnow() + timedelta(minutes=minutes)).timestamp())
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=ALGO)

def require_auth(auth_header: Annotated[Optional[str], Depends(lambda authorization: authorization)]) -> str: 
    """Dependency that validates Authorization: Bearer <token> and returns user id"""
    async def _inner(authorization: Annotated[Optional[str], Header(None)]):
        if not authorization or not authorization.lower().startswith("bearer "):
            raise HTTPException(status_code=401, detail="Missing or invalid Authorization header")
        token = authorization.split()[1]
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[ALGO])
            return payload.get("sub")
        except JWTError:
            raise HTTPException(status_code=401, detail="Invalid or expired token")
    return _inner  # FastAPI quirk – return callable dependency


#Embedding model (load once)

print("Loading embedding model ...")
model = SentenceTransformer(EMBEDDING_MODEL)
print("Model loaded.")

#Schemas

class DevUpsert(BaseModel):
    id: Optional[str] = Field(default_factory=lambda: str(uuid.uuid4()))
    name: str
    bio: str
    skills: List[str]
    interests: List[str] = []
    location: Optional[str] = None
    years_exp: Optional[int] = 0
    rating: Optional[float] = 0.0
    hourly_rate: Optional[int] = None
    availability: Optional[str] = None

class FindReq(BaseModel):
    project_description: str
    required_skills: List[str] = []
    location: Optional[str] = None
    min_rating: Optional[float] = None
    max_hourly_rate: Optional[int] = None
    availability: Optional[str] = None  # e.g., 'weekends'
    top_k: Optional[int] = None


#Helpers


def _embed_profile(skills: List[str], interests: List[str], bio: str) -> np.ndarray:
    skills_text = " ".join(skills)
    interests_text = " ".join(interests)
    # Weighted concatenation to bias skills
    weighted_text = f"SKILLS:{skills_text} BIO:{bio} INTERESTS:{interests_text}"
    vec = model.encode(weighted_text, normalize_embeddings=True)
    return np.asarray(vec, dtype=np.float32)

def _skill_overlap(required: List[str], user_skills: List[str]) -> float:
    if not required:
        return 0.0
    r = {s.strip().lower() for s in required}
    u = {s.strip().lower() for s in user_skills}
    return len(r & u) / len(r)

def _price_score(max_rate: Optional[int], user_rate: Optional[int]) -> float:
    if max_rate is None or user_rate is None:
        return 0.0
    # Simple: 1.0 if within budget, else 0, can be made smoother
    return 1.0 if user_rate <= max_rate else 0.0

def _availability_score(requested: Optional[str], user_avail: Optional[str]) -> float:
    if not requested or not user_avail:
        return 0.0
    return 1.0 if requested.strip().lower() in user_avail.strip().lower() else 0.0


# Health & Token

@app.get("/health")
@limiter.limit("30/minute")
async def health():
    with engine.connect() as conn:
        conn.execute(text("SELECT 1"))
    return {"status": "ok"}

@app.post("/token")
@limiter.limit("10/minute")
async def token_stub():
    # In production, validate credentials; here we return a token for demo user
    return {"access_token": create_token("demo-user"), "token_type": "bearer"}


# Upsert developer (stores embedding in pgvector)

@app.post("/developers/upsert")
@limiter.limit("30/minute")
async def upsert_developer(body: DevUpsert, user_id: str = Depends(require_auth(None))):
    emb = _embed_profile(body.skills, body.interests, body.bio)

    sql = text(
        """
        INSERT INTO developers (id, name, bio, skills, interests, location, years_exp, rating, hourly_rate, availability, embedding)
        VALUES (:id, :name, :bio, :skills, :interests, :location, :years_exp, :rating, :hourly_rate, :availability, :embedding)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            bio = EXCLUDED.bio,
            skills = EXCLUDED.skills,
            interests = EXCLUDED.interests,
            location = EXCLUDED.location,
            years_exp = EXCLUDED.years_exp,
            rating = EXCLUDED.rating,
            hourly_rate = EXCLUDED.hourly_rate,
            availability = EXCLUDED.availability,
            embedding = EXCLUDED.embedding
        """
    )

    with engine.begin() as conn:
        conn.execute(sql, {
        "id": body.id,
        "name": body.name,
        "bio": body.bio,
        "skills": body.skills,
        "interests": body.interests,
        "location": body.location,
        "years_exp": body.years_exp,
        "rating": body.rating,
        "hourly_rate": body.hourly_rate,
        "availability": body.availability,
        "embedding": emb.tolist(),
        })

    return {"status": "ok", "id": body.id}


# Find collaborators (hybrid: rule filters + vector search + re-rank)


@app.post("/find_collaborators")
@limiter.limit("120/minute")
async def find_collaborators(req: FindReq, user_id: str = Depends(require_auth(None))):
    if not req.project_description and not req.required_skills:
        raise HTTPException(400, detail="Provide project_description or required_skills")

    # 1) Batching: single embedding for query (normalize for cosine)
    query_text = f"{req.project_description}. REQUIRED:{' '.join(req.required_skills)}"
    qvec = model.encode(query_text, normalize_embeddings=True).astype(np.float32)

    # 2) Rule filters applied in SQL WHERE (pre-filter to reduce candidate set)
    where_clauses = []
    params = {"qvec": qvec.tolist(), "limit": min(MAX_CANDIDATES, 1000)}

    if req.location:
        where_clauses.append("location = :location")
        params["location"] = req.location

    if req.min_rating is not None:
        where_clauses.append("rating >= :min_rating")
        params["min_rating"] = req.min_rating

    if req.max_hourly_rate is not None:
        where_clauses.append("hourly_rate <= :max_rate")
        params["max_rate"] = req.max_hourly_rate

    if req.availability:
        where_clauses.append("availability ILIKE :availability")
        params["availability"] = f"%{req.availability}%"

# Optional strict skill filter: require at least one overlap (GIN index helps)
    if req.required_skills:
        where_clauses.append("skills && :req_skills")  # array overlaps
        params["req_skills"] = req.required_skills

    where_sql = ("WHERE " + " AND ".join(where_clauses)) if where_clauses else ""

    # 3) Candidate retrieval with pgvector cosine distance (smaller is closer)
    #    Use IVFFLAT index when limit is small vs total data.
    sql = text(f"""
        SELECT id, name, bio, skills, interests, location, years_exp, rating, hourly_rate, availability,
            1 - (embedding <=> :qvec) AS cosine_score
        FROM developers
        {where_sql}
        ORDER BY embedding <=> :qvec ASC
        LIMIT :limit
    """)

    rows = []
    with engine.connect() as conn:
        res = conn.execute(sql, params)
        rows = [dict(r._mapping) for r in res.fetchall()]

    if not rows:
        return {"status": "success", "matches": []}

    # 4) Re-rank with hybrid scoring
    matches = []
    for r in rows:
        overlap = _skill_overlap(req.required_skills, r["skills"]) if req.required_skills else 0.0
        price_s = _price_score(req.max_hourly_rate, r.get("hourly_rate"))
        avail_s = _availability_score(req.availability, r.get("availability"))
        rating_norm = max(0.0, min(1.0, (r.get("rating") or 0.0) / 5.0))

    # Final score weights – tune with offline A/B testing
    final = (
        0.60 * float(r["cosine_score"]) +
        0.25 * overlap +
        0.08 * rating_norm +
        0.04 * price_s +
        0.03 * avail_s
    )

    matches.append({
        "id": r["id"],
        "name": r["name"],
        "skills": r["skills"],
        "location": r["location"],
        "years_exp": r["years_exp"],
        "rating": r["rating"],
        "hourly_rate": r["hourly_rate"],
        "availability": r["availability"],
        "cosine_score": round(float(r["cosine_score"]), 4),
        "overlap_score": round(overlap, 4),
        "final_score": round(final, 4)
    })

    matches.sort(key=lambda x: x["final_score"], reverse=True)
    k = req.top_k or TOP_K
    return {"status": "success", "matches": matches[:k]}


# Optional: Team recommendation (cover required skills with smallest team)

class TeamReq(FindReq): max_team_size: int = 3

@app.post("/recommend_team")
@limiter.limit("60/minute")
async def recommend_team(req: TeamReq, user_id: str = Depends(require_auth(None))):
    # Reuse candidate retrieval, but take more candidates for set cover
    base_req = FindReq(**req.model_dump())
    resp = await find_collaborators(base_req, user_id)  # type: ignore
    candidates: List[dict] = resp["matches"]

    required = set(s.lower() for s in req.required_skills)
    team: List[dict] = []
    covered: set = set()

    for c in candidates:
        if len(team) >= req.max_team_size:
            break
        new_skills = set(s.lower() for s in c["skills"]) - covered
        gain = len(required & new_skills)
        if gain > 0:
            team.append(c)
            covered |= (required & set(s.lower() for s in c["skills"]))
        if covered >= required:
            break

    return {
        "status": "success",
        "team": team,
        "skills_covered": sorted(list(covered)),
        "skills_missing": sorted(list(required - covered))
    }




if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
import os
from sqlalchemy import text

from app.config import settings
from app.database import engine, Base
from app.routes import auth, citizen, supervisor, admin, common

# Automatically create tables in MySQL on startup
try:
    Base.metadata.create_all(bind=engine)
    print("Database tables initialized successfully (or already exist).")
    
    # Check/add columns dynamically for MySQL migrations
    with engine.begin() as conn:
        try:
            conn.execute(text("ALTER TABLE users ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1"))
            print("Successfully added is_active column to users table.")
        except Exception:
            pass
        
        try:
            conn.execute(text("ALTER TABLE supervisors ADD COLUMN latitude DECIMAL(10, 8) NULL"))
            print("Successfully added latitude column to supervisors table.")
        except Exception:
            pass

        try:
            conn.execute(text("ALTER TABLE supervisors ADD COLUMN longitude DECIMAL(11, 8) NULL"))
            print("Successfully added longitude column to supervisors table.")
        except Exception:
            pass
except Exception as e:
    print(f"Warning: Could not create tables on start. Make sure database '{settings.DB_NAME}' exists. Error: {e}")

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Python FastAPI backend for Smart Waste Management App",
    version="1.0.0"
)

# CORS Middleware for client requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Serves uploaded media statically
uploads_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "uploads")
os.makedirs(uploads_path, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=uploads_path), name="uploads")

# Register Routes
app.include_router(auth.router, prefix="/api")
app.include_router(citizen.router, prefix="/api")
app.include_router(supervisor.router, prefix="/api")
app.include_router(admin.router, prefix="/api")
app.include_router(common.router, prefix="/api")

@app.get("/")
def read_root():
    return {
        "status": "online",
        "project": settings.PROJECT_NAME,
        "docs_url": "/docs",
        "redoc_url": "/redoc"
    }

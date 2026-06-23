from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.config import settings

# Create database engine
# For MySQL with PyMySQL
engine = create_engine(
    settings.DATABASE_URL,
    pool_pre_ping=True,  # Checks connection liveness before queries
    pool_recycle=3600    # Recycles connections to avoid MySQL server closed connections
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# Dependency to yield DB session and close it afterwards
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

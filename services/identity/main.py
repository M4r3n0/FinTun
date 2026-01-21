from fastapi import FastAPI
from contextlib import asynccontextmanager
import structlog
from .database import engine, Base
from .routers import auth

logger = structlog.get_logger()

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Identity Service Starting...")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    logger.info("Identity Service Shutting Down...")

app = FastAPI(title="TunFin Identity Service", version="0.1.0", lifespan=lifespan)

from .routers import auth, kyc
app.include_router(auth.router)
app.include_router(kyc.router)

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "identity"}

@app.get("/")
async def root():
    return {"message": "Welcome to TunFin Identity Service"}

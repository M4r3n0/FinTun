from fastapi import FastAPI
from contextlib import asynccontextmanager
import structlog

logger = structlog.get_logger()

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Payment Service Starting...")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    logger.info("Payment Service Shutting Down...")

app = FastAPI(title="TunFin Payment Service", version="0.1.0", lifespan=lifespan)

from .database import engine, Base
from .routers import p2p, qr

app.include_router(p2p.router)
app.include_router(qr.router)

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "payment"}

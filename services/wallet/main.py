from fastapi import FastAPI
from contextlib import asynccontextmanager
import structlog

logger = structlog.get_logger()

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Wallet Service Starting...")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    logger.info("Wallet Service Shutting Down...")

app = FastAPI(title="TunFin Wallet Service", version="0.1.0", lifespan=lifespan)

from .database import engine, Base
from .routers import accounts, ledger

app.include_router(accounts.router)
app.include_router(ledger.router)

@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "wallet"}

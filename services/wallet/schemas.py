from pydantic import BaseModel
from typing import Optional, List
from uuid import UUID
from datetime import datetime
from decimal import Decimal

class AccountCreate(BaseModel):
    user_id: UUID
    currency: str = "TND"

class AccountResponse(BaseModel):
    id: UUID
    user_id: UUID
    currency: str
    balance: Decimal
    status: str
    created_at: datetime

    class Config:
        from_attributes = True

class TransactionRequest(BaseModel):
    reference_id: str
    type: str # P2P, DEPOSIT
    description: Optional[str]
    entries: List["LedgerEntryRequest"]

class LedgerEntryRequest(BaseModel):
    account_id: UUID
    amount: Decimal # Signed amount

class TransactionResponse(BaseModel):
    id: UUID
    reference_id: str
    status: str
    created_at: datetime

    class Config:
        from_attributes = True

from pydantic import BaseModel
from typing import Optional
from uuid import UUID
from decimal import Decimal
from datetime import datetime

class TransferRequest(BaseModel):
    recipient_id: UUID
    amount: Decimal
    description: Optional[str] = "P2P Transfer"

class QRGenerateRequest(BaseModel):
    amount: Optional[Decimal] = None
    description: Optional[str] = "QR Payment"

class QRScanRequest(BaseModel):
    qr_payload: str
    pass

class PaymentResponse(BaseModel):
    id: UUID
    status: str
    amount: Decimal
    created_at: datetime
    
    class Config:
        from_attributes = True

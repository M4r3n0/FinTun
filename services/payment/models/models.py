from sqlalchemy import Column, String, Numeric, Enum, DateTime, UUID, ForeignKey
from sqlalchemy.sql import func
import uuid
from ..database import Base

class PaymentType(str, Enum):
    P2P = "P2P"
    QR = "QR"

class PaymentStatus(str, Enum):
    PENDING = "PENDING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class Payment(Base):
    __tablename__ = "payments"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    sender_id = Column(UUID(as_uuid=True), index=True, nullable=False)
    recipient_id = Column(UUID(as_uuid=True), index=True, nullable=True) # Nullable for QR pending scan
    amount = Column(Numeric(18, 3), nullable=False)
    currency = Column(String, default="TND")
    description = Column(String, nullable=True)
    status = Column(String, default="PENDING")
    
    # QR Specifics
    qr_code_id = Column(String, unique=True, nullable=True) 
    
    created_at = Column(DateTime(timezone=True), server_default=func.now())

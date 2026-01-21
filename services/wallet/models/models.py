from sqlalchemy import Column, String, Numeric, Enum, DateTime, UUID, ForeignKey
from sqlalchemy.sql import func
import uuid
import enum
from ..database import Base

class AccountType(str, enum.Enum):
    ASSET = "ASSET"         # User Wallets
    LIABILITY = "LIABILITY" # User Deposits (from Bank perspective) - For this system, User Wallet is Liability to System? 
                            # Let's stick to simple: User Wallet = ASSET for User. 
                            # But in System Ledger:
                            # User Account (Liability of Platform to User)
                            # Bank Settlement Account (Asset of Platform)
    REVENUE = "REVENUE"     # Fees
    EXPENSE = "EXPENSE"

class AccountStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    FROZEN = "FROZEN"

class Account(Base):
    __tablename__ = "accounts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), nullable=False, index=True) # ID from Identity Service
    currency = Column(String, default="TND")
    type = Column(String, default="LIABILITY") # Default is a User Wallet (Liability to TunFin)
    balance = Column(Numeric(18, 3), default=0) # Denormalized balance for Validations, 3 decimals for TND
    status = Column(String, default="ACTIVE")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class TransactionStatus(str, enum.Enum):
    PENDING = "PENDING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    reference_id = Column(String, unique=True, index=True, nullable=False) # External Ref
    type = Column(String, nullable=False) # P2P, DEPOSIT, WITHDRAWAL, PAYMENT
    status = Column(String, default="PENDING")
    description = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class LedgerEntry(Base):
    __tablename__ = "ledger_entries"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    transaction_id = Column(UUID(as_uuid=True), ForeignKey("transactions.id"), nullable=False)
    account_id = Column(UUID(as_uuid=True), ForeignKey("accounts.id"), nullable=False)
    amount = Column(Numeric(18, 3), nullable=False) # Positive = Debit, Negative = Credit (or vice versa, typically Debit +, Credit - in DB summing)
                                                    # Let's use: Credit (Add to Liability/User Balance) = Positive? 
                                                    # Convention: 
                                                    # Debit (+): Increase Asset, Decrease Liability
                                                    # Credit (-): Decrease Asset, Increase Liability
                                                    # User Wallet is Liability. So Credit (-) increases balance?
                                                    # Let's simplify for this specific DB: 
                                                    # Amount is Signed. 
                                                    # We will validate Sum(Amounts) == 0 per Transaction.
    created_at = Column(DateTime(timezone=True), server_default=func.now())

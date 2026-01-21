from pydantic import BaseModel
from typing import Optional
from uuid import UUID
from datetime import datetime

class UserBase(BaseModel):
    phone_number: str

class UserCreate(UserBase):
    password: str
    national_id: Optional[str] = None
    full_name: Optional[str] = None

class UserResponse(UserBase):
    id: UUID
    national_id: Optional[str]
    full_name: Optional[str]
    kyc_level: str
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    phone_number: Optional[str] = None

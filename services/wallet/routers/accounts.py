from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from ..database import get_db
from ..models.models import Account
from ..schemas import AccountCreate, AccountResponse

router = APIRouter(prefix="/accounts", tags=["Accounts"])

@router.post("/", response_model=AccountResponse)
async def create_account(account: AccountCreate, db: AsyncSession = Depends(get_db)):
    # Check if user already has an account for currency
    result = await db.execute(select(Account).where(
        Account.user_id == account.user_id,
        Account.currency == account.currency
    ))
    if result.scalars().first():
        raise HTTPException(status_code=400, detail="Account already exists for this currency")

    new_account = Account(
        user_id=account.user_id,
        currency=account.currency,
        balance=0
    )
    db.add(new_account)
    await db.commit()
    await db.refresh(new_account)
    return new_account

@router.get("/{user_id}", response_model=AccountResponse)
async def get_balance(user_id: str, db: AsyncSession = Depends(get_db)):
    # Simplified lookup by User ID (assuming 1 account per user for now)
    result = await db.execute(select(Account).where(Account.user_id == user_id))
    account = result.scalars().first()
    if not account:
        raise HTTPException(status_code=404, detail="Account not found")
    return account

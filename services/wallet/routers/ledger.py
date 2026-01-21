from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from ..database import get_db
from ..models.models import Transaction, LedgerEntry, Account
from ..schemas import TransactionRequest, TransactionResponse
from sqlalchemy import select

router = APIRouter(prefix="/ledger", tags=["Ledger"])

@router.post("/transaction", response_model=TransactionResponse)
async def record_transaction(tx_req: TransactionRequest, db: AsyncSession = Depends(get_db)):
    # 1. Validate Double Entry (Sum == 0)
    total_amount = sum(entry.amount for entry in tx_req.entries)
    if total_amount != 0:
        raise HTTPException(status_code=400, detail=f"Ledger imbalance: Sum is {total_amount}, must be 0")

    # 2. Start Transaction
    new_tx = Transaction(
        reference_id=tx_req.reference_id,
        type=tx_req.type,
        description=tx_req.description,
        status="COMPLETED"
    )
    db.add(new_tx)
    await db.flush() # Get ID

    # 3. Process Entries and Update Balances
    for entry in tx_req.entries:
        # Get Account (Locking for Update would be better in prod using with_for_update)
        result = await db.execute(select(Account).where(Account.id == entry.account_id))
        account = result.scalars().first()
        if not account:
            raise HTTPException(status_code=404, detail=f"Account {entry.account_id} not found")
        
        # NOTE: Convention review.
        # Liability Account (User Wallet):
        # Credit (-) increases Liability (Balance goes UP).
        # Debit (+) decreases Liability (Balance goes DOWN).
        #
        # If we use Signed Amounts directly:
        # If User A sends money, their Liability decreases (Debit +). Balance should go DOWN.
        # But wait...
        # Standard Accounting:
        # Asset: Debit (+), Credit (-)
        # Liability: Debit (-), Credit (+)
        #
        # Let's define: Balance = -Sum(LedgerEntries). 
        # So if I have a Credit of -100, Balance becomes -(-100) = +100.
        # If I have a Debit of +50, Balance becomes 100 - 50 = 50.
        #
        # For simplicity in this demo, let's reverse the sign for Balance update if it is a Liability account.
        
        if account.type == "LIABILITY":
            # Credit (-100) means we owe user more money. Balance += 100.
            # Debit (+100) means we owe user less. Balance -= 100.
            # So: Balance -= Amount
            account.balance -= entry.amount
        else:
            # Asset Account: Debit (+) increases balance.
            account.balance += entry.amount
            
        # Check overdraft (only for Liability accounts usually, or if Asset goes negative)
        if account.balance < 0 and account.type == "LIABILITY":
             raise HTTPException(status_code=400, detail="Insufficient funds")

        new_entry = LedgerEntry(
            transaction_id=new_tx.id,
            account_id=entry.account_id,
            amount=entry.amount
        )
        db.add(new_entry)

    await db.commit()
    await db.refresh(new_tx)
    return new_tx

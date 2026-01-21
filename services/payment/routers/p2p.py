from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from ..database import get_db
from ..models.models import Payment
from ..schemas import TransferRequest, PaymentResponse
from ..clients.wallet_client import WalletClient
from uuid import UUID

router = APIRouter(prefix="/p2p", tags=["P2P Payments"])
wallet_client = WalletClient() # In prod, inject via dependency

@router.post("/transfer", response_model=PaymentResponse)
async def p2p_transfer(
    req: TransferRequest, 
    sender_id: UUID, # In prod, extract from JWT (Depends(get_current_user))
    db: AsyncSession = Depends(get_db)
):
    # 1. Verify Sender has Account (via Wallet Service)
    sender_acct = await wallet_client.get_account(sender_id)
    if not sender_acct:
        raise HTTPException(status_code=400, detail="Sender wallet not found")
        
    recipient_acct = await wallet_client.get_account(req.recipient_id)
    if not recipient_acct:
        raise HTTPException(status_code=400, detail="Recipient wallet not found")

    # 2. Check Balance (Optional, Wallet Service does this too, but good for fail-fast)
    # Skipped for now, relying on Wallet Service atomic tx.

    # 3. Create Pending Payment Record
    payment = Payment(
        sender_id=sender_id,
        recipient_id=req.recipient_id,
        amount=req.amount,
        description=req.description
    )
    db.add(payment)
    await db.commit()
    await db.refresh(payment)

    # 4. Execute Ledger Transaction (Debit Sender, Credit Recipient)
    # NOTE: In a real system, this should be an async Message Queue event to ensure eventual consistency
    # or a 2PC. Here we do direct HTTP call.
    
    entries = [
        {"account_id": sender_acct["id"], "amount": float(-req.amount)},  # Sender Looses Money? Wait. 
                                                                         # Liability: Credit (-) increases balance. Debit (+) decreases.
                                                                         # If Sender sends money, their balance decreases.
                                                                         # So we DEBIT the Liability Account.
                                                                         # Wait, my logic in Wallet Service was:
                                                                         # Liability: Balance -= Amount.
                                                                         # So if I send +100 (Debit), Balance -= 100. Correct.
                                                                         # So entry should be Positive amount for Debit?
                                                                         # Let's check Wallet Service logic again.
                                                                         # if account.type == "LIABILITY": account.balance -= entry.amount
                                                                         # So to decrease balance, entry.amount must be POSITIVE.
        {"account_id": recipient_acct["id"], "amount": float(req.amount)} # To increase balance, entry.amount must be NEGATIVE.
    ]
    # WAIT! Sum must be 0.
    # +Amount and +Amount != 0.
    # One must be negative.
    # Logic:
    # Sender (Liability): Debit (+100). Balance -= 100.
    # Recipient (Liability): Credit (-100). Balance -= -100 (Balance += 100).
    # Sum: +100 + (-100) = 0. Perfect.
    
    entries = [
        {"account_id": sender_acct["id"], "amount": float(req.amount)},   # Debit Sender (+)
        {"account_id": recipient_acct["id"], "amount": float(-req.amount)} # Credit Recipient (-)
    ]

    success = await wallet_client.execute_ledger_transaction(
        reference_id=str(payment.id),
        type="P2P",
        description=req.description,
        entries=entries
    )

    if success:
        payment.status = "COMPLETED"
    else:
        payment.status = "FAILED"
        # In prod, we might refund or retry.
    
    await db.commit()
    return payment

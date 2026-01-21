from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.ext.asyncio import AsyncSession
from ..database import get_db
from ..models.models import KYCDocument, User
from uuid import UUID
import shutil
import os

router = APIRouter(prefix="/kyc", tags=["KYC"])
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/upload")
async def upload_kyc(
    user_id: UUID = Form(...),
    document_type: str = Form(...),
    file: UploadFile = File(...),
    db: AsyncSession = Depends(get_db)
):
    # Retrieve user
    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Save file
    file_path = f"{UPLOAD_DIR}/{user_id}_{document_type}_{file.filename}"
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    # Save metadata
    doc = KYCDocument(
        user_id=user_id,
        document_type=document_type,
        file_path=file_path,
        status="PENDING"
    )
    db.add(doc)
    
    # Update user KYC status
    user.kyc_level = "PENDING_VERIFICATION"
    
    await db.commit()
    return {"message": "Document uploaded successfully", "status": "PENDING"}

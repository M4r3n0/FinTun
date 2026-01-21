from fastapi import APIRouter, Depends, HTTPException
from ..schemas import QRGenerateRequest, QRScanRequest
import uuid
import json
import base64

router = APIRouter(prefix="/qr", tags=["QR Payments"])

@router.post("/generate")
async def generate_qr(req: QRGenerateRequest):
    # In a real system, we would sign this payload.
    # Payload format: TUNFIN:MERCHANT_ID:AMOUNT:REF
    
    # Simulating a merchant ID (random for now, or fixed)
    merchant_id = str(uuid.uuid4()) 
    
    payload_data = {
        "merchant_id": merchant_id,
        "amount": float(req.amount) if req.amount else None,
        "description": req.description,
        "ts": str(uuid.uuid1()) # Timestamp/Nonce
    }
    
    # Encode to Base64 to simulate a scannable string
    payload_str = json.dumps(payload_data)
    encoded_payload = base64.b64encode(payload_str.encode()).decode()
    
    return {
        "qr_payload": f"TUNFIN:{encoded_payload}",
        "data": payload_data
    }

@router.post("/scan")
async def scan_qr(req: QRScanRequest):
    try:
        if not req.qr_payload.startswith("TUNFIN:"):
            raise ValueError("Invalid QR Format")
            
        encoded = req.qr_payload.split(":")[1]
        decoded_bytes = base64.b64decode(encoded)
        data = json.loads(decoded_bytes.decode())
        
        return {
            "valid": True,
            "merchant_id": data.get("merchant_id"),
            "amount": data.get("amount"),
            "description": data.get("description")
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail="Invalid QR Code")

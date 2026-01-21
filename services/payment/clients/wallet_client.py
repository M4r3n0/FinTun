import httpx
from uuid import UUID
from decimal import Decimal
from typing import List, Dict

class WalletClient:
    def __init__(self, base_url: str = "http://localhost:8002"):
        self.base_url = base_url

    async def get_account(self, user_id: UUID) -> Dict:
        async with httpx.AsyncClient() as client:
            resp = await client.get(f"{self.base_url}/accounts/{user_id}")
            if resp.status_code != 200:
                return None
            return resp.json()

    async def execute_ledger_transaction(
        self, 
        reference_id: str, 
        type: str, 
        description: str,
        entries: List[Dict]
    ) -> bool:
        payload = {
            "reference_id": reference_id,
            "type": type,
            "description": description,
            "entries": entries
        }
        async with httpx.AsyncClient() as client:
            resp = await client.post(f"{self.base_url}/ledger/transaction", json=payload)
            return resp.status_code == 200

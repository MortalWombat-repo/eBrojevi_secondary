from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
import duckdb

app = FastAPI()

DB_PATH = "db/enumbers_db.duckdb"

class ENumber(BaseModel):
    code: str
    name: str
    description: str
    type: str
    adi: str
    color: str

@app.get("/database", response_model=list[ENumber])
def get_database():
    try:
        with duckdb.connect(DB_PATH, read_only=True) as con:
            result = con.execute("SELECT * FROM enumbers").fetchall()
            if not result:
                raise HTTPException(status_code=404, detail="Database is empty")
            
            columns = [desc[0] for desc in con.description]
            database = [dict(zip(columns, row)) for row in result]
            return database
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.get("/database/{codes:path}", response_model=list[ENumber])
def get_items_by_codes(codes: str):
    try:
        code_list = [code.strip().upper() for code in codes.split("/") if code.strip()]
        if not code_list:
            raise HTTPException(status_code=400, detail="No valid codes provided")
        
        with duckdb.connect(DB_PATH, read_only=True) as con:
            placeholders = ','.join(['?'] * len(code_list))
            query = f"SELECT * FROM enumbers WHERE upper(code) IN ({placeholders})"
            result = con.execute(query, tuple(code_list)).fetchall()
            if not result:
                raise HTTPException(status_code=404, detail="Items not found")
            
            columns = [desc[0] for desc in con.description]
            items = [dict(zip(columns, row)) for row in result]
            return items
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")

@app.get("/test", response_model=list[ENumber])
def test_response():
    return [
        {
            "code": "E100",
            "name": "Test",
            "description": "Test description",
            "type": "Test type",
            "adi": "Test adi",
            "color": "Green"
        },
        {
            "code": "E101",
            "name": "Test 2",
            "description": "Another description",
            "type": "Test type",
            "adi": "Test adi",
            "color": "Yellow"
        }
    ]
    

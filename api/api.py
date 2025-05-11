from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import duckdb

app = FastAPI()

DB_PATH = "db/enumbers_db.duckdb"

@app.get("/database")
def get_database():
    try:
        with duckdb.connect(DB_PATH, read_only=True) as con:
            result = con.execute("SELECT * FROM enumbers").fetchall()
            columns = [desc[0] for desc in con.description]
            database = [dict(zip(columns, row)) for row in result]
            return {"database": database}
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})

@app.get("/database/{codes:path}")
def get_items_by_codes(codes: str):
    try:
        code_list = codes.split("/")  # Split the codes into a list
        with duckdb.connect(DB_PATH, read_only=True) as con:
            # Query for multiple codes using the IN clause
            query = f"SELECT * FROM enumbers WHERE code IN ({','.join(['?'] * len(code_list))})"
            result = con.execute(query, tuple(code_list)).fetchall()

            if not result:
                raise HTTPException(status_code=404, detail="Items not found")

            columns = [desc[0] for desc in con.description]
            items = [dict(zip(columns, row)) for row in result]
            return {"items": items}
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})
    

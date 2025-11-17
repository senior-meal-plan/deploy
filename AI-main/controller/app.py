import os, requests, asyncio
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi import Body
from dotenv import load_dotenv

from service.meal_analysis import meal_analysis
from service.daily_analysis import daily_analysis
from service.weekly_analysis import weekly_analysis



load_dotenv()

API_BASE_URL = os.getenv(
    "API_BASE_URL",
    "http://localhost:8080"
)
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins = ["*"],
    allow_credentials = True,
    allow_methods = ["*"],
    allow_headers = ["*"],
)


async def send_meal_analysis(meal_data: dict):
    try:
        loop = asyncio.get_running_loop()
        analysis_result = await loop.run_in_executor(
            None, lambda: meal_analysis(meal_data)
        )

        response = await loop.run_in_executor(
            None,
            lambda: requests.post(
                f"{API_BASE_URL}/api/v1/webhooks/meals/analysis-result",
                json = analysis_result,
                timeout = 10,
            )
        )
        
        if response.status_code != 200:
            raise HTTPException(status_code = response.status_code, detail = response.text or "no body")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

async def send_daily_analysis(daily_data: dict):
    try:
        loop = asyncio.get_running_loop()
        analysis_result = await loop.run_in_executor(
            None, lambda: daily_analysis(daily_data)
        )

        response = await loop.run_in_executor(
            None,
            lambda: requests.post(
                f"{API_BASE_URL}/api/v1/webhooks/daily/reports/daily-analysis-complete",
                json = analysis_result,
                timeout = 10,
            )
        )

        if response.status_code != 200:
            raise HTTPException(status_code = response.status_code, detail = response.text or "no body")

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
 
async def send_weekly_analysis(weekly_data: dict):
    try:
        loop = asyncio.get_running_loop()
        analysis_result = await loop.run_in_executor(
            None, lambda: weekly_analysis(weekly_data)
        )

        response = await loop.run_in_executor(
            None,
            lambda: requests.post(
                f"{API_BASE_URL}/api/v1/webhooks/weekly/analysis-complete",
                json = analysis_result,
                timeout = 10,
            )
        )

        if response.status_code != 200:
            raise HTTPException(status_code = response.status_code, detail = response.text or "no body")

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



@app.post("/meals/analyze-and-send")
async def analyze_meal_report(meal: dict = Body(...)):
    asyncio.create_task(send_meal_analysis(meal))
    return {"message": "1 meal is being processed asynchronously."}

@app.post("/daily/analyze-and-send")
async def analyze_daily_report(daily_list: list = Body(...)):
    tasks = []
    for daily in daily_list:
        task = asyncio.create_task(send_daily_analysis(daily))
        tasks.append(task)
    return {"message": f"{len(tasks)} daily reports are being processed asynchronously."}
    
@app.post("/weekly/analyze-and-send")
async def analyze_seekly_report(weekly_list: list = Body(...)):
    tasks = []
    for weekly in weekly_list:
        task = asyncio.create_task(send_weekly_analysis(weekly))
        tasks.append(task)
    return {"message": f"{len(tasks)} weekly reports are being processed asynchronously."}

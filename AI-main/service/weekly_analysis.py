# 받는 파일 형식
#{
#  "weeklyReportId": 9007199254740991,
#  "userDto": {
#    "userId": 9007199254740991,
#    "userName": "string",
#    "age": 1073741824,
#    "userHeight": 0,
#    "userWeight": 0,
#    "Gender": "string",
#    "topics": [
#      {
#        "topicId": 9007199254740991,
#        "topicType": "ALLERGEN",
#        "name": "string",
#        "description": "string",
#        "source": "string"
#      }
#    ]
#  },
#  "dailyReports": [
#    {
#      "reportId": 9007199254740991,
#      "userId": 9007199254740991,
#      "reportDate": "2025-11-13",
#      "status": "PENDING",
#      "totalKcal": 0,
#      "totalProtein": 0,
#      "totalCarbs": 0,
#      "totalFat": 0,
#      "totalCalcium": 0,
#      "summary": "string",
#      "severity": "INFO",
#      "summarizeScore": 0,
#      "basicScore": 0,
#      "macularDegenerationScore": 0,
#      "hypertensionScore": 0,
#      "myocardialInfarctionScore": 0,
#      "sarcopeniaScore": 0,
#      "hyperlipidemiaScore": 0,
#      "boneDiseaseScore": 0
#    }
#  ],
#  "meals": [
#    {
#      "mealId": 9007199254740991,
#      "mealDate": "2025-11-13",
#      "mealTime": "00:00:00",
#      "mealType": "string",
#      "totalKcal": 0,
#      "totalProtein": 0,
#      "totalCarbs": 0,
#      "totalFat": 0,
#      "totalCalcium": 0,
#      "foods": [
#        {
#          "name": "string",
#          "kcal": 0,
#          "protein": 0,
#          "carbs": 0,
#          "fat": 0,
#          "calcium": 0,
#          "servingSize": 0,
#          "saturatedFatPercentKcal": 0,
#          "unsaturatedFat": 0,
#          "dietaryFiber": 0,
#          "sodium": 0,
#          "addedSugarKcal": 0,
#          "processedMeatGram": 0,
#          "vitaminD_IU": 0,
#          "isVegetable": true,
#          "isFruit": true,
#          "isFried": true
#        }
#      ]
#    }
#  ]
#}


# --------------------
# 환경설정
# --------------------

import json, os, copy, re
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage

load_dotenv()

llm = ChatOpenAI(
    model="gpt-4o",
    temperature=0.2,
    model_kwargs={"response_format": {"type": "json_object"}}
)

body_format = {
  "UserId": 2,
  "weeklyReport": {
    "WeeklyReportId": 7,
    "summaryGoodPoint": "이걸잘했음",
    "summaryBadPoint": "이걸못했음",
    "summaryAiRecommend": "물을 더 많이 먹으세요",
    "severity": "SOSO"
  },
  "aiRecommendTopic": [
    "고지혈증"
  ],
  "aiRecommendRecipe": [
    1
  ]
}



# --------------------
# AI 피드백 함수
# weekly_scoresheet : 일주일간의 데일리 스코어를 평균 내어 0-60 / 60-80/ 80-100 구간으로 BAD / SOSO / GOOD 판단
# generate_weekly_feedback : 좋은점 나쁜점 건강 조언을 생성하는 함수
# --------------------

def weekly_scoresheet(weekly: dict):
    score_sum = 0
    num_reports = len(weekly["dailyReports"])
    for i in range(num_reports):
        score_sum += weekly["dailyReports"][i]["summarizeScore"]
    average = score_sum / num_reports

    state = ""
    if average >= 80:
        state = "GOOD"
    elif average >= 60:
        state = "SOSO"
    else:
        state = "BAD"

    return state

def generate_weekly_feedback(weekly: dict):

    daily_reports = weekly["dailyReports"]
    meals = weekly["meals"]

    daily_text = json.dumps(daily_reports, ensure_ascii=False, indent=2)
    meals_text = json.dumps(meals, ensure_ascii=False, indent=2)

    system_prompt = """
    너는 60~80대 어르신의 일주일 식단을 분석하는 전문 영양사 AI야.

    <반드시 지킬 출력 JSON 형식>
    {
      "summaryGoodPoint": "",
      "summaryBadPoint": "",
      "summaryAiRecommend": ""
    }

    --- 작성 규칙 ---
    1. 모든 문장은 한국어 존댓말.
    2. summaryGoodPoint (2~3문장):
       - 일주일간 잘한 식습관을 따뜻하게 설명해줘.
    3. summaryBadPoint (2~3문장):
       - 주의가 필요한 문제점이나 부족했던 부분을 부드럽게 지적해줘.
    4. summaryAiRecommend (3~5문장):
       - 다음 주에 실천할 수 있는 구체적이고 실용적인 행동 조언 작성.
       - 예: 채소 섭취 늘리기, 단백질 분배, 나트륨 줄이는 팁, 수분 섭취 습관, 야식 조절 등.
    5. JSON 외의 텍스트(설명·해설·마크다운 등) 절대 금지.
    """

    user_prompt = f"""
    아래는 일주일간의 dailyReports와 meals 전체 데이터야.

    [Daily Reports]
    {daily_text}

    [Meals]
    {meals_text}

    위 두 데이터를 모두 참고해서,
    좋은점, 나쁜점, AI 건강 조언을 생성해줘.
    """

    messages = [
        SystemMessage(system_prompt),
        HumanMessage(user_prompt)
    ]

    try:
        ai_response = llm.invoke(messages)
        parsed = json.loads(ai_response.content)

        return {
            "summaryGoodPoint": parsed.get("summaryGoodPoint", "").strip(),
            "summaryBadPoint": parsed.get("summaryBadPoint", "").strip(),
            "summaryAiRecommend": parsed.get("summaryAiRecommend", "").strip()
        }

    except Exception as e:
        print("AI 요약 생성 오류 발생:", e)
        # fallback
        return {
            "summaryGoodPoint": "일주일 동안 식사를 규칙적으로 챙겨 드신 점이 좋습니다.",
            "summaryBadPoint": "일부 식단에서 영양소 균형이 다소 고르지 않은 부분이 보였습니다.",
            "summaryAiRecommend": "다음 주에는 채소와 단백질 비율을 조금 더 챙겨 드시고, 짠 음식 섭취를 줄여보시면 좋겠습니다."
        }


# -------------------
# 추천 함수
# new_health_goals : 일주일 질환별 점수 평균을 내어 10점 밑인 질환 상태를 목표로 리턴
# load_recipes : recipes.txt 파일을 파싱
# get_user_allergies : 유저의 알러지를 확인
# extract_meal_food_names : 이번주 식단의 모든 음식 이름을 리스트로 추출
# recommand_recieps : 레시피 추천 함수(최대 20개)
#--------------------

def new_health_goals(weekly: dict):

    num_reports = len(weekly["dailyReports"])

    macular_degeneration_score_sum = 0
    hypertension_score_sum = 0
    myocardial_infarction_score_sum = 0
    sarcopenia_score_sum = 0
    hyperlipidemia_score_sum = 0
    bone_disease_score_sum = 0

    for i in range(num_reports):
        macular_degeneration_score_sum += weekly["dailyReports"][i]["macularDegenerationScore"]
        hypertension_score_sum += weekly["dailyReports"][i]["hypertensionScore"]
        myocardial_infarction_score_sum += weekly["dailyReports"][i]["myocardialInfarctionScore"]
        sarcopenia_score_sum += weekly["dailyReports"][i]["sarcopeniaScore"]
        hyperlipidemia_score_sum += weekly["dailyReports"][i]["hyperlipidemiaScore"]
        bone_disease_score_sum += weekly["dailyReports"][i]["boneDiseaseScore"]
    
    macular_degeneration_average = macular_degeneration_score_sum / num_reports
    hypertension_average = hypertension_score_sum / num_reports
    myocardial_infarction_average = myocardial_infarction_score_sum / num_reports
    sarcopenia_average = sarcopenia_score_sum / num_reports
    hyperlipidemia_average = hyperlipidemia_score_sum / num_reports
    bone_disease_average = bone_disease_score_sum / num_reports

    disease_list = [macular_degeneration_average, hypertension_average, myocardial_infarction_average, sarcopenia_average, hyperlipidemia_average, bone_disease_average]

    health_goals = []

    for i in range(len(disease_list)):
        if disease_list[i] <= 10:
            if i == 0:
                health_goals.append("황반변성")
            if i == 1:
                health_goals.append("고혈압")
            if i == 2:
                health_goals.append("심근경색")
            if i == 3:
                health_goals.append("근감소증")
            if i == 4:
                health_goals.append("고지혈증")
            if i == 5:
                health_goals.append("뼈 질환")

    return health_goals

def load_recipes():
    """
    반환 형식:
    [
        {
            "id": 1,
            "disease_tags": [...],
            "allergy_tags": [...],
            "name": "...",
            "ingredients": "...",
            "instructions": "..."
        },
        ...
    ]
    """
    recipes_path = os.path.join(os.path.dirname(__file__), "../recipes.txt")
    with open(recipes_path, "r", encoding="utf-8") as f:
        text = f.read()

    # 레시피별로 블록을 분리
    blocks = re.split(r"\n(?=00\d{2})", text.strip())
    parsed = []

    for block in blocks:
        lines = block.strip().split("\n")
        recipe_id = int(lines[0])

        disease_line = lines[1].replace("질환 태그:", "").strip()
        allergy_line = lines[2].replace("알러지 태그:", "").strip()

        name_line = lines[3].replace("레시피 이름:", "").strip()
        ingredients_line = lines[4].replace("재료:", "").strip()
        instructions_line = "\n".join(lines[5:]).replace("조리법:", "").strip()

        parsed.append({
            "id": recipe_id,
            "disease_tags": json.loads(disease_line),
            "allergy_tags": json.loads(allergy_line),
            "name": name_line,
            "ingredients": ingredients_line,
            "instructions": instructions_line,
        })
    return parsed

def get_user_allergies(weekly: dict):
    userDto = weekly["userDto"]
    allergies = []
    for t in userDto.get("topics", []):
        if t["topicType"] == "ALLERGEN":
            allergies.append(t["name"])
    return allergies

def extract_meal_food_names(weekly: dict):
    eaten = set()
    for meal in weekly["meals"]:
        for food in meal["foods"]:
            eaten.add(food["name"])
    return eaten

def recommend_recipes(weekly, health_goals):
    recipes = load_recipes()
    allergies = get_user_allergies(weekly)
    eaten_foods = extract_meal_food_names(weekly)

    recommended = []

    # --------------------------------------
    # Case A: 건강 목표가 하나라도 있을 때
    # --------------------------------------
    if health_goals:
        for r in recipes:
            # 1) 알러지 차단
            if any(a in allergies for a in r["allergy_tags"]):
                continue

            # 2) health_goals와 매칭되는 질환 레시피만 추천
            if not any(h in r["disease_tags"] for h in health_goals):
                continue

            # 3) 이번 주 먹은 음식과 겹치면 제외
            if any(food in r["ingredients"] for food in eaten_foods):
                continue

            recommended.append(r["id"])
            if len(recommended) >= 20:
                break

        return recommended

    # --------------------------------------
    # Case B: 건강 목표가 없음 → disease_tags == [] 레시피 추천
    # --------------------------------------
    for r in recipes:
        if any(a in allergies for a in r["allergy_tags"]):
            continue

        if len(r["disease_tags"]) != 0:
            continue

        if any(food in r["ingredients"] for food in eaten_foods):
            continue

        recommended.append(r["id"])
        if len(recommended) >= 20:
            break

    # 그래도 부족하면 disease_tag 있는 레시피도 완화해서 추천
    if len(recommended) < 20:
        for r in recipes:
            if any(a in allergies for a in r["allergy_tags"]):
                continue

            if any(food in r["ingredients"] for food in eaten_foods):
                continue

            if r["id"] not in recommended:
                recommended.append(r["id"])

            if len(recommended) >= 20:
                break

    return recommended


# --------------------
# 메인 함수
# --------------------

def weekly_analysis(weekly: dict):
    result = copy.deepcopy(body_format)

    result["UserId"] = weekly["userDto"]["userId"]
    result["weeklyReport"]["WeeklyReportId"] = weekly["weeklyReportId"]

    result["weeklyReport"]["severity"] = weekly_scoresheet(weekly)
    result["aiRecommendTopic"] = new_health_goals(weekly)

    feedback = generate_weekly_feedback(weekly)
    result["weeklyReport"]["summaryGoodPoint"] = feedback["summaryGoodPoint"]
    result["weeklyReport"]["summaryBadPoint"] = feedback["summaryBadPoint"]
    result["weeklyReport"]["summaryAiRecommend"] = feedback["summaryAiRecommend"]

    goals = []
    for i in weekly["userDto"]["topics"]:
        if i["topicType"] in ["HEALTH_GOAL", "DISEASE_HISTORY"]:
            goals.append(i["name"])

    health_goals = list(set(result["aiRecommendTopic"] + goals))

    result["aiRecommendRecipe"] = recommend_recipes(weekly, health_goals)

    return result


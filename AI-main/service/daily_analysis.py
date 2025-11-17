# 받는 데이터 형식:
#{
#  "reportId": 9007199254740991,
#  "whoAmIDto": {
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
#  "meals": [
#    {
#      "mealType": "BREAKFAST",
#      "mealTime": {
#        "hour": 1073741824,
#        "minute": 1073741824,
#        "second": 1073741824,
#        "nano": 1073741824
#      },
#      "photoUrl": "string",
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
#  ],
#  "callbackUrl": "string"
#}

# topictype: "ALLERGEN""HEALTH_GOAL""DISEASE_HISTORY"



# --------------------
# 환경설정
# --------------------

import os, json, copy
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage



# --------------------
# LLM 설정 및 return body format 세팅
# --------------------

load_dotenv()

os.environ["OPENAI_API_KEY"] = os.getenv("OPENAI_API_KEY")
llm = ChatOpenAI(
    model="gpt-4o",
    temperature=0.0,
    model_kwargs={"response_format": {"type": "json_object"}}
)

body_format = {
  "reportId": 0,
  "status": "string",
  "errorMessage": "string",
  "totalKcal": 0,
  "totalProtein": 0,
  "totalCarbs": 0,
  "totalFat": 0,
  "totalCalcium": 0,
  "summary": "string",
  "severity": "string",
  "summarizeScore": 0,
  "basicScore": 0,
  "macularDegenerationScore": 0,
  "hypertensionScore": 0,
  "myocardialInfarctionScore": 0,
  "sarcopeniaScore": 0,
  "hyperlipidemiaScore": 0,
  "boneDiseaseScore": 0
}



# --------------------
# 건강 점수 계산 함수
# basic_score: 기본 점수 계산
# mucular_score: 황반변성 점수 계산
# hypertension_score: 고혈압 점수 계산
# myocardial_score: 심근경색 점수 계산
# sarcopenia_score: 근감소증 점수 계산
# hyperlipidemia_score: 고지혈증 점수 계산
# bone_score: 뼈 질환 점수 계산
# --------------------

def basic_score(daily: dict):

    # -----------------------------
    # 1) 하루 전체 영양소 합산
    # -----------------------------
    total_kcal = 0
    total_protein = 0
    total_carbs = 0
    total_fat = 0
    total_fiber = 0
    total_sodium = 0
    total_saturated_fat_percent = 0
    total_processed_meat_gram = 0
    total_added_sugar_kcal = 0

    fried_count = 0
    vegetable_serv = 0
    fruit_serv = 0

    # 끼니별 계산을 위해 저장
    meal_macro_list = []

    for meal in daily["meals"]:
        meal_kcal = 0
        meal_p = 0
        meal_c = 0
        meal_f = 0

        for food in meal["foods"]:
            kcal = food["kcal"]
            p = food["protein"]
            c = food["carbs"]
            f = food["fat"]

            total_kcal += kcal
            total_protein += p
            total_carbs += c
            total_fat += f

            meal_kcal += kcal
            meal_p += p
            meal_c += c
            meal_f += f

            total_fiber += food["dietaryFiber"]
            total_sodium += food["sodium"]
            total_added_sugar_kcal += food["addedSugarKcal"]
            total_processed_meat_gram += food["processedMeatGram"]

            if food["isFried"]:
                fried_count += 1
            if food["isVegetable"]:
                vegetable_serv += 1
            if food["isFruit"]:
                fruit_serv += 1

        meal_macro_list.append((meal_kcal, meal_p, meal_c, meal_f))

    # kcal 0 예외처리
    if total_kcal == 0:
        return 0

    # 총 섭취량 대비 퍼센트(kcal 기준)
    C_ratio = (total_carbs * 4) / total_kcal
    P_ratio = (total_protein * 4) / total_kcal
    F_ratio = (total_fat * 9) / total_kcal

    # Saturated Fat %kcal (이미 % 형태로 들어옴 → 단순 평균)
    saturated_fat_percent_kcal = total_saturated_fat_percent  # 합산 방식은 이후 정의

    # -----------------------------
    # 2) 단백질 점수 (최대 20)
    # -----------------------------
    W = daily["whoAmIDto"]["userWeight"]
    R = 1.2  # 기본 권장 1g/kg

    S_protein = 20 * min(1, total_protein / (R * W))

    # -----------------------------
    # 3) 탄·단·지 균형 (최대 15)
    # -----------------------------
    C_star, P_star, F_star = 0.50, 0.20, 0.30

    S_daily = 10 * max(
        0,
        1 - (1/3)*(
            abs(C_ratio - C_star)/0.15 +
            abs(P_ratio - P_star)/0.10 +
            abs(F_ratio - F_star)/0.10
        )
    )

    # 끼니별 보정: S_meals (5점)
    B_list = []
    for (mkcal, mp, mc, mf) in meal_macro_list:
        if mkcal > 0:
            c_m = (mc*4)/mkcal
            p_m = (mp*4)/mkcal
            f_m = (mf*9)/mkcal
        else:
            c_m = p_m = f_m = 0

        Bm = max(0,
            1
            - abs(c_m - C_star)/0.20
            - abs(p_m - P_star)/0.12
            - abs(f_m - F_star)/0.12
        )
        B_list.append(Bm)

    S_meals = 5 * (sum(B_list)/len(B_list)) if B_list else 0
    S_macro = S_daily + S_meals

    # -----------------------------
    # 4) 지방 점수 (10점)
    # -----------------------------
    SF = saturated_fat_percent_kcal
    Q_sat = 1 - min(1, (SF/12)**2)
    Q_fiber = min(1, total_fiber/25)
    S_fatq = 10 * (0.6*Q_sat + 0.4*Q_fiber)

    # -----------------------------
    # 5) 식이섬유 + 나트륨 (20점)
    # -----------------------------
    Na = total_sodium

    S_fiberNa = 10 * min(1, total_fiber/25) + 10 * max(0, 1 - min(1, (Na - 2000)/2000))

    # -----------------------------
    # 6) 식물성/비정제 보너스 (15점)
    # -----------------------------
    S_veg = 8 * min(1, vegetable_serv / 3)
    S_fruit = 4 * min(1, fruit_serv / 2)

    # 비정제 탄수화물 비율은 이후 raw 식재로 계산 필요
    S_unref = 0  # placeholder

    S_plant = min(15, S_veg + S_fruit + S_unref)

    # -----------------------------
    # 7) 패널티 (음수)
    # -----------------------------
    SugarP = (total_added_sugar_kcal / total_kcal)*100 if total_kcal>0 else 0

    D_pen = -(
        5 * max(0, min(1, (SugarP - 10)/10))
        + 5 * min(1, fried_count / 2)
        + 4 * min(1, total_processed_meat_gram / 50)
    )

    # -----------------------------
    # 최종 basic Score
    # -----------------------------
    basicScore = S_protein + S_macro + S_fatq + S_fiberNa + S_plant + D_pen

    return round(basicScore, 1)

def macular_score(daily: dict):
    gender = daily["whoAmIDto"]["Gender"]

    V = 0  # vegetable servings
    Fr = 0 # fruit servings

    fish_flag = False
    nut_flag = False
    oil_flag = False

    fish_keywords = ["연어", "고등어", "참치", "명태", "대구", "장어", "정어리"]
    nut_keywords = ["아몬드", "땅콩", "호두", "캐슈넛", "피스타치오", "브라질너트"]
    oil_keywords = ["올리브유", "카놀라유", "참기름", "들기름", "해바라기유"]

    for meal in daily["meals"]:
        for food in meal["foods"]:
            name = food["name"]

            if food.get("isVegetable", False):
                V += 1
            if food.get("isFruit", False):
                Fr += 1

            if any(k in name for k in fish_keywords):
                fish_flag = True
            if any(k in name for k in nut_keywords):
                nut_flag = True
            if any(k in name for k in oil_keywords):
                oil_flag = True

    # ------------------------
    # 성별별 점수 계산
    # ------------------------
    if gender == "FEMALE":
        macularDegenerationScore = (
            8 * min(1, V / 3)
            + 6 * min(1, Fr / 2)
        )
    else:  # MALE
        macularDegenerationScore = (
            8 * min(1, Fr / 2)
            + 6 * min(1, V / 3)
        )

    # 공통: 생선 + 견과 + 식물성 기름 점수
    macularDegenerationScore += (3 if fish_flag else 0)
    macularDegenerationScore += (2 if nut_flag else 0)
    macularDegenerationScore += (1 if oil_flag else 0)

    # 소수점 첫째 자리 반올림
    return round(macularDegenerationScore, 1)

def hypertension_score(daily: dict):
    total_sodium = 0
    total_kcal = 0
    weighted_sf = 0  # kcal * SF%
    
    V = 0  # vegetable servings
    Fr = 0 # fruit servings
    dairy_flag = False

    # 유제품 판정 기준: 칼슘이 높고 다른 분류가 아니어야 함
    for meal in daily["meals"]:
        for food in meal["foods"]:
            kcal = food.get("kcal", 0)
            sf = food.get("saturatedFatPercentKcal", 0)

            total_kcal += kcal
            weighted_sf += kcal * sf

            total_sodium += food.get("sodium", 0)

            if food.get("isVegetable", False):
                V += 1
            if food.get("isFruit", False):
                Fr += 1

            # 유제품 판정
            if (
                food.get("calcium", 0) >= 80
                and not food.get("isVegetable", False)
                and not food.get("isFruit", False)
                and not food.get("isFried", False)
                and food.get("processedMeatGram", 0) == 0
            ):
                dairy_flag = True

    # -------------------------------
    # 1) SF% 계산
    # -------------------------------
    if total_kcal > 0:
        SF_percent = weighted_sf / total_kcal
    else:
        SF_percent = 0

    # -------------------------------
    # 1) 나트륨 점수 ≤6
    # -------------------------------
    Na = total_sodium
    S_sodium = 6 * max(0, 1 - max(0, (Na - 2000)) / 1500)

    # -------------------------------
    # 2) 채소 점수 ≤4
    # -------------------------------
    S_veg = 4 * min(1, V / 5)

    # -------------------------------
    # 3) 과일 점수 ≤3
    # -------------------------------
    S_fruit = 3 * min(1, Fr / 4)

    # -------------------------------
    # 4) 포화지방 점수 ≤3
    # -------------------------------
    S_sat_fat = 3 * max(0, 1 - max(0, SF_percent - 7) / 5)

    # -------------------------------
    # 5) 비정제 탄수화물 점수 ≤2
    # -------------------------------
    Runref = daily.get("unrefinedCarbRatio", 0)
    S_unref = 2 * Runref

    # -------------------------------
    # 6) 유제품 점수 ≤2
    # -------------------------------
    S_dairy = 2 if dairy_flag else 0

    # -------------------------------
    # 총합
    # -------------------------------
    hypertensionScore = S_sodium + S_veg + S_fruit + S_sat_fat + S_unref + S_dairy
    return round(hypertensionScore, 1)

def myocardial_score(daily: dict):
    total_sodium = 0
    total_kcal = 0
    weighted_sf = 0  # kcal * SF% sum
    
    V = 0   # vegetable count
    Fr = 0  # fruit count
    fish_flag = False
    dairy_flag = False
    
    fish_keywords = ["연어", "고등어", "참치", "명태", "대구", "장어", "정어리"]

    for meal in daily["meals"]:
        for food in meal["foods"]:
            kcal = food.get("kcal", 0)
            sf = food.get("saturatedFatPercentKcal", 0)
            total_kcal += kcal
            weighted_sf += kcal * sf
            total_sodium += food.get("sodium", 0)

            # 채소/과일 서빙
            if food.get("isVegetable", False):
                V += 1
            if food.get("isFruit", False):
                Fr += 1

            # 생선
            if any(k in food["name"] for k in fish_keywords):
                fish_flag = True

            # 유제품 판정
            if (
                food.get("calcium", 0) >= 80
                and not food.get("isVegetable", False)
                and not food.get("isFruit", False)
                and not food.get("isFried", False)
                and food.get("processedMeatGram", 0) == 0
            ):
                dairy_flag = True

    # ----------------------------
    # SF% 계산
    # ----------------------------
    if total_kcal > 0:
        SF_percent = weighted_sf / total_kcal
    else:
        SF_percent = 0

    # ----------------------------
    # 1) 포화지방 점수 ≤5
    # ----------------------------
    S_sat_fat_mi = 5 * max(0, 1 - max(0, SF_percent - 7) / 8)

    # ----------------------------
    # 2) 과일 점수 ≤4
    # ----------------------------
    S_fruit_mi = 4 * min(1, Fr / 4)

    # ----------------------------
    # 3) 나트륨 점수 ≤3
    # ----------------------------
    Na = total_sodium
    S_sodium_mi = 3 * max(0, 1 - max(0, (Na - 2000)) / 2000)

    # ----------------------------
    # 4) 채소 점수 ≤3
    # ----------------------------
    S_veg_mi = 3 * min(1, V / 5)

    # ----------------------------
    # 5) 생선 점수 ≤3
    # ----------------------------
    S_fish_mi = 3 if fish_flag else 0

    # ----------------------------
    # 6) 비정제 탄수화물 점수 ≤1
    # ----------------------------
    Runref = daily.get("unrefinedCarbRatio", 0)
    S_unref_mi = 1 * Runref

    # ----------------------------
    # 7) 유제품 점수 ≤1
    # ----------------------------
    S_dairy_mi = 1 if dairy_flag else 0

    # ----------------------------
    # 총합 ≤20 + 소수점 첫째자리 반올림
    # ----------------------------
    myocardialInfarctionScore = (
        S_sat_fat_mi
        + S_fruit_mi
        + S_sodium_mi
        + S_veg_mi
        + S_fish_mi
        + S_unref_mi
        + S_dairy_mi
    )

    return round(myocardialInfarctionScore, 1)

def sarcopenia_score(daily: dict):
    total_protein = 0
    W = daily["whoAmIDto"]["userWeight"]

    dairy_flag = False
    vitc_flag = False
    vitb_flag = False

    for meal in daily["meals"]:
        for food in meal["foods"]:
            total_protein += food.get("protein", 0)
            name = food["name"]

            # --------------------------
            # 1) 유제품 추론 로직
            # --------------------------
            # 칼슘 높음 & 채소/과일/생선/고기/튀김이 아닐 때
            if (
                food.get("calcium", 0) >= 80
                and not food.get("isVegetable", False)
                and not food.get("isFruit", False)
                and not food.get("isFried", False)
                and food.get("processedMeatGram", 0) == 0
            ):
                dairy_flag = True

            # --------------------------
            # 2) 비타민 C 과일 (isFruit 사용)
            # --------------------------
            if food.get("isFruit", False):
                vitc_flag = True

            # --------------------------
            # 3) B군 급원 추론
            # --------------------------
            # 이름 기반 보조
            if any(k in name for k in ["연어", "고등어", "참치", "닭", "달걀", "계란", "두부", "콩"]):
                vitb_flag = True

    if W <= 0:
        return 0

    # --------------------------
    # 점수 계산
    # --------------------------
    S_protein = 12 * min(1, total_protein / (1.2 * W))
    S_dairy = 3 if dairy_flag else 0
    S_vitc = 3 if vitc_flag else 0
    S_vitb = 2 if vitb_flag else 0

    sarcopenia_score = S_protein + S_dairy + S_vitc + S_vitb

    return round(sarcopenia_score, 1)

def hyperlipidemia_score(daily: dict):
    total_kcal = 0
    weighted_sf = 0  # SF% * kcal
    total_fiber = 0

    fish_flag = False
    nut_flag = False
    oil_flag = False

    fish_keywords = ["연어", "고등어", "참치", "명태", "대구", "장어", "정어리"]
    nut_keywords = ["아몬드", "땅콩", "호두", "캐슈넛", "피스타치오", "브라질너트"]
    oil_keywords = ["올리브유", "카놀라유", "참기름", "들기름", "해바라기유"]

    for meal in daily["meals"]:
        for food in meal["foods"]:
            kcal = food.get("kcal", 0)
            sf = food.get("saturatedFatPercentKcal", 0)

            total_kcal += kcal
            weighted_sf += kcal * sf
            total_fiber += food.get("dietaryFiber", 0)

            fname = food["name"]

            # 생선
            if any(k in fname for k in fish_keywords):
                fish_flag = True
            # 견과
            if any(k in fname for k in nut_keywords):
                nut_flag = True
            # 식물성 기름
            if any(k in fname for k in oil_keywords):
                oil_flag = True

    # ----------------------------
    # Saturated Fat % (SF%)
    # ----------------------------
    if total_kcal > 0:
        SF_percent = weighted_sf / total_kcal
    else:
        SF_percent = 0

    # ----------------------------
    # 1) 포화지방 점수 (max 8)
    # ----------------------------
    S_sf = 8 * (1 - min(1, (SF_percent / 12) ** 2))

    # ----------------------------
    # 2) 섬유 점수 (max 6)
    # ----------------------------
    S_fiber = 6 * min(1, total_fiber / 25)

    # ----------------------------
    # 3) 불포화지방 점수 (생선 + 견과 + 식물성기름)
    # ----------------------------
    S_unsat = (3 if fish_flag else 0) + \
              (2 if nut_flag else 0) + \
              (1 if oil_flag else 0)

    # ----------------------------
    # 총점 (≤20) + 소수점 첫째 자리 반올림
    # ----------------------------
    hyperlipidemia_score = S_sf + S_fiber + S_unsat
    return round(hyperlipidemia_score, 1)

def bone_score(daily: dict):
    # -----------------------------
    # 1) 하루 전체 칼슘, 비타민 D, 단백질 합산
    # -----------------------------
    total_calcium = 0
    total_vitd = 0
    total_protein = 0

    for meal in daily["meals"]:
        for food in meal["foods"]:
            total_calcium += food.get("calcium", 0)
            total_vitd += food.get("vitaminD_IU", 0)
            total_protein += food.get("protein", 0)

    # -----------------------------
    # 2) 체중 W 가져오기
    # -----------------------------
    W = daily["whoAmIDto"]["userWeight"]

    if W <= 0:
        return 0  # 예외처리

    # -----------------------------
    # 3) 각각 점수 계산
    # -----------------------------
    # 칼슘 (max 12)
    S_calcium = 12 * min(1, total_calcium / 1200)

    # 비타민 D (max 5)
    S_vitd = 5 * min(1, total_vitd / 800)

    # 단백질 보조 (max 3)
    S_protein = 3 * min(1, total_protein / (1.0 * W))

    # -----------------------------
    # 4) 총점 반환 (≤20)
    # -----------------------------
    boneDiseaseScore = S_calcium + S_vitd + S_protein
    return round(boneDiseaseScore, 1)




# --------------------
# 점수 합산 함수
# total_sum: totalKcal, totalProtein, totalCarbs, totalFat, totalCalcium 계산
# score_sum: 점수를 리포트에 입력 및 기본점수와 대표질환점수(여러개면 평균) 합산
# --------------------

def total_sum(meals: list, result: dict):
    kcal = 0
    protein = 0
    carbs = 0
    fat = 0
    calcium = 0

    for i in meals:
        for j in i["foods"]:
            kcal += j["kcal"]
            protein += j["protein"]
            carbs += j["carbs"]
            fat += j["fat"]
            calcium += j["calcium"]

    result["totalKcal"] = kcal
    result["totalProtein"] = protein
    result["totalCarbs"] = carbs
    result["totalFat"] = fat
    result["totalCalcium"] = calcium

    return result

def score_sum(daily: dict, result: dict):

    result["basicScore"] = basic_score(daily)
    result["macularDegenerationScore"] = macular_score(daily)
    result["hypertensionScore"] = hypertension_score(daily)
    result["myocardialInfarctionScore"] = myocardial_score(daily)
    result["sarcopeniaScore"] = sarcopenia_score(daily)
    result["hyperlipidemiaScore"] = hyperlipidemia_score(daily)
    result["boneDiseaseScore"] = bone_score(daily)

    disease_average_score = 0

    topic_list = daily["whoAmIDto"]["topics"]
    disease_list = []
    for topic in topic_list:
        if topic["topicType"] in ["HEALTH_GOAL", "DISEASE_HISTORY"]:
            disease_list.append(topic["name"])

    for i in disease_list:
        if i == "황반변성":
            disease_average_score += result["macularDegenerationScore"]
        elif i == "고혈압":
            disease_average_score += result["hypertensionScore"]
        elif i == "심근경색":
            disease_average_score += result["myocardialInfarctionScore"]
        elif i == "근감소증":
            disease_average_score += result["sarcopeniaScore"]
        elif i == "고지혈증":
            disease_average_score += result["hyperlipidemiaScore"]
        elif i == "뼈 질환":
            disease_average_score += result["boneDiseaseScore"]

    if len(disease_list) == 0:
        disease_average_score = 20
    else:
        disease_average_score = disease_average_score / len(disease_list)

    result["summarizeScore"] = round(result["basicScore"] + disease_average_score, 1)

    return result


# --------------------
# AI 평가 함수
# feedback: AI가 피드백을 주어 summary와 severity를 입력하는 함수
# --------------------

def feedback(daily: dict, result: dict):
    meals_text = json.dumps(daily["meals"], ensure_ascii = False, indent = 2)
    system_prompt = """
        너는 60~80대 어르신의 하루 식단을 평가하는 영양사 AI야.

        [해야 할 일]
        1. 하루 동안 먹은 모든 식사를 보고, 건강 측면에서 간단히 평가해라.
        2. 아래 두 가지 값을 JSON 형식으로만 반환해야 한다.

        [summary 작성 규칙]
        - 한국어로 2~3문장으로 작성한다.
        - 존댓말(친절하고 부드러운 말투)을 사용한다.
        - 탄수화물/단백질/지방의 균형, 채소·과일 섭취 여부, 짠 음식·기름진 음식·단 음식·가공육 여부 등 주요 특징을 꼭 언급한다.
        - 개선이 필요한 점이 있으면 구체적으로 한두 가지 정도만 짚어서 제안해 준다.
        - 지나치게 무섭게 말하지 말고, "다음에는 이렇게 해보시면 더 좋겠습니다." 같은 톤으로 말한다.

        [severity 규칙]
        - 아래 셋 중 하나만 사용해야 한다. (다른 문자열 절대 금지)
            - "GOOD": 전반적으로 균형이 괜찮고 큰 문제는 없을 때
            - "SOSO": 대체로 괜찮지만 짜게 먹거나, 기름기·당류가 조금 많은 등 주의가 필요한 부분이 있을 때
            - "BAD" : 아주 짜거나, 매우 기름지거나, 단 음식/튀김/가공육 위주의 식단처럼 건강에 좋지 않은 편일 때

        [출력 형식 - 반드시 이 JSON 구조만 반환]
        {
          "summary": "여기에 2~3문장 요약",
          "severity": "GOOD 또는 SOSO 또는 BAD"
        }
        기타 설명, 말머리, 코드블록 표시는 절대 붙이지 마라.
    """

    messages = [
        SystemMessage(system_prompt),
        HumanMessage(
            "다음은 어떤 어르신의 하루 식단 정보입니다.\n"
            "이 정보를 바탕으로 하루 식단을 평가해 주세요.\n\n"
            f"{meals_text}"
        ),
    ]

    try:
        ai_response = llm.invoke(messages)
        parsed = json.loads(ai_response.content)

        summary = str(parsed.get("summary", "")).strip()
        severity = str(parsed.get("severity", "")).strip().upper()

        if severity not in {"GOOD", "SOSO", "BAD"}:
            severity = "SOSO"

        result["summary"] = summary
        result["severity"] = severity

    except Exception as e:
        result["summary"] = ""
        result["severity"] = "SOSO"
        result["status"] = "ERROR"
        result["errorMessage"] = f"식단 요약 생성 중 오류가 발생했습니다: {e}"

    return result



# --------------------
# 메인 함수
# --------------------

def daily_analysis(daily: dict):
    result = copy.deepcopy(body_format)
    result["reportId"] = daily["reportId"]

    try:
        result = total_sum(daily["meals"], result)
        result = score_sum(daily, result)
        result = feedback(daily, result)

        if result["status"] != "ERROR":        
            result["status"] = "SUCCESS"
            result["errorMessage"] = "none"

    except Exception as e:
        result = body_format.copy()
        result["reportId"] = daily["reportId"]
        result["status"] = "ERROR"
        result["errorMessage"] = str(e)

    return result



# --------------------
# 테스트용 코드
# --------------------

if __name__ == "__main__":

    test_daily_json = """
    {
        "reportId": 12353,
        "whoAmIDto": {
            "userId": 9007199254740991,
            "userName": "철수철수",
            "age": 65,
            "userHeight": 170,
            "userWeight": 78,
            "Gender": "MALE",
            "topics": [
              {
                "topicId": 9007199254740991,
                "topicType": "ALLERGEN",
                "name": "갑각류",
                "description": "갑각류 알러지",
                "source": "string"
              },
              {
                "topicId": 9007199254740992,
                "topicType": "DISEASE_HISTORY",
                "name": "황반변성",
                "description": "황반변성 질환",
                "source": "string"
              },
              {
                "topicId": 9007199254740993,
                "topicType": "DISEASE_HISTORY",
                "name": "뼈 질환",
                "description": "뼈 질환",
                "source": "string"
              }
            ]
          },
          "meals": [
            {
            "mealType": "BREAKFAST",
            "mealTime": "10:00:00",
              "photoUrl": "string",
              "foods": [
                {
                  "name": "밥",
                  "kcal": 130.0,
                  "protein": 2.7,
                  "carbs": 28.2,
                  "fat": 0.3,
                  "calcium": 10,
                  "servingSize": 100,
                  "saturatedFatPercentKcal": 0,
                  "unsaturatedFat": 0.1,
                  "dietaryFiber": 0.4,
                  "sodium": 0,
                  "addedSugarKcal": 0,
                  "processedMeatGram": 0,
                  "vitaminD_IU": 0,
                  "isVegetable": false,
                  "isFruit": false,
                  "isFried": false
                },
                {
                  "name": "미역국",
                  "kcal": 80,
                  "protein": 5,
                  "carbs": 8,
                  "fat": 3,
                  "calcium": 150,
                  "servingSize": 200,
                  "saturatedFatPercentKcal": 10,
                  "unsaturatedFat": 2,
                  "dietaryFiber": 1,
                  "sodium": 500,
                  "addedSugarKcal": 0,
                  "processedMeatGram": 0,
                  "vitaminD_IU": 0,
                  "isVegetable": true,
                  "isFruit": false,
                  "isFried": false
                }
              ]
            },
            {
            "mealType": "DINNER",
            "mealTime": "22:00:00",
              "photoUrl": "string",
              "foods": [
                {
                  "name": "돼지갈비",
                  "kcal": 300,
                  "protein": 25,
                  "carbs": 10,
                  "fat": 18,
                  "calcium": 20,
                  "servingSize": 150,
                  "saturatedFatPercentKcal": 40,
                  "unsaturatedFat": 8,
                  "dietaryFiber": 0,
                  "sodium": 500,
                  "addedSugarKcal": 20,
                  "processedMeatGram": 0,
                  "vitaminD_IU": 0,
                  "isVegetable": true,
                  "isFruit": false,
                  "isFried": false
                }
              ]
            }
          ],
          "callbackUrl": "string"
        }
    """

    test_daily = json.loads(test_daily_json)
    result = daily_analysis(test_daily)

    print(result)
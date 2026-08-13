# 💰 계층형 지출 관리 시스템 - myJava Mgr REST API 버전 (`expense-mgr-api`)

본 프로젝트는 `C:\Java\myJava` 폴더의 전통적인 **`DBConnectionMgr` (커넥션 풀 싱글톤)** 및 **`XxxMgr` / `XxxBean` 패턴**을 기반으로 구축된 RESTful API 서버입니다.

---

## 🏗 프로젝트 구조 (myJava Mgr & Bean 아키텍처)

```text
C:\Java\expense-mgr-api
├── pom.xml                                   # Jackson JSON 라이브러리 + MySQL 드라이버 + Shaded Executable JAR
├── init_sample_data.sql                      # DB 예시 데이터 구축 스크립트
├── README.md                                 # 프로젝트 안내 및 실행 가이드
├── 프로젝트_API_문서.html                     # 웹 브라우저용 API 명세서
└── src/main/java/
    ├── mgr/
    │   ├── DBConnectionMgr.java              # myJava 스타일 싱글톤 커넥션 풀
    │   ├── UserMgr.java                      # 회원 관련 SQL 및 비즈니스 매니저
    │   ├── ExpenseMgr.java                   # 지출 내역 SQL 및 비즈니스 매니저
    │   ├── CategoryMgr.java                  # 대/중분류 카테고리 매니저
    │   ├── BudgetMgr.java                    # 예산 한도 매니저
    │   ├── PointMgr.java                     # 포인트 적립/차감 매니저
    │   ├── PostMgr.java                      # 커뮤니티 매니저
    │   ├── FriendMgr.java                    # 친구 요청/수락 매니저
    │   ├── ChallengeMgr.java                 # 팀 챌린지 매니저
    │   ├── StoreMgr.java                     # 상점 및 구매 매니저
    │   └── SettingMgr.java                   # 앱 설정 매니저
    ├── bean/
    │   ├── UserBean.java                     # 회원 정보 자바 빈즈
    │   ├── ExpenseBean.java                  # 지출 정보 자바 빈즈
    │   ├── LargeCategoryBean.java            # 대분류 자바 빈즈
    │   ├── MediumCategoryBean.java           # 중분류 자바 빈즈
    │   ├── BudgetBean.java                   # 예산 정보 자바 빈즈
    │   ├── AppSettingBean.java               # 앱 설정 자바 빈즈
    │   ├── PointHistoryBean.java             # 포인트 이력 자바 빈즈
    │   ├── PostBean.java                     # 커뮤니티 게시글 자바 빈즈
    │   ├── FriendBean.java                   # 친구 관계 자바 빈즈
    │   ├── TeamRoomBean.java                 # 팀룸 자바 빈즈
    │   ├── TeamMemberBean.java               # 팀원 자바 빈즈
    │   ├── ItemBean.java                     # 상품 정보 자바 빈즈
    │   └── PurchaseBean.java                 # 구매 이력 자바 빈즈
    └── api/
        ├── MgrApiServer.java                 # 메인 실행 클래스 (포트 8080)
        └── handler/                          # Mgr 연동 REST API 핸들러 모듈 (10개)
```

---

## ⚡ 🚀 서버 실행 방법 (3가지 방식)

### 1. Maven Exec 플러그인으로 실행
```powershell
C:\Java\apache-maven-3.9.6\bin\mvn.cmd exec:java -f C:\Java\expense-mgr-api\pom.xml
```

### 2. Executable JAR 실행 (Maven 설치 없이 바로 구동)
```powershell
java -jar C:\Java\expense-mgr-api\target\expense-mgr-api-1.0.0.jar
```

### 3. Eclipse / IntelliJ / VS Code IDE에서 실행
* `src/main/java/api/MgrApiServer.java` 파일을 우클릭하여 **Run (실행)** 선택

---

## 🗄 DB 초기화 및 예시 데이터 넣기

MySQL Command Line 또는 커맨드 창에서 아래 명령어를 실행하면 `richman` 데이터베이스와 14개 테이블 및 예시 데이터가 생성됩니다:

```powershell
mysql -u root -p < C:\Java\expense-mgr-api\init_sample_data.sql
```

---

## 📡 Base URL 및 핵심 REST API 엔드포인트

* **Base URL**: `http://localhost:8080`

| 모듈 | Method | Endpoint | 설명 |
|---|---|---|---|
| **회원** | `POST` | `/api/users/login` | 로그인 (UserBean 반환) |
| **회원** | `POST` | `/api/users/register` | 신규 회원가입 |
| **지출** | `GET` | `/api/users/expenses?userId=1` | 회원 지출 목록 (`Vector<ExpenseBean>`) |
| **지출** | `POST` | `/api/expenses` | 지출 신규 등록 |
| **카테고리**| `GET` | `/api/categories/large` | 전체 대분류 목록 |
| **예산** | `GET` | `/api/budgets?userId=1` | 회원 예산 목록 |
| **포인트** | `POST` | `/api/points/earn` | 포인트 적립 |
| **상점** | `POST` | `/api/store/buy` | 포인트 상품 구매 |

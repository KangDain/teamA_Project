# 거지탈출 프로젝트 (Project_A)

본 프로젝트는 Swing 기반의 Java 클라이언트 애플리케이션과, myJava Mgr 패턴을 기반으로 한 REST API 서버(`expense-mgr-api`)로 구성된 지출 관리 시스템입니다.

## 📂 프로젝트 구조 (Project Structure)

```text
📦 project_A
 ┣ 📂 expense-mgr-api       # REST API 서버 백엔드 (Maven 기반, 내장 HTTP 서버 사용)
 ┃ ┣ 📜 pom.xml             # 백엔드 의존성 설정 파일
 ┃ ┣ 📜 init_sample_data.sql# DB 예시 데이터 스크립트
 ┃ ┣ 📜 README.md           # API 서버 상세 가이드
 ┃ ┗ 📂 src/main/java       # 백엔드 API 서버 소스 코드
 ┣ 📂 src                   # Swing UI 클라이언트 소스
 ┃ ┗ 📂 com.richman.ui
 ┃   ┗ 📜 GeojiTalchulApp.java # 메인 클라이언트 앱 소스
 ┣ 📂 lib                   # UI 클라이언트용 외부 라이브러리
 ┃ ┣ 📜 flatlaf-3.5.4.jar
 ┃ ┗ 📜 gson-2.14.0.jar
 ┣ 📂 font                  # 클라이언트 UI에서 사용하는 폰트 리소스
 ┃ ┣ 📜 MemomentKkukkukk.otf
 ┃ ┗ 📜 MemomentKkukkukk.ttf
 ┗ 📜 README.md             # 루트 프로젝트 구조 안내문 (현재 파일)
```

## 🚀 실행 가이드 (How to Run)

### 1. 백엔드 (API 서버) 구동
- `expense-mgr-api` 디렉토리로 이동하여 IDE나 Maven을 이용해 서버를 구동합니다.
- 메인 실행 클래스: `api.MgrApiServer` (기본 포트 `8080` 사용)
- 서버 실행에 관련된 상세 내용은 `expense-mgr-api/README.md` 문서를 참고하세요.

### 2. 프론트엔드 (UI 클라이언트) 구동
- Eclipse, VS Code, IntelliJ 등의 IDE에서 `C:\Java\project_A`를 열고 실행합니다.
- `lib/flatlaf-3.5.4.jar`와 `lib/gson-2.14.0.jar`가 프로젝트의 빌드 패스(클래스패스)에 추가되어 있어야 합니다.
- 메인 실행 클래스: `com.richman.ui.GeojiTalchulApp`

---

### 기타 Git 명령어 (참고용)

**Git 시작 및 파일 커밋**
```bash
git init
git add .
git commit -m "초기 커밋"
```

**브랜치 이름 변경 및 깃허브 주소 연결**
```bash
git branch -M main
git remote add origin [깃허브 저장소 URL 주소]
git push -u origin main
```

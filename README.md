## 📂 프로젝트 구조 (Project Structure)

```text
📦 project_A
 ┣ 📂 src
 ┃ ┣ 📂 com.richman.api     # REST API HTTP 통신 및 JSON 파싱 로직
 ┃ ┣ 📂 com.richman.model   # DTO 객체 (UserBean, ExpenseBean 등)
 ┃ ┗ 📂 com.richman.ui      # Swing 기반 화면 렌더링 및 이벤트 처리
 ┣ 📂 lib
 ┃ ┣ 📜 flatlaf-3.5.4.jar
 ┃ ┗ 📜 gson-2.14.0.jar
 ┗ 📜 .gitignore


.gitignore 충동 방지
```echo "bin/" > .gitignore

git 시작 및 파일 포장
```git init
```git add .
```git commit -m "Init: text"

브랜치 이름 변경 및 깃허브 주소 연결
```git branch -M kdn
```git remote add origin [깃허브 저장소 URL 주소]

push
```git push -u origin kdn

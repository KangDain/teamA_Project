-- =====================================================================
-- 계층형 지출 관리 시스템 (Expense Manager)
-- 데이터베이스 스키마 생성 및 초기 예시 데이터 삽입 스크립트
--
-- [특징]
--  - 사용자 1: 남성 사회초년생 (홍길동, 27세 백엔드 개발자) - 3개월치(6~8월) 상세 지출 데이터
--  - 사용자 2: 여성 대학교 4학년생 (김나영, 23세 취준생) - 3개월치(6~8월) 상세 지출 데이터
--
-- 실행 방법 (MySQL CLI 또는 MySQL Workbench):
--   mysql -u root -p < init_sample_data.sql
-- =====================================================================

-- 1. 데이터베이스 생성 및 선택
CREATE DATABASE IF NOT EXISTS richman
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE richman;

-- 외래 키 제약 조건 잠시 비활성화 (테이블 재생성/초기화 시)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 기존 테이블 삭제 (초기화용)
DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS team_room;
DROP TABLE IF EXISTS friend;
DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS purchase;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS point_history;
DROP TABLE IF EXISTS app_setting;
DROP TABLE IF EXISTS budget;
DROP TABLE IF EXISTS expense;
DROP TABLE IF EXISTS medium_category;
DROP TABLE IF EXISTS large_category;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 3. 테이블 스키마 생성 (14개 테이블)
-- =====================================================================

-- [1] 회원 테이블 (user)
CREATE TABLE user (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    login_id      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    user_name     VARCHAR(50)  NOT NULL,
    birth_date    DATE,
    gender        VARCHAR(10),
    phone         VARCHAR(20),
    job           VARCHAR(50),
    address       VARCHAR(255),
    income        INT DEFAULT 0,
    point_balance INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [2] 대분류 카테고리 (large_category)
CREATE TABLE large_category (
    large_id   INT AUTO_INCREMENT PRIMARY KEY,
    large_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [3] 중분류 카테고리 (medium_category)
CREATE TABLE medium_category (
    medium_id   INT AUTO_INCREMENT PRIMARY KEY,
    large_id    INT NOT NULL,
    medium_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (large_id) REFERENCES large_category(large_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [4] 지출 내역 (expense)
CREATE TABLE expense (
    expense_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    medium_id      INT NOT NULL,
    item_name      VARCHAR(100) NOT NULL,
    expense_amount INT NOT NULL,
    spent_date     DATE NOT NULL,
    memo           VARCHAR(255),
    is_fixed       TINYINT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (medium_id) REFERENCES medium_category(medium_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [5] 예산 설정 (budget)
CREATE TABLE budget (
    budget_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    large_id     INT NULL, -- NULL이면 전체 예산
    limit_amount INT NOT NULL,
    budget_scope VARCHAR(20) DEFAULT 'LARGE', -- 'TOTAL' or 'LARGE'
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (large_id) REFERENCES large_category(large_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [6] 앱 알림 설정 (app_setting)
CREATE TABLE app_setting (
    setting_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL UNIQUE,
    start_day       DATE NOT NULL, -- 정산 시작일
    alert_weekday   VARCHAR(10) DEFAULT '월', -- 주간 알림 요일
    alert_threshold INT DEFAULT 80, -- 경고 기준 임계값(%)
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [7] 포인트 변동 이력 (point_history)
CREATE TABLE point_history (
    point_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    point_type   VARCHAR(50) NOT NULL, -- '출석', '좋아요', '구매차감', '랭킹'
    point_amount INT NOT NULL, -- 양수=적립, 음수=차감
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [8] 상점 상품 마스터 (item)
CREATE TABLE item (
    item_id      INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    product_type VARCHAR(50) NOT NULL, -- '기프티콘', '스킨'
    price_point  INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [9] 상품 구매 내역 (purchase)
CREATE TABLE purchase (
    purchase_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    item_id      INT NOT NULL,
    used_point   INT NOT NULL,
    purchased_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [10] 커뮤니티 게시글 (post)
CREATE TABLE post (
    post_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    content    TEXT NOT NULL,
    like_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [11] 게시글 좋아요 (post_like)
CREATE TABLE post_like (
    like_id    INT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT NOT NULL,
    user_id    INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [12] 친구 관계 (friend)
CREATE TABLE friend (
    friend_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    friend_user_id INT NOT NULL,
    status         VARCHAR(20) DEFAULT '요청', -- '요청', '수락'
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friend_pair (user_id, friend_user_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [13] 챌린지 팀룸 (team_room)
CREATE TABLE team_room (
    room_id    INT AUTO_INCREMENT PRIMARY KEY,
    owner_id   INT NOT NULL,
    room_name  VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- [14] 챌린지 팀 멤버 (team_member)
CREATE TABLE team_member (
    member_id   INT AUTO_INCREMENT PRIMARY KEY,
    room_id     INT NOT NULL,
    user_id     INT NOT NULL,
    goal_amount INT NOT NULL,
    joined_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_user (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES team_room(room_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- 4. 예시 데이터 삽입 (Sample Data)
-- =====================================================================

-- 1) 대분류 카테고리
INSERT INTO large_category (large_id, large_name) VALUES
(1, '식비'),
(2, '교통/차량'),
(3, '주거/통신'),
(4, '패션/쇼핑'),
(5, '문화/취미'),
(6, '뷰티/미용'),
(7, '건강/의료'),
(8, '자기계발/교육');

-- 2) 중분류 카테고리
INSERT INTO medium_category (medium_id, large_id, medium_name) VALUES
-- 식비 (1)
(1, 1, '배달음식'),
(2, 1, '외식/식당'),
(3, 1, '카페/디저트'),
(4, 1, '식료품/장보기'),
-- 교통/차량 (2)
(5, 2, '대중교통'),
(6, 2, '주유/세차'),
(7, 2, '택시'),
-- 주거/통신 (3)
(8, 3, '월세'),
(9, 3, '관리비/공과금'),
(10, 3, '통신비/인터넷'),
-- 패션/쇼핑 (4)
(11, 4, '의류'),
(12, 4, '패션잡화'),
(13, 4, '생활용품'),
-- 문화/취미 (5)
(14, 5, '영화/공연'),
(15, 5, '도서/자기계발'),
(16, 5, '운동/헬스'),
-- 뷰티/미용 (6)
(17, 6, '화장품/스킨케어'),
(18, 6, '헤어/미용실'),
-- 자기계발/교육 (8)
(19, 8, '자격증/시험응시료'),
(20, 8, '스터디카페/독서실');

-- 3) 회원 데이터 (비밀번호: password123)
--  User 1: 남성 사회초년생 (홍길동, 27세, 월수입 280만원)
--  User 2: 여성 대학교 4학년생 (김나영, 23세 취준생, 월수입 95만원 - 알바비+용돈)
INSERT INTO user (user_id, login_id, password, user_name, birth_date, gender, phone, job, address, income, point_balance) VALUES
(1, 'hong123',   'password123', '홍길동', '1999-03-15', '남', '010-1234-5678', '사회초년생 (백엔드 개발자)', '서울시 마포구 공덕동', 2800000, 3450),
(2, 'nayoung22', 'password123', '김나영', '2003-08-22', '여', '010-9876-5432', '대학교 4학년생 (컴퓨터공학과)', '서울시 서대문구 신촌동', 950000, 2180),
(3, 'chulsoo',   'password123', '이철수', '1995-11-05', '남', '010-5555-4444', '직장인 (3년차)',           '경기도 성남시 분당구', 3800000, 1500),
(4, 'jimin',     'password123', '박지민', '2002-01-30', '여', '010-3333-2222', '대학생 (3학년)',            '서울시 마포구 신수동', 1200000, 800);

-- 4) 앱 알림 설정
INSERT INTO app_setting (setting_id, user_id, start_day, alert_weekday, alert_threshold) VALUES
(1, 1, '2026-08-25', '월', 80),
(2, 2, '2026-08-01', '금', 85),
(3, 3, '2026-08-25', '월', 75),
(4, 4, '2026-08-01', '수', 90);

-- 5) 예산 설정
INSERT INTO budget (budget_id, user_id, large_id, limit_amount, budget_scope) VALUES
(1, 1, NULL, 1800000, 'TOTAL'),  -- 홍길동 전체 예산 180만원
(2, 1, 1,    550000,  'LARGE'),  -- 홍길동 식비 예산 55만원
(3, 1, 2,    150000,  'LARGE'),  -- 홍길동 교통 예산 15만원
(4, 2, NULL, 700000,  'TOTAL'),  -- 김나영 전체 예산 70만원
(5, 2, 1,    300000,  'LARGE'),  -- 김나영 식비 예산 30만원
(6, 2, 8,    150000,  'LARGE');  -- 김나영 자기계발 예산 15만원

-- 6) 3개월치 지출 내역 (2026년 6월, 7월, 8월)

-- =====================================================================
-- [USER 1: 남성 사회초년생 홍길동 (27세 개발자)] 3개월치 지출 데이터
-- =====================================================================
INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES
-- --- 2026년 6월 (User 1) ---
(1, 8,  '6월 마포 원룸 월세', 550000, '2026-06-01', '매월 1일 고정 이체', 1),
(1, 16, '공덕 헬스장 3개월 분납', 60000,  '2026-06-01', '운동 고정비', 1),
(1, 5,  '지하철/버스 6월 정기권', 65000,  '2026-06-02', '출퇴근 교통비', 1),
(1, 3,  '스타벅스 아이스 아메리카노', 4500, '2026-06-02', '출근길 모닝커피', 0),
(1, 2,  '구의동 부대찌개 점심', 9500,   '2026-06-03', '팀원 점심식사', 0),
(1, 9,  '자취방 5월 관리비/가스비', 115000, '2026-06-05', '공과금', 1),
(1, 1,  '배달의민족 푸라닭 치킨', 23000,  '2026-06-06', '주말 금요일 야식', 0),
(1, 4,  '이마트 마포점 장보기', 74500,  '2026-06-07', '식료품/계란/고기', 0),
(1, 10, 'KT 5G 알뜰폰 통신비', 48000,  '2026-06-10', '통신비 자동이체', 1),
(1, 15, '스프링 부트 실전 개발 도서', 32000, '2026-06-11', '개발 서적 구입', 0),
(1, 7,  '야간 야근 카카오T 택시', 16400, '2026-06-12', '야근 귀가', 0),
(1, 3,  '투썸플레이스 케이크 & 커피', 11000, '2026-06-14', '주말 카페 공부', 0),
(1, 2,  '팀 회사 회식 (삼겹살)', 38000,  '2026-06-16', '부서 회식 1차', 0),
(1, 11, 'ZARA 여름 반팔 셔츠', 59000,  '2026-06-19', '출근용 의류', 0),
(1, 17, '올리브영 남성 올인원 스킨', 34000, '2026-06-22', '기초 화장품', 0),
(1, 14, 'CGV 영화 관람 & 팝콘', 21000,  '2026-06-26', '주말 영화', 0),
(1, 4,  '쿠팡 닭가슴살 팩 20개', 29800,  '2026-06-29', '식단 관리용', 0),

-- --- 2026년 7월 (User 1) ---
(1, 8,  '7월 마포 원룸 월세', 550000, '2026-07-01', '매월 1일 고정 이체', 1),
(1, 16, '공덕 헬스장 7월분', 60000,  '2026-07-01', '운동 고정비', 1),
(1, 5,  '지하철/버스 7월 정기권', 65000,  '2026-07-02', '출퇴근 교통비', 1),
(1, 3,  '스타벅스 자몽 허니 블랙티', 5700, '2026-07-03', '점심 후 디저트', 0),
(1, 9,  '자취방 6월 관리비/전기세', 128000, '2026-07-05', '여름 에어컨 전기세 포함', 1),
(1, 2,  '회사 근처 쌀국수 점심', 11000,  '2026-07-06', '점심 식사', 0),
(1, 13, '다이소 자취 생활용품', 14500,  '2026-07-08', '청소용품 및 휴지', 0),
(1, 10, 'KT 5G 알뜰폰 통신비', 48000,  '2026-07-10', '통신비 자동이체', 1),
(1, 12, '개발용 무소음 기계식 키보드', 128000, '2026-07-11', '장비 지출', 0),
(1, 2,  '맘스터치 싸이버거 세트', 7500,   '2026-07-14', '혼밥 점심', 0),
(1, 7,  '소나기 폭우 카카오T 택시', 19800, '2026-07-16', '비 많이 와서 택시 타기', 0),
(1, 1,  '배달의민족 엽기떡볶이', 19000,  '2026-07-19', '주말 배달', 0),
(1, 4,  '이마트 7월 식료품 장보기', 82400, '2026-07-21', '장보기', 0),
(1, 18, '공덕 헤어샵 남자 컷트', 22000,  '2026-07-25', '미용실', 0),
(1, 11, '무신사 여름 쿨링 슬랙스', 45000, '2026-07-28', '여름 출근 바지', 0),

-- --- 2026년 8월 (User 1 - 8/1~8/12) ---
(1, 8,  '8월 마포 원룸 월세', 550000, '2026-08-01', '매월 1일 이체', 1),
(1, 5,  '지하철/버스 8월 정기권', 65000,  '2026-08-02', '출퇴근', 1),
(1, 2,  '순대국밥 점심', 9000,    '2026-08-02', '점심', 0),
(1, 3,  '스타벅스 아이스 아메리카노', 4500, '2026-08-03', '커피', 0),
(1, 9,  '자취방 7월 관리비/전기세', 135000, '2026-08-05', '에어컨 사용 증가', 1),
(1, 4,  '이마트 8월 첫째주 장보기', 65200, '2026-08-07', '장보기', 0),
(1, 11, '여름 휴가 대비 반팔티', 29900,  '2026-08-09', '유니클로', 0),
(1, 10, 'KT 5G 알뜰폰 통신비', 48000,  '2026-08-10', '통신비', 1),
(1, 1,  '배달의민족 굽네 고추바사삭', 24000, '2026-08-10', '야식', 0),
(1, 3,  '메가커피 아이스 라떼', 3000,   '2026-08-11', '점심 커피', 0),
(1, 2,  '구의동 김치찌개 점심', 9000,   '2026-08-12', '점심', 0);


-- =====================================================================
-- [USER 2: 여성 대학교 4학년생 김나영 (23세 취준생)] 3개월치 지출 데이터
-- =====================================================================
INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES
-- --- 2026년 6월 (User 2 - 기말고사 & 졸업작품) ---
(2, 5,  '기후동행카드 6월 대학생권', 55000, '2026-06-01', '통학 교통비', 1),
(2, 10, '알뜰폰 통신비', 18500,        '2026-06-10', '고정 통신비', 1),
(2, 2,  '학생식당 돈까스 정식', 5500,     '2026-06-02', '점심 식사', 0),
(2, 3,  '공차 타피오카 버블티', 4800,     '2026-06-03', '등교길 음료', 0),
(2, 3,  '이디야 신촌점 (기말 공부)', 3800,  '2026-06-05', '카공', 0),
(2, 2,  '신촌 떡볶이 & 튀김', 7000,      '2026-06-07', '동기들과 저녁', 0),
(2, 20, '신촌 스터디카페 100시간권', 110000, '2026-06-09', '기말고사 & 취준 공부', 0),
(2, 2,  '서브웨이 이탈리안 비엠티', 7300,  '2026-06-12', '팀플 발표 전 점심', 0),
(2, 2,  '신촌 마라탕 (기말 종강)', 13500,   '2026-06-15', '시험 끝나고 회식', 0),
(2, 17, '올리브영 썸머 세일 선크림', 38500,  '2026-06-18', '화장품 구매', 0),
(2, 19, '토익(TOEIC) 정기시험 응시료', 52500, '2026-06-21', '어학 성적 준비', 0),
(2, 14, '인생네컷 동기들과 사진', 4000,    '2026-06-24', '추억 사진', 0),
(2, 15, '알라딘 중고서점 IT 면접서적', 14000, '2026-06-27', '취준 서적', 0),

-- --- 2026년 7월 (User 2 - 여름방학 인턴 & 취업 준비) ---
(2, 5,  '기후동행카드 7월권', 55000,    '2026-07-01', '교통비', 1),
(2, 10, '알뜰폰 통신비', 18500,        '2026-07-10', '통신비', 1),
(2, 20, '신촌 스터디룸 4시간 대여', 6000,   '2026-07-02', '취업 스터디 모임', 0),
(2, 3,  '아마스빈 사과요거트 버블티', 4200, '2026-07-04', '간식', 0),
(2, 18, '취업증명사진 스튜디오 촬영', 35000, '2026-07-07', '이력서 사진', 0),
(2, 2,  '합정 파스타 맛집 약속', 16500,   '2026-07-09', '친구 만남', 0),
(2, 2,  '알바 퇴근길 버거킹 세트', 6900,   '2026-07-12', '알바 저녁', 0),
(2, 11, '면접용 정장 블라우스', 49000,    '2026-07-15', '면접 의류', 0),
(2, 19, '정보처리기사 실기 교재', 28000,   '2026-07-18', '자격증 공부', 0),
(2, 3,  '스타벅스 생크림 케이크 & 커피', 10400, '2026-07-21', '카페 공부', 0),
(2, 17, '올리브영 마스크팩 세일', 22000,   '2026-07-24', '스킨케어', 0),
(2, 14, '대림미술관 전시회 티켓', 12000,   '2026-07-27', '문화생활', 0),

-- --- 2026년 8월 (User 2 - 8/1~8/12 수강신청 & 졸업학기 준비) ---
(2, 5,  '기후동행카드 8월권', 55000,    '2026-08-01', '교통비', 1),
(2, 2,  '학생식당 김치볶음밥', 5000,     '2026-08-02', '학식', 0),
(2, 3,  '투썸플레이스 아메리카노', 4500,   '2026-08-03', '커피', 0),
(2, 2,  '피자스쿨 포테이토 피자', 13900,   '2026-08-05', '수강신청 성공 축하', 0),
(2, 12, '면접용 블랙 구두', 65000,      '2026-08-08', '구두 구매', 0),
(2, 10, '알뜰폰 통신비', 18500,        '2026-08-10', '통신비', 1),
(2, 15, '카카오톡 생일 기프티콘 선물', 25000, '2026-08-10', '친구 선물', 0),
(2, 2,  '신촌 마라탕 점심', 11500,      '2026-08-12', '점심', 0);


-- 7) 포인트 변동 이력
INSERT INTO point_history (user_id, point_type, point_amount, created_at) VALUES
(1, '출석체크', 10, '2026-08-01 09:00:00'),
(1, '출석체크', 10, '2026-08-02 09:05:00'),
(1, '게시글 좋아요 수신', 5, '2026-08-03 14:20:00'),
(1, '7월 짠테크 챌린지 우승', 3000, '2026-08-01 10:00:00'),
(1, '구매차감', -500, '2026-08-10 11:30:00'),
(2, '출석체크', 10, '2026-08-01 08:30:00'),
(2, '출석체크', 10, '2026-08-02 08:40:00'),
(2, '6월 대학생 절약방 2등', 1500, '2026-07-01 10:00:00');

-- 8) 상점 상품 마스터
INSERT INTO item (item_id, product_name, product_type, price_point) VALUES
(1, '스타벅스 아이스 아메리카노 T', '기프티콘', 4500),
(2, '배달의민족 10,000원 상품권', '기프티콘', 10000),
(3, 'CU 편의점 5,000원 모바일금액권', '기프티콘', 5000),
(4, '앱 세련된 다크테마 스킨', '스킨', 500),
(5, '앱 네온 골드 뱃지 아이콘', '스킨', 1000);

-- 9) 상품 구매 내역
INSERT INTO purchase (user_id, item_id, used_point, purchased_at) VALUES
(1, 4, 500, '2026-08-10 11:30:00'),
(2, 1, 4500, '2026-07-05 16:45:00');

-- 10) 커뮤니티 게시글 (현실적인 절약 & 취준 스토리)
INSERT INTO post (post_id, user_id, content, like_count, created_at) VALUES
(1, 1, '사회초년생 3개월차 가계부 결산 중입니다! 마포 원룸 월세가 부담스러운데 외식 줄이고 헬스장 다니면서 월 100만원씩 적금 넣고 있어요 💪 다들 화이팅!', 5, '2026-08-03 14:00:00'),
(2, 2, '컴공 4학년 막학기 취준생입니다 ㅠㅠ 토익 응시료랑 스터디카페 비용이 은근 무섭네요.. 학식 적극 활용하고 기후동행카드로 교통비 절약하고 있습니다!', 8, '2026-08-05 10:30:00'),
(3, 3, '직장인 3년차 팁: 매월 고정지출(통신비 알뜰폰 전환, 미사용 OTT 해지)만 정리해도 연간 100만원 이상 절약됩니다!', 4, '2026-08-07 15:10:00');

-- 11) 게시글 좋아요
INSERT INTO post_like (post_id, user_id) VALUES
(1, 2), (1, 3), (1, 4),
(2, 1), (2, 3), (2, 4),
(3, 1), (3, 2);

-- 12) 친구 관계 (홍길동 - 김나영 수락, 홍길동 - 이철수 수락)
INSERT INTO friend (friend_id, user_id, friend_user_id, status) VALUES
(1, 1, 2, '수락'),
(2, 1, 3, '수락'),
(3, 2, 4, '요청');

-- 13) 챌린지 팀룸
INSERT INTO team_room (room_id, owner_id, room_name, start_date, end_date) VALUES
(1, 1, '여름 짠테크 챌린지 🏆 (사회초년생&취준생)', '2026-08-01', '2026-08-31'),
(2, 2, '대학생 식비 30만원 이하 도전 방 🥗', '2026-08-01', '2026-08-31');

-- 14) 챌린지 팀 멤버
INSERT INTO team_member (room_id, user_id, goal_amount) VALUES
(1, 1, 600000), -- 홍길동 목표: 60만원 이하 변동지출
(1, 2, 400000), -- 김나영 목표: 40만원 이하 변동지출
(1, 3, 800000), -- 이철수
(2, 2, 300000), -- 김나영
(2, 4, 250000); -- 박지민

-- 외래 키 제약 조건 복구
SET FOREIGN_KEY_CHECKS = 1;

-- 초기화 완료 메시지 출력
SELECT 'expense_manager DB 스키마 생성 및 3개월 상세 데모 데이터 삽입 완료!' AS message;

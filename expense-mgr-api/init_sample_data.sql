
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
    point_balance INT DEFAULT 0,
    profile_image MEDIUMTEXT
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
INSERT INTO user (user_id, login_id, password, user_name, birth_date, gender, phone, job, address, income, point_balance) VALUES
(1, 'woojin',    'password123', '정우진', '1999-05-15', '남', '010-1111-1111', '사회초년생 (1년차)', '서울시 관악구 신림동', 2800000, 3000),
(2, 'jimin',     'password123', '한지민', '2004-03-22', '여', '010-2222-2222', '대학생 (3학년)', '서울시 마포구 연남동', 800000, 1500),
(3, 'jihyun',    'password123', '김지현', '1982-10-10', '여', '010-3333-3333', '워킹맘 (마케터)', '경기도 성남시 분당구', 4500000, 1000),
(4, 'minsoo',    'password123', '박민수', '1975-07-25', '남', '010-4444-4444', '영업부장', '서울시 강남구 도곡동', 6500000, 500),
(5, 'eunyoung',  'password123', '최은영', '1992-12-05', '여', '010-5555-5555', '프리랜서 디자이너', '서울시 서대문구 창천동', 3200000, 800),
(6, 'soojin',    'password123', '이수진', '2008-08-08', '여', '010-6666-6666', '고등학생', '서울시 양천구 목동', 150000, 200),
(7, 'donghyun',  'password123', '강동현', '1985-02-14', '남', '010-7777-7777', '1인가구 (직장인)', '서울시 영등포구 당산동', 3500000, 4500),
(8, 'eunji',     'password123', '오은지', '2003-09-09', '여', '010-8888-8888', '대학생', '서울시 성동구 성수동', 600000, 1200),
(9, 'linda',     'password123', '린다',   '1994-11-20', '여', '010-9999-9999', '원어민 강사', '서울시 용산구 이태원동', 3000000, 900),
(10, 'yoojin',   'password123', '이유진', '1983-04-18', '여', '010-1010-1010', '주부', '경기도 용인시 수지구', 0, 1100);

-- 4) 앱 알림 설정
INSERT INTO app_setting (setting_id, user_id, start_day, alert_weekday, alert_threshold) VALUES
(1, 1, '2026-08-25', '월', 80),
(2, 2, '2026-08-01', '금', 85),
(3, 3, '2026-08-25', '월', 90),
(4, 4, '2026-08-01', '수', 100),
(5, 5, '2026-08-15', '금', 75),
(6, 6, '2026-08-01', '일', 80),
(7, 7, '2026-08-10', '토', 90),
(8, 8, '2026-08-01', '수', 85),
(9, 9, '2026-08-05', '월', 80),
(10, 10, '2026-08-01', '금', 80);

-- 5) 예산 설정
INSERT INTO budget (budget_id, user_id, large_id, limit_amount, budget_scope) VALUES
(1, 1, NULL, 1200000, 'TOTAL'),  -- 정우진(시드머니 목표, 타이트한 예산)
(2, 1, 1,    350000,  'LARGE'),
(3, 1, 2,    100000,  'LARGE'),
(4, 2, NULL, 700000,  'TOTAL'),  -- 한지민(비교탐색형)
(5, 2, 1,    400000,  'LARGE'),
(6, 3, 4,    300000,  'LARGE'),  -- 김지현(충동쇼핑 예산 제한)
(7, 5, NULL, 1500000, 'TOTAL');  -- 최은영(만성적자형)

-- 6) 3개월치 지출 내역 (2026년 6월, 7월, 8월)

-- =====================================================================
-- [USER 1: 정우진 (20대 사회초년생 - 목표 자금형)]
-- =====================================================================
INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES
-- 6월
(1, 8,  '6월 신림 원룸 월세', 450000, '2026-06-01', '고정비', 1),
(1, 5,  '지하철 정기권', 65000, '2026-06-02', '출퇴근', 1),
(1, 9,  '자취방 관리비', 80000, '2026-06-05', '고정비', 1),
(1, 10, '알뜰폰 요금', 25000, '2026-06-10', '고정비', 1),
(1, 4,  '쿠팡 닭가슴살 대량구매', 35000, '2026-06-12', '식비 방어용', 0),
(1, 2,  '구내식당 식권 20장', 100000, '2026-06-15', '점심 해결', 0),
(1, 16, '동네 헬스장 1개월', 40000, '2026-06-20', '운동', 1),
(1, 3,  '메가커피 아메리카노', 2000, '2026-06-25', '야근 커피', 0),
-- 7월
(1, 8,  '7월 신림 원룸 월세', 450000, '2026-07-01', '고정비', 1),
(1, 5,  '지하철 정기권', 65000, '2026-07-02', '출퇴근', 1),
(1, 9,  '자취방 관리비(에어컨)', 95000, '2026-07-05', '여름이라 좀 나옴', 1),
(1, 10, '알뜰폰 요금', 25000, '2026-07-10', '고정비', 1),
(1, 4,  '이마트 트레이더스 장보기', 55000, '2026-07-13', '주말 식비', 0),
(1, 2,  '친구 모임 (삼겹살)', 35000, '2026-07-18', '오랜만에 외식', 0),
(1, 16, '동네 헬스장 1개월', 40000, '2026-07-20', '운동', 1),
-- 8월
(1, 8,  '8월 신림 원룸 월세', 450000, '2026-08-01', '고정비', 1),
(1, 5,  '지하철 정기권', 65000, '2026-08-02', '출퇴근', 1),
(1, 9,  '자취방 관리비', 110000, '2026-08-05', '전기세 폭탄 방어 실패..', 1),
(1, 10, '알뜰폰 요금', 25000, '2026-08-10', '고정비', 1),
(1, 2,  '구내식당 식권 10장', 50000, '2026-08-11', '점심', 0),
(1, 4,  '당근마켓 중고 냄비', 10000, '2026-08-15', '생활비 절약', 0),
(1, 16, '동네 헬스장 1개월', 40000, '2026-08-20', '운동', 1);

-- =====================================================================
-- [USER 2: 한지민 (20대 대학생 - 비교탐색형)]
-- =====================================================================
INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES
-- 6월
(2, 2,  '연남동 하이엔드 오마카세', 120000, '2026-06-03', '인스타용 맛집!', 0),
(2, 3,  '성수 감성 카페', 18000, '2026-06-03', '디저트 넘 예쁨', 0),
(2, 11, '여름 원피스 (지그재그)', 65000, '2026-06-07', '데이트룩', 0),
(2, 17, '백화점 립스틱 신상', 48000, '2026-06-12', '화장품 쇼핑', 0),
(2, 5,  '기후동행카드', 55000, '2026-06-15', '교통비', 1),
(2, 7,  '새벽 택시', 23000, '2026-06-20', '술자리 후 귀가', 0),
(2, 18, '레이어드컷+펌', 180000, '2026-06-25', '기분전환', 0),
-- 7월
(2, 2,  '이태원 루프탑 펍', 75000, '2026-07-02', '친구들이랑 생파', 0),
(2, 3,  '신상 디저트 카페 투어', 22000, '2026-07-05', 'SNS 인증', 0),
(2, 14, '인생네컷+포토이즘', 16000, '2026-07-05', '사진', 0),
(2, 12, '디자이너 브랜드 실버백', 135000, '2026-07-10', '유행템 겟!', 0),
(2, 5,  '기후동행카드', 55000, '2026-07-15', '교통비', 1),
(2, 2,  '한강 피크닉 치맥', 38000, '2026-07-22', '배민', 0),
(2, 7,  '늦잠 지각 위기 택시', 12000, '2026-07-28', '아까운 내 돈', 0),
-- 8월 (현타 오고 절약 모드)
(2, 5,  '기후동행카드', 55000, '2026-08-01', '교통비', 1),
(2, 2,  '학식 돈까스', 5500, '2026-08-03', '이번 달은 진짜 아낀다', 0),
(2, 3,  '메가커피', 2000, '2026-08-05', '비싼 카페 금지', 0),
(2, 15, '토익 문제집', 18000, '2026-08-10', '공부나 하자', 0),
(2, 1,  '배민 (할인쿠폰 영끌)', 11000, '2026-08-15', '야식의 유혹 ㅠㅠ', 0),
(2, 2,  '서브웨이 단품', 6500, '2026-08-20', '점심', 0);

-- =====================================================================
-- [USER 3~10: 서브 페르소나 (간략한 3개월 예시)]
-- =====================================================================
INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES
-- 김지현 (워킹맘 - 스트레스 쇼핑)
(3, 11, '밤 11시 쿠팡 로켓배송 의류', 75000, '2026-06-12', '육아 퇴근 후 충동구매', 0),
(3, 4,  '마켓컬리 야간 장보기', 92000, '2026-07-15', '식비 폭발', 0),
(3, 13, '오늘의집 인테리어 소품', 45000, '2026-08-05', '또 샀네..', 0),
(3, 1,  '배민 육퇴후 야식', 28000, '2026-08-20', '스트레스 해소', 0),

-- 박민수 (영업부장 - 무기록, 골프, 택시)
(4, 6,  '주유비 (에스오일)', 80000, '2026-06-05', '법카인지 헷갈림', 0),
(4, 16, '스크린골프장 모임', 120000, '2026-07-10', '주말 골프', 0),
(4, 2,  '한우 등심 회식', 250000, '2026-08-02', '어디서 긁은거지', 0),
(4, 7,  '회식 후 모범택시', 35000, '2026-08-15', '기억안남', 0),

-- 최은영 (프리랜서 - 만성적자)
(5, 8,  '창천동 투룸 월세', 750000, '2026-06-01', '숨만 쉬어도 나가는 돈', 1),
(5, 20, '작업용 공유오피스 월패스', 250000, '2026-07-02', '고정비', 1),
(5, 10, '어도비, 넷플릭스 등 구독', 85000, '2026-08-05', '고정지출 너무 많아', 1),
(5, 1,  '야간 작업용 배민', 22000, '2026-08-12', '배고파', 0),

-- 이수진 (고등학생 - 편의점, 떡볶이)
(6, 4,  'GS25 편의점 간식', 4500, '2026-06-10', '하교길', 0),
(6, 2,  '동대문 엽떡', 16000, '2026-07-15', '친구들이랑 더치페이', 0),
(6, 17, '다이소 화장품', 5000, '2026-08-05', '가성비 굿', 0),
(6, 3,  '버블티', 3500, '2026-08-20', '당충전', 0),

-- 강동현 (1인가구 - 짠테크/퀘스트)
(7, 2,  '편의점 혜자도시락', 5500, '2026-06-15', '출석체크 포인트 받기용', 0),
(7, 10, '알뜰폰 초저가 요금제', 5500, '2026-07-10', '짠테크', 1),
(7, 1,  '배달 포장 (할인 영끌)', 12000, '2026-08-05', '미션 달성용 포장', 0),

-- 오은지 (커플 - 데이트 통장)
(8, 14, 'CGV 커플석+팝콘', 35000, '2026-06-20', '데이트', 0),
(8, 2,  '아웃백 스테이크', 85000, '2026-07-25', '기념일 데이트', 0),
(8, 3,  '성수동 대형 카페', 18000, '2026-08-14', '데이트', 0),

-- 린다 (외국인 - 이사 준비)
(9, 8,  '이태원 쉐어하우스 월세', 600000, '2026-06-01', 'Monthly Rent', 1),
(9, 13, '이케아 가구/소품', 150000, '2026-07-10', 'Moving prep', 0),
(9, 4,  '코스트코 식료품', 120000, '2026-08-05', 'Groceries', 0),

-- 이유진 (주부 - 연말정산)
(10, 19, '첫째 영어학원비', 350000, '2026-06-05', '교육비 영수증 챙기기', 1),
(10, 7,  '이마트 주말 장보기', 145000, '2026-07-12', '생활비 카드 연말정산', 0),
(10, 2,  '가족 외식 (갈비)', 85000, '2026-08-15', '외식비', 0);

-- 7) 포인트 변동 이력
INSERT INTO point_history (user_id, point_type, point_amount, created_at) VALUES
(1, '출석체크', 10, '2026-08-01 09:00:00'),
(2, '랭킹 보상', 1000, '2026-08-01 10:00:00');

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
(1, 1, '전세 자금 모으기 1년차! 식비 줄이기가 제일 힘드네요. 구내식당 최고!', 15, '2026-08-03 14:00:00'),
(2, 2, '다들 오마카세 가고 펑펑 쓰길래 저도 따라하다가 카드값 폭탄 맞았어요 ㅠㅠ 이번 달부터 진짜 아낍니다!', 28, '2026-08-05 10:30:00'),
(3, 5, '고정지출이 너무 많아서 줄일 데가 없는데 어쩌죠... 랭킹 1위분 존경스럽습니다.', 12, '2026-08-07 15:10:00');

-- 11) 게시글 좋아요
INSERT INTO post_like (post_id, user_id) VALUES
(1, 2), (1, 3), (1, 5),
(2, 1), (2, 4), (2, 6),
(3, 1), (3, 2);

-- 12) 친구 관계
INSERT INTO friend (friend_id, user_id, friend_user_id, status) VALUES
(1, 1, 2, '수락'),
(2, 2, 5, '수락'),
(3, 1, 7, '요청');

-- 13) 챌린지 팀룸
INSERT INTO team_room (room_id, owner_id, room_name, start_date, end_date) VALUES
(1, 1, '20대 시드머니 모으기 방 💰', '2026-08-01', '2026-08-31'),
(2, 8, '커플 데이트 비용 방어전 💑', '2026-08-01', '2026-08-31');

-- 14) 챌린지 팀 멤버
INSERT INTO team_member (room_id, user_id, goal_amount) VALUES
(1, 1, 500000), 
(1, 2, 600000),
(2, 8, 400000);

-- 외래 키 제약 조건 복구
SET FOREIGN_KEY_CHECKS = 1;

-- 초기화 완료 메시지 출력
SELECT 'expense_manager DB 스키마 생성 및 10명 페르소나 데모 데이터 삽입 완료!' AS message;
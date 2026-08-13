package gui;

import api.MgrApiServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ApiTestGui - expense-mgr-api REST API 전체 기능 검증용 Java Swing GUI 애플리케이션
 */
public class ApiTestGui extends JFrame {

    private final ApiClient apiClient;

    // Server Control UI
    private JTextField txtBaseUrl;
    private JLabel lblServerStatus;
    private JButton btnStartServer;
    private JButton btnStopServer;
    private JButton btnHealthCheck;

    // Response & Log Console UI
    private JLabel lblStatusBadge;
    private JLabel lblElapsedTime;
    private JTextArea txtPrettyJson;
    private JTextArea txtLogConsole;
    private JTabbedPane consoleTabbedPane;

    public ApiTestGui() {
        super("💰 계층형 지출 관리 REST API 통합 검증 테스터 (myJava Mgr)");
        this.apiClient = new ApiClient("http://localhost:8080");

        initUI();
        checkServerStatus();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 880);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // 1. Top Toolbar (Server Config & Status)
        add(createTopPanel(), BorderLayout.NORTH);

        // 2. Center Split Pane (Left: API Tabs, Right: Response/Log Console)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(640);
        mainSplitPane.setResizeWeight(0.5);

        // Left Panel: API Action Tabs
        mainSplitPane.setLeftComponent(createApiTabbedPane());

        // Right Panel: Response JSON & Log Console
        mainSplitPane.setRightComponent(createConsolePanel());

        add(mainSplitPane, BorderLayout.CENTER);

        // Window Closing Listener (Clean server shutdown prompt/cleanup if needed)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (MgrApiServer.isRunning()) {
                    MgrApiServer.stopServer();
                }
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));
        panel.setBackground(new Color(245, 247, 250));

        // Base URL Configuration
        JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        urlPanel.setOpaque(false);
        urlPanel.add(new JLabel("🌐 Base URL:"));

        txtBaseUrl = new JTextField("http://localhost:8080", 20);
        txtBaseUrl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtBaseUrl.addActionListener(e -> apiClient.setBaseUrl(txtBaseUrl.getText().trim()));
        urlPanel.add(txtBaseUrl);

        btnHealthCheck = new JButton("🔍 연결 확인");
        btnHealthCheck.setToolTipText("카테고리 API(GET /api/categories/large)를 호출하여 서버 연결 상태를 점검합니다.");
        btnHealthCheck.addActionListener(e -> checkServerStatus());
        urlPanel.add(btnHealthCheck);

        panel.add(urlPanel, BorderLayout.WEST);

        // Server Control & Status Indicator
        JPanel serverControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        serverControlPanel.setOpaque(false);

        lblServerStatus = new JLabel("⚪ 상태 확인 중...");
        lblServerStatus.setFont(new Font("Malgun Gothic", Font.BOLD, 12));

        btnStartServer = new JButton("🚀 내장 서버 시작");
        btnStartServer.setBackground(new Color(40, 167, 69));
        btnStartServer.setForeground(Color.WHITE);
        btnStartServer.setFocusPainted(false);
        btnStartServer.addActionListener(e -> startEmbeddedServer());

        btnStopServer = new JButton("🛑 내장 서버 중지");
        btnStopServer.setBackground(new Color(220, 53, 69));
        btnStopServer.setForeground(Color.WHITE);
        btnStopServer.setFocusPainted(false);
        btnStopServer.addActionListener(e -> stopEmbeddedServer());

        serverControlPanel.add(lblServerStatus);
        serverControlPanel.add(btnStartServer);
        serverControlPanel.add(btnStopServer);

        panel.add(serverControlPanel, BorderLayout.EAST);

        return panel;
    }

    private JTabbedPane createApiTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Malgun Gothic", Font.BOLD, 12));

        tabbedPane.addTab("👤 회원 API", createUsersTab());
        tabbedPane.addTab("💸 지출 API", createExpensesTab());
        tabbedPane.addTab("📂 카테고리 API", createCategoriesTab());
        tabbedPane.addTab("📊 예산 API", createBudgetsTab());
        tabbedPane.addTab("⭐ 포인트 & 상점", createPointsAndStoreTab());
        tabbedPane.addTab("💬 커뮤니티 API", createPostsTab());
        tabbedPane.addTab("👥 친구 & 챌린지", createFriendsAndChallengesTab());
        tabbedPane.addTab("⚙️ 환경설정 API", createSettingsTab());

        return tabbedPane;
    }

    // =========================================================================
    // 1. 회원 API Tab (/api/users)
    // =========================================================================
    private JPanel createUsersTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 1-1. 로그인 (POST /api/users/login)
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(new TitledBorder("🔑 로그인 테스트 (POST /api/users/login)"));
        GridBagConstraints gbc = createGbc();

        JTextField txtLoginId = new JTextField("hong123", 15);
        JPasswordField txtPassword = new JPasswordField("password123", 15);

        addFormField(loginPanel, gbc, 0, "로그인 ID:", txtLoginId);
        addFormField(loginPanel, gbc, 1, "비밀번호:", txtPassword);

        JPanel loginBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLogin = new JButton("▶ 로그인 요청");
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(e -> {
            String json = String.format("{\"loginId\":\"%s\",\"password\":\"%s\"}",
                    txtLoginId.getText().trim(), new String(txtPassword.getPassword()));
            executeApiRequest("POST", "/api/users/login", json);
        });
        loginBtnPanel.add(btnLogin);
        addCustomField(loginPanel, gbc, 2, loginBtnPanel);
        subTabs.addTab("로그인", loginPanel);

        // 1-2. 회원가입 (POST /api/users/register)
        JPanel regPanel = new JPanel(new GridBagLayout());
        regPanel.setBorder(new TitledBorder("📝 회원가입 테스트 (POST /api/users/register)"));
        gbc = createGbc();

        JTextField rLoginId = new JTextField("tester_new", 15);
        JTextField rPassword = new JTextField("pass1234", 15);
        JTextField rUserName = new JTextField("홍길동", 15);
        JTextField rBirthDate = new JTextField("1995-05-15", 15);
        JComboBox<String> rGender = new JComboBox<>(new String[]{"남", "여"});
        JTextField rPhone = new JTextField("010-1234-5678", 15);
        JTextField rJob = new JTextField("사회초년생 (백엔드 개발자)", 15);
        JTextField rAddress = new JTextField("서울시 마포구 공덕동", 15);
        JTextField rIncome = new JTextField("2800000", 15);

        addFormField(regPanel, gbc, 0, "아이디:", rLoginId);
        addFormField(regPanel, gbc, 1, "비밀번호:", rPassword);
        addFormField(regPanel, gbc, 2, "이름:", rUserName);
        addFormField(regPanel, gbc, 3, "생년월일(YYYY-MM-DD):", rBirthDate);
        addFormField(regPanel, gbc, 4, "성별:", rGender);
        addFormField(regPanel, gbc, 5, "연락처:", rPhone);
        addFormField(regPanel, gbc, 6, "직업:", rJob);
        addFormField(regPanel, gbc, 7, "주소:", rAddress);
        addFormField(regPanel, gbc, 8, "연소득(원):", rIncome);

        JPanel regBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRegister = new JButton("▶ 회원가입 요청");
        btnRegister.setBackground(new Color(40, 167, 69));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(e -> {
            String json = String.format("{\"loginId\":\"%s\",\"password\":\"%s\",\"userName\":\"%s\",\"birthDate\":\"%s\",\"gender\":\"%s\",\"phone\":\"%s\",\"job\":\"%s\",\"address\":\"%s\",\"income\":%s}",
                    rLoginId.getText().trim(), rPassword.getText().trim(), rUserName.getText().trim(),
                    rBirthDate.getText().trim(), rGender.getSelectedItem(), rPhone.getText().trim(),
                    rJob.getText().trim(), rAddress.getText().trim(), rIncome.getText().trim());
            executeApiRequest("POST", "/api/users/register", json);
        });

        JButton btnRegSample = new JButton("⚡ 샘플 데이터 채우기");
        btnRegSample.addActionListener(e -> {
            long rand = System.currentTimeMillis() % 10000;
            rLoginId.setText("user_" + rand);
            rPassword.setText("pass" + rand);
            rUserName.setText("홍길동_" + rand);
            rPhone.setText("010-1234-" + String.format("%04d", rand));
        });

        regBtnPanel.add(btnRegister);
        regBtnPanel.add(btnRegSample);
        addCustomField(regPanel, gbc, 9, regBtnPanel);
        subTabs.addTab("회원가입", new JScrollPane(regPanel));

        // 1-3. 아이디 중복확인 (GET /api/users/check-duplicate)
        JPanel dupPanel = new JPanel(new GridBagLayout());
        dupPanel.setBorder(new TitledBorder("🔍 아이디 중복확인 (GET /api/users/check-duplicate)"));
        gbc = createGbc();

        JTextField dupLoginId = new JTextField("hong123", 15);
        addFormField(dupPanel, gbc, 0, "검색 아이디:", dupLoginId);

        JButton btnDupCheck = new JButton("▶ 중복 확인 요청");
        btnDupCheck.addActionListener(e -> executeApiRequest("GET", "/api/users/check-duplicate?loginId=" + dupLoginId.getText().trim(), null));
        addCustomField(dupPanel, gbc, 1, btnDupCheck);
        subTabs.addTab("중복 확인", dupPanel);

        // 1-4. 회원 상세 조회 (GET /api/users/{userId})
        JPanel userDetailPanel = new JPanel(new GridBagLayout());
        userDetailPanel.setBorder(new TitledBorder("🆔 회원 정보 조회 (GET /api/users/{userId})"));
        gbc = createGbc();

        JTextField txtUserId = new JTextField("1", 15);
        addFormField(userDetailPanel, gbc, 0, "회원 ID (userId):", txtUserId);

        JButton btnGetUser = new JButton("▶ 회원정보 조회");
        btnGetUser.addActionListener(e -> executeApiRequest("GET", "/api/users/" + txtUserId.getText().trim(), null));
        addCustomField(userDetailPanel, gbc, 1, btnGetUser);
        subTabs.addTab("회원 조회", userDetailPanel);

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 2. 지출 API Tab (/api/expenses)
    // =========================================================================
    private JPanel createExpensesTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 2-1. 지출 목록 조회 (GET /api/expenses?userId=1)
        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setBorder(new TitledBorder("📋 회원 지출 목록 (GET /api/expenses?userId=...)"));
        GridBagConstraints gbc = createGbc();

        JTextField listUserId = new JTextField("1", 15);
        addFormField(listPanel, gbc, 0, "회원 ID (userId):", listUserId);

        JPanel listBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGetExpenses = new JButton("▶ 지출 목록 조회");
        btnGetExpenses.addActionListener(e -> executeApiRequest("GET", "/api/expenses?userId=" + listUserId.getText().trim(), null));

        JButton btnGetTotal = new JButton("💰 총 지출 금액 조회");
        btnGetTotal.addActionListener(e -> executeApiRequest("GET", "/api/expenses/total?userId=" + listUserId.getText().trim(), null));

        listBtnPanel.add(btnGetExpenses);
        listBtnPanel.add(btnGetTotal);
        addCustomField(listPanel, gbc, 1, listBtnPanel);

        subTabs.addTab("지출 목록 및 총액", listPanel);

        // 2-2. 지출 신규 등록 (POST /api/expenses)
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBorder(new TitledBorder("➕ 지출 신규 등록 (POST /api/expenses)"));
        gbc = createGbc();

        JTextField eUserId = new JTextField("1", 15);
        JTextField eMediumId = new JTextField("1", 15); // 중분류 ID
        JTextField eItemName = new JTextField("아메리카노 커피", 15);
        JTextField eAmount = new JTextField("4500", 15);
        JTextField eDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 15);
        JTextField eMemo = new JTextField("팀원 회의 중 간식 구매", 15);
        JCheckBox eFixed = new JCheckBox("고정 지출 여부 (fixed)");

        addFormField(addPanel, gbc, 0, "회원 ID (userId):", eUserId);
        addFormField(addPanel, gbc, 1, "중분류 ID (mediumId):", eMediumId);
        addFormField(addPanel, gbc, 2, "항목명 (itemName):", eItemName);
        addFormField(addPanel, gbc, 3, "금액 (expenseAmount):", eAmount);
        addFormField(addPanel, gbc, 4, "지출일 (spentDate):", eDate);
        addFormField(addPanel, gbc, 5, "메모 (memo):", eMemo);
        addCustomField(addPanel, gbc, 6, eFixed);

        JPanel addBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddExpense = new JButton("▶ 지출 등록 요청");
        btnAddExpense.setBackground(new Color(0, 123, 255));
        btnAddExpense.setForeground(Color.WHITE);
        btnAddExpense.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"mediumId\":%s,\"itemName\":\"%s\",\"expenseAmount\":%s,\"spentDate\":\"%s\",\"memo\":\"%s\",\"fixed\":%b}",
                    eUserId.getText().trim(), eMediumId.getText().trim(), eItemName.getText().trim(),
                    eAmount.getText().trim(), eDate.getText().trim(), eMemo.getText().trim(), eFixed.isSelected());
            executeApiRequest("POST", "/api/expenses", json);
        });

        JButton btnSample = new JButton("⚡ 샘플 데이터 채우기");
        btnSample.addActionListener(e -> {
            eItemName.setText("점심 식사 (비빔밥)");
            eAmount.setText("9000");
            eMemo.setText("맛있는 점심 식사");
            eFixed.setSelected(false);
        });

        addBtnPanel.add(btnAddExpense);
        addBtnPanel.add(btnSample);
        addCustomField(addPanel, gbc, 7, addBtnPanel);

        subTabs.addTab("지출 신규 등록", new JScrollPane(addPanel));

        // 2-3. 지출 삭제 (DELETE /api/expenses/{expenseId})
        JPanel delPanel = new JPanel(new GridBagLayout());
        delPanel.setBorder(new TitledBorder("❌ 지출 삭제 (DELETE /api/expenses/{expenseId})"));
        gbc = createGbc();

        JTextField delExpenseId = new JTextField("1", 15);
        addFormField(delPanel, gbc, 0, "지출 ID (expenseId):", delExpenseId);

        JButton btnDeleteExpense = new JButton("▶ 지출 삭제 요청");
        btnDeleteExpense.setBackground(new Color(220, 53, 69));
        btnDeleteExpense.setForeground(Color.WHITE);
        btnDeleteExpense.addActionListener(e -> executeApiRequest("DELETE", "/api/expenses/" + delExpenseId.getText().trim(), null));
        addCustomField(delPanel, gbc, 1, btnDeleteExpense);

        subTabs.addTab("지출 삭제", delPanel);

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 3. 카테고리 API Tab (/api/categories)
    // =========================================================================
    private JPanel createCategoriesTab() {
        JPanel main = new JPanel(new GridLayout(2, 1, 10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. 전체 대분류 목록 조회 (largeId 필요 없음)
        JPanel largePanel = new JPanel(new BorderLayout(10, 10));
        largePanel.setBorder(new TitledBorder("🏢 1. 전체 대분류 목록 조회 (GET /api/categories/large)"));

        JLabel lblLargeInfo = new JLabel("<html><b>💡 전체 대분류(식비, 교통, 주거 등) 목록을 조회합니다.</b><br/>(이 API는 대분류 ID 파라미터가 필요하지 않으며 전체 목록을 반환합니다.)</html>");
        lblLargeInfo.setBorder(new EmptyBorder(5, 5, 5, 5));
        largePanel.add(lblLargeInfo, BorderLayout.NORTH);

        JButton btnLargeCat = new JButton("▶ 전체 대분류 목록 조회");
        btnLargeCat.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnLargeCat.addActionListener(e -> executeApiRequest("GET", "/api/categories/large", null));
        largePanel.add(btnLargeCat, BorderLayout.SOUTH);

        main.add(largePanel);

        // 2. 대분류별 중분류 목록 조회 (largeId 필수 사용)
        JPanel medPanel = new JPanel(new BorderLayout(10, 10));
        medPanel.setBorder(new TitledBorder("📂 2. 특정 대분류의 중분류 목록 조회 (GET /api/categories/medium/by-large/{largeId})"));

        JLabel lblMedInfo = new JLabel("<html><b>💡 아래 입력한 [대분류 ID]에 해당하는 세부 중분류 항목들을 조회합니다.</b><br/>(예: 1 = 식비 [배달, 외식, 카페...], 2 = 교통/차량 [대중교통, 주유, 택시...], 3 = 주거/통신 [월세, 관리비...])</html>");
        lblMedInfo.setBorder(new EmptyBorder(5, 5, 5, 5));
        medPanel.add(lblMedInfo, BorderLayout.NORTH);

        JPanel inputForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputForm.add(new JLabel("대분류 ID (largeId):"));
        JTextField txtLargeId = new JTextField("1", 8);
        txtLargeId.setFont(new Font("Monospaced", Font.BOLD, 13));
        inputForm.add(txtLargeId);

        JButton btnMedCat = new JButton("▶ 해당 대분류의 중분류 목록 조회");
        btnMedCat.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        btnMedCat.setBackground(new Color(0, 123, 255));
        btnMedCat.setForeground(Color.WHITE);
        btnMedCat.addActionListener(e -> executeApiRequest("GET", "/api/categories/medium/by-large/" + txtLargeId.getText().trim(), null));
        inputForm.add(btnMedCat);

        medPanel.add(inputForm, BorderLayout.CENTER);

        main.add(medPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.add(main, BorderLayout.NORTH);
        return container;
    }

    // =========================================================================
    // 4. 예산 API Tab (/api/budgets)
    // =========================================================================
    private JPanel createBudgetsTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 4-1. 예산 목록 조회 (GET /api/budgets?userId=1)
        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setBorder(new TitledBorder("📋 예산 목록 (GET /api/budgets?userId=...)"));
        GridBagConstraints gbc = createGbc();

        JTextField bUserId = new JTextField("1", 15);
        addFormField(listPanel, gbc, 0, "회원 ID (userId):", bUserId);

        JButton btnGetBudgets = new JButton("▶ 예산 목록 조회");
        btnGetBudgets.addActionListener(e -> executeApiRequest("GET", "/api/budgets?userId=" + bUserId.getText().trim(), null));
        addCustomField(listPanel, gbc, 1, btnGetBudgets);

        subTabs.addTab("예산 조회", listPanel);

        // 4-2. 예산 설정 등록 (POST /api/budgets)
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBorder(new TitledBorder("➕ 예산 설정 등록 (POST /api/budgets)"));
        gbc = createGbc();

        JTextField bAddUserId = new JTextField("1", 15);
        JTextField bLargeId = new JTextField("1", 15);
        JTextField bLimitAmount = new JTextField("550000", 15);
        JComboBox<String> bScope = new JComboBox<>(new String[]{"TOTAL", "LARGE"});

        addFormField(addPanel, gbc, 0, "회원 ID (userId):", bAddUserId);
        addFormField(addPanel, gbc, 1, "대분류 ID (largeId):", bLargeId);
        addFormField(addPanel, gbc, 2, "한도 금액 (limitAmount):", bLimitAmount);
        addFormField(addPanel, gbc, 3, "예산 범위 (budgetScope - TOTAL:전체, LARGE:대분류):", bScope);

        JButton btnAddBudget = new JButton("▶ 예산 등록/업데이트");
        btnAddBudget.setBackground(new Color(0, 123, 255));
        btnAddBudget.setForeground(Color.WHITE);
        btnAddBudget.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"largeId\":%s,\"limitAmount\":%s,\"budgetScope\":\"%s\"}",
                    bAddUserId.getText().trim(), bLargeId.getText().trim(), bLimitAmount.getText().trim(), bScope.getSelectedItem());
            executeApiRequest("POST", "/api/budgets", json);
        });

        addCustomField(addPanel, gbc, 4, btnAddBudget);
        subTabs.addTab("예산 설정", addPanel);

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 5. 포인트 & 상점 API Tab (/api/points & /api/store)
    // =========================================================================
    private JPanel createPointsAndStoreTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 5-1. 포인트 조회 및 적립
        JPanel pointPanel = new JPanel(new GridBagLayout());
        pointPanel.setBorder(new TitledBorder("⭐ 포인트 잔액 및 적립/이력 (/api/points)"));
        GridBagConstraints gbc = createGbc();

        JTextField pUserId = new JTextField("1", 15);
        addFormField(pointPanel, gbc, 0, "회원 ID (userId):", pUserId);

        JPanel pGetBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGetBalance = new JButton("▶ 포인트 잔액 조회 (/balance)");
        btnGetBalance.addActionListener(e -> executeApiRequest("GET", "/api/points/balance?userId=" + pUserId.getText().trim(), null));

        JButton btnGetHistory = new JButton("▶ 포인트 이력 조회 (/history)");
        btnGetHistory.addActionListener(e -> executeApiRequest("GET", "/api/points/history?userId=" + pUserId.getText().trim(), null));

        pGetBtns.add(btnGetBalance);
        pGetBtns.add(btnGetHistory);
        addCustomField(pointPanel, gbc, 1, pGetBtns);

        // 적립 섹션
        JComboBox<String> pType = new JComboBox<>(new String[]{"출석체크", "게시글 좋아요 수신", "7월 짠테크 챌린지 우승", "게시글 작성", "출석"});
        pType.setEditable(true);
        JTextField pAmount = new JTextField("100", 15);
        addFormField(pointPanel, gbc, 2, "적립 유형 (pointType):", pType);
        addFormField(pointPanel, gbc, 3, "적립 금액 (amount):", pAmount);

        JButton btnEarnPoint = new JButton("▶ 포인트 적립 요청 (POST /earn)");
        btnEarnPoint.setBackground(new Color(40, 167, 69));
        btnEarnPoint.setForeground(Color.WHITE);
        btnEarnPoint.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"pointType\":\"%s\",\"amount\":%s}",
                    pUserId.getText().trim(), pType.getSelectedItem().toString().trim(), pAmount.getText().trim());
            executeApiRequest("POST", "/api/points/earn", json);
        });

        addCustomField(pointPanel, gbc, 4, btnEarnPoint);
        subTabs.addTab("포인트 관리", pointPanel);

        // 5-2. 상점 및 구매
        JPanel storePanel = new JPanel(new GridBagLayout());
        storePanel.setBorder(new TitledBorder("🏪 상점 아이템 및 구매 (/api/store)"));
        gbc = createGbc();

        JButton btnGetItems = new JButton("▶ 상점 상품 목록 조회 (GET /api/store/items)");
        btnGetItems.addActionListener(e -> executeApiRequest("GET", "/api/store/items", null));
        addCustomField(storePanel, gbc, 0, btnGetItems);

        JTextField sUserId = new JTextField("1", 15);
        JTextField sItemId = new JTextField("1", 15);
        addFormField(storePanel, gbc, 1, "회원 ID (userId):", sUserId);
        addFormField(storePanel, gbc, 2, "상품 ID (itemId):", sItemId);

        JButton btnBuyItem = new JButton("▶ 포인트 상품 구매 (POST /api/store/buy)");
        btnBuyItem.setBackground(new Color(255, 193, 7));
        btnBuyItem.setForeground(Color.BLACK);
        btnBuyItem.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"itemId\":%s}",
                    sUserId.getText().trim(), sItemId.getText().trim());
            executeApiRequest("POST", "/api/store/buy", json);
        });

        addCustomField(storePanel, gbc, 3, btnBuyItem);
        subTabs.addTab("상점 & 구매", storePanel);

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 6. 커뮤니티 API Tab (/api/posts)
    // =========================================================================
    private JPanel createPostsTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 6-1. 게시글 목록 (GET /api/posts)
        JPanel listPanel = new JPanel(new GridBagLayout());
        listPanel.setBorder(new TitledBorder("📜 게시글 목록 조회 (GET /api/posts)"));
        GridBagConstraints gbc = createGbc();

        JButton btnGetPosts = new JButton("▶ 전체 게시글 목록 조회");
        btnGetPosts.addActionListener(e -> executeApiRequest("GET", "/api/posts", null));
        addCustomField(listPanel, gbc, 0, btnGetPosts);
        subTabs.addTab("게시글 목록", listPanel);

        // 6-2. 게시글 작성 (POST /api/posts)
        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBorder(new TitledBorder("✍️ 게시글 작성 (POST /api/posts)"));
        gbc = createGbc();

        JTextField postUserId = new JTextField("1", 15);
        JTextArea postContent = new JTextArea(4, 25);
        postContent.setText("이번 달 무지출 챌린지 성공했습니다! 다들 화이팅!");
        postContent.setLineWrap(true);

        addFormField(addPanel, gbc, 0, "회원 ID (userId):", postUserId);
        addFormField(addPanel, gbc, 1, "게시글 내용 (content):", new JScrollPane(postContent));

        JButton btnAddPost = new JButton("▶ 게시글 작성 요청");
        btnAddPost.setBackground(new Color(0, 123, 255));
        btnAddPost.setForeground(Color.WHITE);
        btnAddPost.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"content\":\"%s\"}",
                    postUserId.getText().trim(), postContent.getText().replace("\n", "\\n").replace("\"", "\\\""));
            executeApiRequest("POST", "/api/posts", json);
        });

        addCustomField(addPanel, gbc, 2, btnAddPost);
        subTabs.addTab("게시글 작성", addPanel);

        // 6-3. 게시글 좋아요 (POST /api/posts/{postId}/like?userId=...)
        JPanel likePanel = new JPanel(new GridBagLayout());
        likePanel.setBorder(new TitledBorder("👍 게시글 좋아요 (POST /api/posts/{postId}/like)"));
        gbc = createGbc();

        JTextField lPostId = new JTextField("1", 15);
        JTextField lUserId = new JTextField("1", 15);

        addFormField(likePanel, gbc, 0, "게시글 ID (postId):", lPostId);
        addFormField(likePanel, gbc, 1, "회원 ID (userId):", lUserId);

        JButton btnLikePost = new JButton("▶ 좋아요 클릭");
        btnLikePost.addActionListener(e -> executeApiRequest("POST", "/api/posts/" + lPostId.getText().trim() + "/like?userId=" + lUserId.getText().trim(), null));
        addCustomField(likePanel, gbc, 2, btnLikePost);

        subTabs.addTab("좋아요", likePanel);

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 7. 친구 & 챌린지 API Tab (/api/friends & /api/challenges)
    // =========================================================================
    private JPanel createFriendsAndChallengesTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();

        // 7-1. 친구 API
        JPanel friendPanel = new JPanel(new GridBagLayout());
        friendPanel.setBorder(new TitledBorder("👥 친구 목록 및 요청 (/api/friends)"));
        GridBagConstraints gbc = createGbc();

        JTextField fUserId = new JTextField("1", 15);
        addFormField(friendPanel, gbc, 0, "회원 ID (userId):", fUserId);

        JButton btnGetFriends = new JButton("▶ 친구 목록 조회 (GET /api/friends?userId=...)");
        btnGetFriends.addActionListener(e -> executeApiRequest("GET", "/api/friends?userId=" + fUserId.getText().trim(), null));
        addCustomField(friendPanel, gbc, 1, btnGetFriends);

        JTextField fReqUserId = new JTextField("1", 15);
        JTextField fFriendId = new JTextField("2", 15);
        JComboBox<String> fStatus = new JComboBox<>(new String[]{"요청", "수락"});

        addFormField(friendPanel, gbc, 2, "요청자 ID (userId):", fReqUserId);
        addFormField(friendPanel, gbc, 3, "대상 친구 ID (friendUserId):", fFriendId);
        addFormField(friendPanel, gbc, 4, "상태 (status):", fStatus);

        JButton btnReqFriend = new JButton("▶ 친구 추가/요청 (POST /api/friends)");
        btnReqFriend.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"friendUserId\":%s,\"status\":\"%s\"}",
                    fReqUserId.getText().trim(), fFriendId.getText().trim(), fStatus.getSelectedItem());
            executeApiRequest("POST", "/api/friends", json);
        });
        addCustomField(friendPanel, gbc, 5, btnReqFriend);

        subTabs.addTab("친구 관리", friendPanel);

        // 7-2. 챌린지 API
        JPanel chalPanel = new JPanel(new GridBagLayout());
        chalPanel.setBorder(new TitledBorder("🏆 팀 챌린지 방 (/api/challenges)"));
        gbc = createGbc();

        JButton btnGetRooms = new JButton("▶ 전체 챌린지 방 목록 (GET /api/challenges)");
        btnGetRooms.addActionListener(e -> executeApiRequest("GET", "/api/challenges", null));
        addCustomField(chalPanel, gbc, 0, btnGetRooms);

        JTextField cOwnerId = new JTextField("1", 15);
        JTextField cRoomName = new JTextField("이번 주 10만원 절약 챌린지", 15);
        JTextField cStartDate = new JTextField("2026-08-15", 15);
        JTextField cEndDate = new JTextField("2026-08-22", 15);

        addFormField(chalPanel, gbc, 1, "방장 ID (ownerId):", cOwnerId);
        addFormField(chalPanel, gbc, 2, "방 이름 (roomName):", cRoomName);
        addFormField(chalPanel, gbc, 3, "시작일 (startDate):", cStartDate);
        addFormField(chalPanel, gbc, 4, "종료일 (endDate):", cEndDate);

        JButton btnCreateRoom = new JButton("▶ 챌린지 방 생성 (POST /api/challenges)");
        btnCreateRoom.setBackground(new Color(40, 167, 69));
        btnCreateRoom.setForeground(Color.WHITE);
        btnCreateRoom.addActionListener(e -> {
            String json = String.format("{\"ownerId\":%s,\"roomName\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\"}",
                    cOwnerId.getText().trim(), cRoomName.getText().trim(), cStartDate.getText().trim(), cEndDate.getText().trim());
            executeApiRequest("POST", "/api/challenges", json);
        });
        addCustomField(chalPanel, gbc, 5, btnCreateRoom);

        // 방 참여
        JTextField jRoomId = new JTextField("1", 15);
        JTextField jUserId = new JTextField("2", 15);
        JTextField jGoal = new JTextField("100000", 15);

        addFormField(chalPanel, gbc, 6, "참여 방 ID (roomId):", jRoomId);
        addFormField(chalPanel, gbc, 7, "참여자 ID (userId):", jUserId);
        addFormField(chalPanel, gbc, 8, "목표 금액 (goalAmount):", jGoal);

        JButton btnJoinRoom = new JButton("▶ 챌린지 방 참여 (POST /api/challenges/join)");
        btnJoinRoom.addActionListener(e -> {
            String json = String.format("{\"roomId\":%s,\"userId\":%s,\"goalAmount\":%s}",
                    jRoomId.getText().trim(), jUserId.getText().trim(), jGoal.getText().trim());
            executeApiRequest("POST", "/api/challenges/join", json);
        });
        addCustomField(chalPanel, gbc, 9, btnJoinRoom);

        subTabs.addTab("챌린지 방", new JScrollPane(chalPanel));

        main.add(subTabs, BorderLayout.CENTER);
        return main;
    }

    // =========================================================================
    // 8. 환경설정 API Tab (/api/settings)
    // =========================================================================
    private JPanel createSettingsTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("⚙️ 앱 환경설정 관리 (/api/settings)"));
        GridBagConstraints gbc = createGbc();

        JTextField sUserId = new JTextField("1", 15);
        addFormField(panel, gbc, 0, "회원 ID (userId):", sUserId);

        JButton btnGetSetting = new JButton("▶ 환경설정 조회 (GET /api/settings?userId=...)");
        btnGetSetting.addActionListener(e -> executeApiRequest("GET", "/api/settings?userId=" + sUserId.getText().trim(), null));
        addCustomField(panel, gbc, 1, btnGetSetting);

        JTextField sStartDay = new JTextField("2026-08-01", 15);
        JComboBox<String> sWeekday = new JComboBox<>(new String[]{"월", "화", "수", "목", "금", "토", "일"});
        JTextField sThreshold = new JTextField("80", 15);

        addFormField(panel, gbc, 2, "시작일 (startDay):", sStartDay);
        addFormField(panel, gbc, 3, "알림 요일 (alertWeekday):", sWeekday);
        addFormField(panel, gbc, 4, "알림 임계값 % (alertThreshold):", sThreshold);

        JButton btnSaveSetting = new JButton("▶ 환경설정 저장 (POST /api/settings)");
        btnSaveSetting.setBackground(new Color(0, 123, 255));
        btnSaveSetting.setForeground(Color.WHITE);
        btnSaveSetting.addActionListener(e -> {
            String json = String.format("{\"userId\":%s,\"startDay\":\"%s\",\"alertWeekday\":\"%s\",\"alertThreshold\":%s}",
                    sUserId.getText().trim(), sStartDay.getText().trim(), sWeekday.getSelectedItem(), sThreshold.getText().trim());
            executeApiRequest("POST", "/api/settings", json);
        });

        addCustomField(panel, gbc, 5, btnSaveSetting);

        main.add(panel, BorderLayout.NORTH);
        return main;
    }

    // =========================================================================
    // Right Console & Output Panel
    // =========================================================================
    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));

        // Header (Status Badge + Elapsed Time)
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        headerPanel.setBackground(new Color(238, 240, 243));

        lblStatusBadge = new JLabel(" READY ");
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(Color.GRAY);
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        lblStatusBadge.setBorder(new EmptyBorder(3, 8, 3, 8));

        lblElapsedTime = new JLabel("응답 대기 중...");
        lblElapsedTime.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));

        headerPanel.add(lblStatusBadge, BorderLayout.WEST);
        headerPanel.add(lblElapsedTime, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnBar.setOpaque(false);

        JButton btnCopyJson = new JButton("📋 JSON 복사");
        btnCopyJson.addActionListener(e -> {
            if (txtPrettyJson.getText() != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(txtPrettyJson.getText()), null);
                JOptionPane.showMessageDialog(this, "JSON이 클립보드에 복사되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnClear = new JButton("🗑️ 로그 지우기");
        btnClear.addActionListener(e -> {
            txtPrettyJson.setText("");
            txtLogConsole.setText("");
            lblStatusBadge.setText(" READY ");
            lblStatusBadge.setBackground(Color.GRAY);
            lblElapsedTime.setText("로그 초기화됨");
        });

        btnBar.add(btnCopyJson);
        btnBar.add(btnClear);
        headerPanel.add(btnBar, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Console Tabbed Pane (Pretty JSON vs Full Request Log)
        consoleTabbedPane = new JTabbedPane();
        consoleTabbedPane.setFont(new Font("Malgun Gothic", Font.BOLD, 12));

        txtPrettyJson = new JTextArea();
        txtPrettyJson.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtPrettyJson.setEditable(false);
        txtPrettyJson.setMargin(new Insets(8, 8, 8, 8));
        consoleTabbedPane.addTab("📄 응답 본문 (Pretty JSON)", new JScrollPane(txtPrettyJson));

        txtLogConsole = new JTextArea();
        txtLogConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtLogConsole.setEditable(false);
        txtLogConsole.setMargin(new Insets(8, 8, 8, 8));
        consoleTabbedPane.addTab("🔍 요청 & 실행 로그", new JScrollPane(txtLogConsole));

        panel.add(consoleTabbedPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // API Execution & Async Worker
    // =========================================================================
    private void executeApiRequest(String method, String endpoint, String jsonBody) {
        String baseUrl = txtBaseUrl.getText().trim();
        apiClient.setBaseUrl(baseUrl);

        lblStatusBadge.setText(" SENDING... ");
        lblStatusBadge.setBackground(new Color(23, 162, 184));
        lblElapsedTime.setText("요청 처리 중...");

        SwingWorker<ApiClient.ApiResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiClient.ApiResponse doInBackground() {
                return apiClient.sendRequest(method, endpoint, jsonBody);
            }

            @Override
            protected void done() {
                try {
                    ApiClient.ApiResponse res = get();
                    displayResponse(res);
                } catch (Exception e) {
                    lblStatusBadge.setText(" ERROR ");
                    lblStatusBadge.setBackground(Color.RED);
                    lblElapsedTime.setText("오류 발생: " + e.getMessage());
                    txtLogConsole.append("\n[EXCEPTION] " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void displayResponse(ApiClient.ApiResponse res) {
        // Status Badge Styling
        int code = res.getStatusCode();
        if (code >= 200 && code < 300) {
            lblStatusBadge.setText(String.format(" %d %s ", code, code == 201 ? "CREATED" : "OK"));
            lblStatusBadge.setBackground(new Color(40, 167, 69));
        } else if (code >= 400 && code < 500) {
            lblStatusBadge.setText(String.format(" %d BAD REQUEST ", code));
            lblStatusBadge.setBackground(new Color(255, 193, 7));
            lblStatusBadge.setForeground(Color.BLACK);
        } else if (code >= 500) {
            lblStatusBadge.setText(String.format(" %d SERVER ERROR ", code));
            lblStatusBadge.setBackground(new Color(220, 53, 69));
            lblStatusBadge.setForeground(Color.WHITE);
        } else {
            lblStatusBadge.setText(" CONNECTION FAILED ");
            lblStatusBadge.setBackground(Color.RED);
            lblStatusBadge.setForeground(Color.WHITE);
        }

        lblElapsedTime.setText(String.format("소요 시간: %d ms | URL: %s", res.getElapsedMs(), res.getUrl()));

        // Pretty JSON
        if (res.getPrettyResponseBody() != null) {
            txtPrettyJson.setText(res.getPrettyResponseBody());
            txtPrettyJson.setCaretPosition(0);
        } else if (res.getErrorMessage() != null) {
            txtPrettyJson.setText("❌ 요청 실패: " + res.getErrorMessage());
        } else {
            txtPrettyJson.setText("(응답 본문 없음)");
        }

        // Full Log
        StringBuilder log = new StringBuilder();
        log.append("==================================================\n");
        log.append(String.format("[%s] HTTP %s Request\n", new SimpleDateFormat("HH:mm:ss").format(new Date()), res.getMethod()));
        log.append("URL: ").append(res.getUrl()).append("\n");
        if (res.getRequestBody() != null && !res.getRequestBody().isBlank()) {
            log.append("Payload: ").append(res.getRequestBody()).append("\n");
        }
        log.append("Status: ").append(res.getStatusCode()).append(" (Time: ").append(res.getElapsedMs()).append("ms)\n");
        if (res.getErrorMessage() != null) {
            log.append("Error: ").append(res.getErrorMessage()).append("\n");
        }
        log.append("==================================================\n\n");

        txtLogConsole.append(log.toString());
        txtLogConsole.setCaretPosition(txtLogConsole.getDocument().getLength());
    }

    // =========================================================================
    // Server Status & Control
    // =========================================================================
    private void checkServerStatus() {
        lblServerStatus.setText("⚪ 연결 점검 중...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                String baseUrl = txtBaseUrl.getText().trim();
                apiClient.setBaseUrl(baseUrl);
                ApiClient.ApiResponse res = apiClient.sendRequest("GET", "/api/categories/large", null);
                return res.isSuccess();
            }

            @Override
            protected void done() {
                try {
                    boolean online = get();
                    if (online) {
                        lblServerStatus.setText("🟢 서버 정상 동작 중 (Port 8080)");
                        lblServerStatus.setForeground(new Color(40, 167, 69));
                    } else {
                        lblServerStatus.setText("🔴 서버 응답 없음 (서버 시작 필요)");
                        lblServerStatus.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblServerStatus.setText("🔴 서버 응답 없음");
                    lblServerStatus.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void startEmbeddedServer() {
        if (MgrApiServer.isRunning()) {
            JOptionPane.showMessageDialog(this, "서버가 이미 실행 중입니다.", "안내", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            MgrApiServer.startServer();
            lblServerStatus.setText("🟢 서버 정상 동작 중 (Port 8080)");
            lblServerStatus.setForeground(new Color(40, 167, 69));
            JOptionPane.showMessageDialog(this, "🚀 내장 Mgr REST API 서버가 구동되었습니다! (http://localhost:8080)", "서버 구동 성공", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 구동 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopEmbeddedServer() {
        if (!MgrApiServer.isRunning()) {
            JOptionPane.showMessageDialog(this, "서버가 실행 중이지 않습니다.", "안내", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MgrApiServer.stopServer();
        lblServerStatus.setText("🔴 서버 중지됨");
        lblServerStatus.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, "🛑 내장 REST API 서버가 중지되었습니다.", "서버 중지", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // Layout Helpers
    // =========================================================================
    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void addCustomField(JPanel panel, GridBagConstraints gbc, int row, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
        gbc.gridwidth = 1; // reset
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            ApiTestGui frame = new ApiTestGui();
            frame.setVisible(true);
        });
    }
}

package com.richman.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeojiTalchulApp extends JFrame {

    // --- UI 테마 상수 ---
    // 배경 및 기본 색상
    static final Color BG = Color.decode("#F7EDDA"); 
    static final Color WHITE = Color.WHITE;
    
    // 텍스트 및 비활성 색상
    static final Color TEXT = new Color(42, 48, 56); 
    static final Color MUTED = new Color(112, 124, 141);
    
    // 테두리 색상
    static final Color BORDER = Color.decode("#C5DEDA"); 

    // 주요 브랜드 색상 (버튼, 강조 효과 등)
    static final Color NAVY = Color.decode("#4F7670");
    static final Color GREEN_DARK = Color.decode("#4F7670");
    static final Color GREEN = Color.decode("#659F7C");
    static final Color GREEN_PALE = Color.decode("#92BEA9");
    static final Color BLUE = Color.decode("#9CB4D4");

    // 상태 색상 (경고, 알림 등)
    static final Color RED = new Color(219, 87, 91); 
    static final Color ORANGE = new Color(232, 155, 72); 
    static final Color PURPLE = new Color(132, 103, 175); 

    // 커스텀 폰트 객체를 직접 저장
    static Font BASE_FONT;
    static {
        try {
            java.io.File fontFile = new java.io.File("font/MemomentKkukkukk.ttf");
            // 만약 IDE 등에서 실행 위치가 다를 경우를 대비한 상대경로 폴백
            if (!fontFile.exists()) {
                fontFile = new java.io.File("../font/MemomentKkukkukk.ttf");
            }
            
            if(fontFile.exists()) {
                BASE_FONT = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(BASE_FONT);
                
                java.util.Enumeration<Object> keys = javax.swing.UIManager.getDefaults().keys();
                Font defaultFont = BASE_FONT.deriveFont(Font.PLAIN, 14f);
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = javax.swing.UIManager.get(key);
                    if (value instanceof javax.swing.plaf.FontUIResource || value instanceof Font) {
                        javax.swing.UIManager.put(key, new javax.swing.plaf.FontUIResource(defaultFont));
                    }
                }
            } else {
                System.out.println("폰트 파일을 찾을 수 없습니다: " + fontFile.getAbsolutePath());
                BASE_FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            e.printStackTrace();
            BASE_FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
        }
    }

    static final Font FONT = BASE_FONT.deriveFont(Font.PLAIN, 14f);
    static final Font FONT_BOLD = BASE_FONT.deriveFont(Font.BOLD, 14f);
    static final Font FONT_TITLE = BASE_FONT.deriveFont(Font.BOLD, 25f);

    // --- 애플리케이션 상태 (전역 변수) ---
    final AppState state = new AppState();
    String dismissedFixedCandidateKey = "";
    final JPanel content = new JPanel(new CardLayout());
    final JLabel pointLabel = new JLabel();
    final JLabel userLabel = new JLabel("로그인 필요");
    final JLabel sidebarAvatarLabel = new JLabel();

    HomePanel homePanel;
    CalendarPanel calendarPanel;
    StatisticsPanel statisticsPanel;
    CommunityPanel communityPanel;
    MyStorePanel myStorePanel;

    // --- 최상위 화면(루트) 관리 ---
    final JPanel rootContainer = new JPanel(new CardLayout());
    JPanel loginPanel;
    JPanel signupPanel;
    JPanel mainShell;

    // --- 로그인 세션 정보 ---
    int currentUserId = -1;
    Image currentUserProfileImage = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png")).getImage();

    public GeojiTalchulApp() {
        super("거지 탈출 - 계층형 지출 관리");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 820));
        setSize(1500, 900);
        setLocationRelativeTo(null);

        homePanel = new HomePanel();
        calendarPanel = new CalendarPanel();
        statisticsPanel = new StatisticsPanel();
        communityPanel = new CommunityPanel();
        myStorePanel = new MyStorePanel();

        content.add(homePanel, "HOME");
        content.add(calendarPanel, "CALENDAR");
        content.add(statisticsPanel, "STATS");
        content.add(communityPanel, "COMMUNITY");
        content.add(myStorePanel, "STORE");

        loginPanel = buildLoginPanel();
        signupPanel = buildSignupPanel();
        mainShell = buildShell(); 

        rootContainer.add(loginPanel, "AUTH_LOGIN");
        rootContainer.add(signupPanel, "AUTH_SIGNUP");
        rootContainer.add(mainShell, "MAIN_APP");

        setContentPane(rootContainer);
        ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_LOGIN");

        refreshAll();
        loadCategoriesFromServer();
    }

    void loadCategoriesFromServer() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/categories/large");
                if (el != null && el.isJsonArray()) {
                    java.util.List<String> largeCats = new java.util.ArrayList<>();
                    java.util.Map<String, java.util.List<String>> tempMediumMap = new java.util.LinkedHashMap<>();
                    for (com.google.gson.JsonElement item : el.getAsJsonArray()) {
                        com.google.gson.JsonObject obj = item.getAsJsonObject();
                        int largeId = obj.get("largeId").getAsInt();
                        String largeName = obj.get("largeName").getAsString();
                        largeCats.add(largeName);
                        com.google.gson.JsonElement mediumEl = httpGetElement("http://localhost:8080/api/categories/medium/by-large/" + largeId);
                        java.util.List<String> mediumCats = new java.util.ArrayList<>();
                        if (mediumEl != null && mediumEl.isJsonArray()) {
                            for (com.google.gson.JsonElement mItem : mediumEl.getAsJsonArray()) {
                                mediumCats.add(mItem.getAsJsonObject().get("mediumName").getAsString());
                            }
                        }
                        tempMediumMap.put(largeName, mediumCats);
                    }
                    state.largeCategories = largeCats;
                    state.mediumMap = tempMediumMap;
                }
                return null;
            }
        }.execute();
    }

    void loadMySettingsFromServer() {
        new SwingWorker<com.google.gson.JsonObject, Void>() {
            @Override
            protected com.google.gson.JsonObject doInBackground() throws Exception {
                com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/settings?userId=" + currentUserId);
                if (el != null && el.isJsonObject()) {
                    return el.getAsJsonObject();
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    com.google.gson.JsonObject obj = get();
                    if (obj != null) {
                        if (obj.has("alertThreshold")) state.alertThreshold = obj.get("alertThreshold").getAsInt();
                        if (obj.has("currentSkin") && !obj.get("currentSkin").isJsonNull()) {
                            state.currentSkin = obj.get("currentSkin").getAsString();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    void loadMySkinsFromServer() {
        new SwingWorker<com.google.gson.JsonArray, Void>() {
            @Override
            protected com.google.gson.JsonArray doInBackground() throws Exception {
                com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/store/purchases?userId=" + currentUserId);
                if (el != null && el.isJsonArray()) {
                    return el.getAsJsonArray();
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    com.google.gson.JsonArray arr = get();
                    if (arr != null) {
                        state.ownedSkins.clear();
                        state.ownedSkins.add("poorman.png"); // 기본 스킨은 무조건 포함
                        for (int i = 0; i < arr.size(); i++) {
                            com.google.gson.JsonObject item = arr.get(i).getAsJsonObject();
                            String name = item.has("productName") ? item.get("productName").getAsString() : "";
                            if (name.contains("거지")) {
                                state.ownedSkins.add("poorman.png");
                            } else if (name.contains("부자") || name.contains("캐릭터 꾸미기")) {
                                state.ownedSkins.add("richman.png");
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    void loadMyBudgetFromServer() {
        new SwingWorker<com.google.gson.JsonElement, Void>() {
            @Override
            protected com.google.gson.JsonElement doInBackground() throws Exception {
                return httpGetElement("http://localhost:8080/api/budgets?userId=" + currentUserId);
            }
            @Override
            protected void done() {
                try {
                    com.google.gson.JsonElement el = get();
                    if (el != null && el.isJsonArray()) {
                        for (com.google.gson.JsonElement item : el.getAsJsonArray()) {
                            com.google.gson.JsonObject obj = item.getAsJsonObject();
                            String scope = obj.has("budgetScope") ? obj.get("budgetScope").getAsString() : "";
                            if ("TOTAL".equals(scope) && obj.has("limitAmount")) {
                                state.budget = obj.get("limitAmount").getAsLong();
                                refreshAll();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // 서버에서 내 지출 내역을 싹 긁어오는 메서드
    void loadMyExpensesFromServer() {
        new SwingWorker<com.google.gson.JsonElement, Void>() {
            @Override
            protected com.google.gson.JsonElement doInBackground() throws Exception {
                return httpGetElement("http://localhost:8080/api/expenses?userId=" + currentUserId);
            }

            @Override
            protected void done() {
                try {
                    com.google.gson.JsonElement el = get();
                    if (el == null) return;

                    com.google.gson.JsonArray arr = null;

                    // 서버가 {"data": [...]} 형태로 반환하는 경우
                    if (el.isJsonObject()) {
                        com.google.gson.JsonObject res = el.getAsJsonObject();
                        if (res.has("data")) {
                            arr = res.getAsJsonArray("data");
                        }
                    // 서버가 [...] 배열을 바로 반환하는 경우 (방어 처리)
                    } else if (el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    }

                    if (arr == null) return;

                    state.expenses.clear(); // 기존 거 싹 비우고

                    for (com.google.gson.JsonElement item : arr) {
                        JsonObject obj = item.getAsJsonObject();
                        String large  = obj.has("largeCategory")  ? obj.get("largeCategory").getAsString()  : "";
                        String medium = obj.has("mediumCategory") ? obj.get("mediumCategory").getAsString() : "";
                        String small  = obj.has("smallCategory")  ? obj.get("smallCategory").getAsString()  : "";
                        String name   = obj.has("item")           ? obj.get("item").getAsString()           : "";
                        long amount   = obj.has("amount")         ? obj.get("amount").getAsLong()           : 0;
                        String dateStr = obj.has("expenseDate")   ? obj.get("expenseDate").getAsString()    : "";
                        int expenseId = obj.has("expenseId")      ? obj.get("expenseId").getAsInt()         : (state.nextId++);
                        if (dateStr.isEmpty()) continue;
                        state.addDbExpense(expenseId, large, medium, small, name, amount, LocalDate.parse(dateStr));
                    }
                    refreshAll(); // 데이터 다 받았으니 화면 싹 새로고침!
                } catch (Exception ex) {
                    System.err.println("데이터 불러오기 실패: " + ex.getMessage());
                }
            }
        }.execute();
    }

    /** GET 요청을 보내고 응답을 JsonElement로 반환 (배열/객체 모두 처리). 오류 시 null 반환. */
    com.google.gson.JsonElement httpGetElement(String urlStr) {
        try {
            java.net.URL url = new java.net.URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return JsonParser.parseString(sb.toString());
            }
        } catch (Exception e) {
            System.err.println("httpGetElement 실패: " + e.getMessage());
            return null;
        }
    }



    JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        // 중앙 로그인 박스 (둥근 테두리)
        JPanel card = roundedPanel(WHITE, 20);
        card.setLayout(new BorderLayout(0, 25));
        card.setBorder(new EmptyBorder(50, 60, 50, 60));

        JLabel title = new JLabel("거지탈출", SwingConstants.CENTER);
        title.setFont(BASE_FONT.deriveFont(Font.BOLD, 32f));
        title.setForeground(GREEN_DARK);
        card.add(title, BorderLayout.NORTH);

        // 아이디 / 비밀번호 입력 폼
        JPanel form = new JPanel(new GridLayout(2, 1, 0, 15));
        form.setOpaque(false);
        
        JTextField idField = new JTextField();
        idField.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER), "아이디"));
        idField.setFont(FONT);
        
        JPasswordField pwField = new JPasswordField();
        pwField.setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER), "비밀번호"));
        pwField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        form.add(idField);
        form.add(pwField);
        card.add(form, BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnBox = new JPanel(new GridLayout(2, 1, 0, 10));
        btnBox.setOpaque(false);
        btnBox.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton loginBtn = primaryButton("로그인");
        loginBtn.setForeground(Color.WHITE); //  글자는 흰색으로 명확하게
        loginBtn.setBackground(new Color(41, 128, 185)); //  눈에 확 띄는 쨍한 파란색 계열 적용
        loginBtn.addActionListener(e -> {
            String loginId = idField.getText().trim();
            String password = new String(pwField.getPassword()).trim();

            if (loginId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력해 주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            loginBtn.setEnabled(false);
            loginBtn.setText("로그인 중...");

            new SwingWorker<JsonObject, Void>() {
                @Override
                protected JsonObject doInBackground() throws Exception {
                    String body = "{\"loginId\":\"" + loginId + "\",\"password\":\"" + password + "\"}";
                    return httpPost("http://localhost:8080/api/users/login", body);
                }

                @Override
                protected void done() {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("로그인");
                    try {
                        JsonObject res = get();
                        if (res != null && res.has("userId")) {
                            currentUserId = res.get("userId").getAsInt();

                            // 로그인 성공 후 데이터 동기화
                            loadMyBudgetFromServer();
                            loadMySettingsFromServer();
                            loadMyExpensesFromServer();
                            loadMySkinsFromServer();
                            communityPanel.loadPostsFromServer();

                            String userName = res.has("userName") ? res.get("userName").getAsString() : loginId;
                            userLabel.setText(userName + " 님");
                            
                            // 로그인 시 포인트 잔액 동기화
                            if (res.has("pointBalance")) {
                                state.points = res.get("pointBalance").getAsInt();
                            }
                            
                            //  [추가할 부분] 백엔드가 프로필 사진을 줬는지 검사!
                            try {
                                if (res.has("profileImage") && !res.get("profileImage").isJsonNull()) {
                                    String base64 = res.get("profileImage").getAsString();
                                    byte[] imgBytes = java.util.Base64.getDecoder().decode(base64);
                                    currentUserProfileImage = new ImageIcon(imgBytes).getImage();
                                } else {
                                    ImageIcon pIcon = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png"));
                                    currentUserProfileImage = pIcon.getImage();
                                }
                            } catch (Exception imgEx) {
                                currentUserProfileImage = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png")).getImage();
                            }
                            if (currentUserProfileImage != null) {
                                Image sideImg = currentUserProfileImage.getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                                sidebarAvatarLabel.setIcon(new ImageIcon(sideImg));
                            }
                            //  ----------------------------------------------------

                            idField.setText("");
                            pwField.setText("");
                            ((CardLayout) rootContainer.getLayout()).show(rootContainer, "MAIN_APP");
                            showCard("HOME");
                        } else {
                            String msg = (res != null && res.has("message")) ? res.get("message").getAsString() : "아이디 또는 비밀번호가 일치하지 않습니다.";
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this, msg, "로그인 실패", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GeojiTalchulApp.this,
                                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해 주세요.\n(" + ex.getMessage() + ")",
                                "연결 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        
        JButton signupBtn = flatButton("회원가입 하기");
        signupBtn.addActionListener(e -> ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_SIGNUP"));
        
        btnBox.add(loginBtn);
        btnBox.add(signupBtn);
        card.add(btnBox, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }

    JPanel buildSignupPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        // 중앙 회원가입 박스
        JPanel card = roundedPanel(WHITE, 20);
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("회원가입", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        card.add(title, BorderLayout.NORTH);

        // ERD 기반 입력 폼 (필드를 변수로 선언해 나중에 값을 읽을 수 있도록)
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int r = 0;
        JTextField signupIdField    = new JTextField(15);
        JPasswordField signupPwField = new JPasswordField(15); signupPwField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JTextField signupNameField  = new JTextField(15);
        JTextField signupBirthField = new JTextField(15);
        JTextField signupPhoneField = new JTextField(15);
        JTextField signupJobField   = new JTextField(15);
        JTextField signupAddrField  = new JTextField(15);
        JTextField signupIncomeField= new JTextField(15);

        addFormRow(form, g, r++, "아이디", signupIdField);
        addFormRow(form, g, r++, "비밀번호", signupPwField);
        addFormRow(form, g, r++, "이름", signupNameField);
        addFormRow(form, g, r++, "생년월일(8자리)", signupBirthField);

        JPanel genderBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        genderBox.setOpaque(false);
        JRadioButton m = new JRadioButton("남성", true); m.setOpaque(false); m.setFont(FONT);
        JRadioButton f = new JRadioButton("여성"); f.setOpaque(false); f.setFont(FONT);
        ButtonGroup bg = new ButtonGroup(); bg.add(m); bg.add(f);
        genderBox.add(m); genderBox.add(f);
        addFormRow(form, g, r++, "성별", genderBox);

        addFormRow(form, g, r++, "전화번호", signupPhoneField);
        addFormRow(form, g, r++, "직업", signupJobField);
        addFormRow(form, g, r++, "주소", signupAddrField);
        addFormRow(form, g, r++, "월 수입(원)", signupIncomeField);

        card.add(form, BorderLayout.CENTER);

        // 하단 버튼 영역
        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton backBtn = flatButton("취소");
        backBtn.addActionListener(e -> {
            // 입력 필드 초기화 후 로그인 화면으로
            signupIdField.setText(""); signupPwField.setText("");
            signupNameField.setText(""); signupBirthField.setText("");
            signupPhoneField.setText(""); signupJobField.setText("");
            signupAddrField.setText(""); signupIncomeField.setText("");
            ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_LOGIN");
        });

        JButton submitBtn = primaryButton("가입 완료");
        submitBtn.addActionListener(e -> {
            // 1. 입력값 수집
            String loginId  = signupIdField.getText().trim();
            String password = new String(signupPwField.getPassword()).trim();
            String userName = signupNameField.getText().trim();
            String birth    = signupBirthField.getText().trim();
            String gender   = m.isSelected() ? "남" : "여";
            String phone    = signupPhoneField.getText().trim();
            String job      = signupJobField.getText().trim();
            String addr     = signupAddrField.getText().trim();
            String incomeStr= signupIncomeField.getText().trim();

            // 2. 기본 유효성 검사
            if (loginId.isEmpty() || password.isEmpty() || userName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디, 비밀번호, 이름은 필수 입력 항목입니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 생년월일: 20020804(8자리) 또는 2002-08-04(yyyy-MM-dd) 모두 허용
            String birthForApi = "";
            if (!birth.isEmpty()) {
                if (birth.matches("\\d{8}")) {
                    // 8자리 숫자 → yyyy-MM-dd 변환
                    birthForApi = birth.substring(0, 4) + "-" + birth.substring(4, 6) + "-" + birth.substring(6, 8);
                } else if (birth.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // 이미 yyyy-MM-dd 형식
                    birthForApi = birth;
                } else {
                    JOptionPane.showMessageDialog(this, "생년월일 형식이 올바르지 않습니다.\n20020804 또는 2002-08-04 형식으로 입력해 주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            final String finalBirth = birthForApi;

            int income = 0;
            if (!incomeStr.isEmpty()) {
                try { income = Integer.parseInt(incomeStr.replace(",", "")); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "월 수입은 숫자로 입력해 주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            final int finalIncome = income;
            submitBtn.setEnabled(false);
            submitBtn.setText("처리 중...");

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // 3. 아이디 중복 확인
                    JsonObject dupRes = httpGet("http://localhost:8080/api/users/check-duplicate?loginId=" + URLEncoder.encode(loginId, "UTF-8"));
                    if (dupRes != null && dupRes.has("duplicate") && dupRes.get("duplicate").getAsBoolean()) {
                        return "이미 사용 중인 아이디입니다.";
                    }

                    // 4. 회원가입 API 호출
                    JsonObject body = new JsonObject();
                    body.addProperty("loginId", loginId);
                    body.addProperty("password", password);
                    body.addProperty("userName", userName);
                    body.addProperty("birthDate", finalBirth);
                    body.addProperty("gender", gender);
                    body.addProperty("phone", phone);
                    body.addProperty("job", job);
                    body.addProperty("address", addr);
                    body.addProperty("income", finalIncome);

                    JsonObject res = httpPost("http://localhost:8080/api/users/register", body.toString());
                    if (res != null && res.has("success") && res.get("success").getAsBoolean()) {
                        return null; // 성공
                    }
                    return (res != null && res.has("message")) ? res.get("message").getAsString() : "회원가입에 실패했습니다.";
                }

                @Override
                protected void done() {
                    submitBtn.setEnabled(true);
                    submitBtn.setText("가입 완료");
                    try {
                        String errorMsg = get();
                        if (errorMsg == null) {
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this, "회원가입이 완료되었습니다!\n로그인 화면으로 이동합니다.", "가입 성공", JOptionPane.INFORMATION_MESSAGE);
                            // 입력 필드 초기화
                            signupIdField.setText(""); signupPwField.setText("");
                            signupNameField.setText(""); signupBirthField.setText("");
                            signupPhoneField.setText(""); signupJobField.setText("");
                            signupAddrField.setText(""); signupIncomeField.setText("");
                            ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_LOGIN");
                        } else {
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this, errorMsg, "회원가입 실패", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GeojiTalchulApp.this,
                                "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해 주세요.\n(" + ex.getMessage() + ")",
                                "연결 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        btnBox.add(backBtn);
        btnBox.add(submitBtn);
        card.add(btnBox, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }


    JPanel buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopbar(), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        return root;
    }

    JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(220, 0));
        side.setBackground(WHITE);
        side.setBorder(new MatteBorder(0, 0, 1, 1, BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);
        top.setBorder(new EmptyBorder(24, 22, 24, 18));
        JLabel logo = new JLabel("거지탈출");
        logo.setFont(FONT_BOLD);
        top.add(logo, BorderLayout.CENTER);
        side.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(WHITE);
        menu.setBorder(new EmptyBorder(18, 16, 18, 16));

        addNavButton(menu, "홈 (대시보드)", "HOME");
        addNavButton(menu, "달력 (일정)", "CALENDAR");
        addNavButton(menu, "통계 및 분석", "STATS");
        addNavButton(menu, "커뮤니티 (챌린지)", "COMMUNITY");
        addNavButton(menu, "포인트 상점", "STORE");

        side.add(menu, BorderLayout.CENTER);

        JPanel user = new JPanel(new BorderLayout(10, 0));
        user.setBackground(WHITE);
        user.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 16, 14, 16)));
        if (currentUserProfileImage != null) {
            Image pImg = currentUserProfileImage.getScaledInstance(36, 36, Image.SCALE_SMOOTH);
            sidebarAvatarLabel.setIcon(new ImageIcon(pImg));
        } else {
            ImageIcon pIcon = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png"));
            Image pImg = pIcon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
            sidebarAvatarLabel.setIcon(new ImageIcon(pImg));
        }
        sidebarAvatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        sidebarAvatarLabel.setOpaque(true);
        sidebarAvatarLabel.setBackground(new Color(232, 237, 241));
        sidebarAvatarLabel.setBorder(new CircleBorder(new Color(232,237,241), 28));
        sidebarAvatarLabel.setPreferredSize(new Dimension(42, 42));

        JPanel userText = new JPanel();
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.setBackground(WHITE);
        userText.add(userLabel);
        pointLabel.setForeground(GREEN_DARK);
        pointLabel.setFont(FONT_BOLD);
        userText.add(pointLabel);
        // ... (avatar, userText 세팅 코드 유지) ...
        user.add(sidebarAvatarLabel, BorderLayout.WEST);
        user.add(userText, BorderLayout.CENTER);

        //  [추가] 마우스 커서를 손가락 모양으로 바꾸고 클릭 이벤트 달기!
        user.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        user.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showUserInfoEditDialog(); // 클릭 시 정보 수정 창 팝업
            }
        });

        side.add(user, BorderLayout.SOUTH);
        return side;
    }

    //  내 정보 수정 팝업창 (사진의 부드러운 테마 적용)
    //  내 정보 수정 팝업창 (사진 변경 추가 & 크기 확장)
    void showUserInfoEditDialog() {
        JDialog dlg = new JDialog(this, "내 정보 수정", true);
        //  항목이 늘어났으니 창 크기도 넉넉하게 키워줍니다! (글자 잘림 방지)
        dlg.setSize(480, 450); 
        dlg.setLocationRelativeTo(this);

        // 팝업 배경도 우리의 감성적인 베이지색(BG)으로 통일
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG); 
        root.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 둥근 알약 느낌의 하얀색 카드 패널
        JPanel card = roundedPanel(WHITE, 40); 
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(25, 20, 25, 20)); // 안쪽 여백
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 10, 12, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int r = 0;
        
        //  1. 프로필 사진 미리보기 & 변경 버튼 영역
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        profileBox.setOpaque(false);
        
        // 거지 이미지 불러오기 (45x45 둥근 테두리 안에 쏙!)
        ImageIcon currentIcon = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png"));
        Image currentImg = currentUserProfileImage.getScaledInstance(-1, 38, Image.SCALE_SMOOTH);
        JLabel currentProfileImg = new JLabel(new ImageIcon(currentImg), SwingConstants.CENTER);
        currentProfileImg.setPreferredSize(new Dimension(48, 48));
        currentProfileImg.setBorder(new CircleBorder(BORDER, 48));
        
        JButton changePicBtn = flatButton("사진 변경");
        // 가짜 알림창 지우고, 진짜 갤러리 열어서 백엔드로 사진 쏘는 로직 장착
        changePicBtn.addActionListener(e -> {
            // 파일 선택창(갤러리) 띄우기
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일 (JPG, PNG)", "jpg", "jpeg", "png"));
            
            int result = fileChooser.showOpenDialog(dlg);
            
            // 사용자가 사진을 선택했다면?
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                try {
                    // 사진 파일을 컴퓨터가 읽을 수 있게 Base64(긴 텍스트)로 변환
                    byte[] fileContent = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    String encodedString = java.util.Base64.getEncoder().encodeToString(fileContent);

                    // 백엔드로 보낼 JSON 데이터 포장 (내 아이디 + 사진 텍스트)
                    JsonObject body = new JsonObject();
                    body.addProperty("userId", currentUserId); // 현재 로그인한 유저 ID
                    body.addProperty("profileImage", encodedString);

                    // 버튼 연속 클릭 방지
                    changePicBtn.setEnabled(false);
                    changePicBtn.setText("업로드 중...");

                    // 서버로
                    new SwingWorker<JsonObject, Void>() {
                        @Override
                        protected JsonObject doInBackground() throws Exception {
                            // 아까 뚫어둔 백엔드 API 주소로 POST 요청 날리기
                            return httpPost("http://localhost:8080/api/profile", body.toString());
                        }

                        @Override
                        protected void done() {
                            changePicBtn.setEnabled(true);
                            changePicBtn.setText("사진 변경");
                            try {
                                JsonObject res = get();
                                if (res != null && res.has("success") && res.get("success").getAsBoolean()) {
                                    JOptionPane.showMessageDialog(dlg, "프로필 사진이 성공적으로 변경되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
                                    
                                    // 6. DB에 들어갔으니, 지금 화면에 보이는 둥근 아이콘도 새 사진으로 즉시 갱신!
                                    ImageIcon newIcon = new ImageIcon(selectedFile.getAbsolutePath());
                                    Image newImg = newIcon.getImage().getScaledInstance(-1, 38, Image.SCALE_SMOOTH);
                                    currentProfileImg.setIcon(new ImageIcon(newImg));
                                    currentUserProfileImage = newIcon.getImage();
                                    Image sideImg = currentUserProfileImage.getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                                    sidebarAvatarLabel.setIcon(new ImageIcon(sideImg));
                                    
                                } else {
                                    String msg = (res != null && res.has("message")) ? res.get("message").getAsString() : "업로드 실패";
                                    JOptionPane.showMessageDialog(dlg, msg, "실패", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(dlg, "서버 연결 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "파일을 읽는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        profileBox.add(currentProfileImg);
        profileBox.add(changePicBtn);
        
        //  2. 폼 항목들 순서대로 꽂아 넣기 (중복 방지!)
        addFormRow(card, g, r++, "프로필", profileBox); 
        JTextField nameField = new JTextField(userLabel.getText().replace(" 님", ""));
        JPasswordField oldPwField = new JPasswordField(); oldPwField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JPasswordField newPwField = new JPasswordField(); newPwField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        addFormRow(card, g, r++, "닉네임", nameField);
        addFormRow(card, g, r++, "기존 비밀번호", oldPwField);
        addFormRow(card, g, r++, "새 비밀번호", newPwField);
        
        root.add(card, BorderLayout.CENTER);

        // 하단 취소 / 저장 버튼
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton cancel = flatButton("취소");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = primaryButton("정보 저장");
        save.addActionListener(e -> {
            String newName = nameField.getText().trim();
            String oldPass = new String(oldPwField.getPassword());
            String newPass = new String(newPwField.getPassword());
            
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "닉네임을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            save.setEnabled(false);
            new SwingWorker<JsonObject, Void>() {
                @Override
                protected JsonObject doInBackground() throws Exception {
                    JsonObject req = new JsonObject();
                    req.addProperty("userName", newName);
                    if (!newPass.isEmpty()) {
                        req.addProperty("oldPassword", oldPass);
                        req.addProperty("newPassword", newPass);
                    }
                    return httpPost("http://localhost:8080/api/users/" + currentUserId + "/update", req.toString());
                }
                
                @Override
                protected void done() {
                    save.setEnabled(true);
                    try {
                        JsonObject res = get();
                        if (res != null && res.has("success") && res.get("success").getAsBoolean()) {
                            JOptionPane.showMessageDialog(dlg, "회원 정보가 성공적으로 수정되었습니다.", "수정 완료", JOptionPane.INFORMATION_MESSAGE);
                            if (res.has("newName")) {
                                userLabel.setText(res.get("newName").getAsString() + " 님");
                            }
                            refreshAll();
                            dlg.dispose();
                        } else {
                            String msg = res != null && res.has("message") ? res.get("message").getAsString() : "수정 실패";
                            JOptionPane.showMessageDialog(dlg, msg, "실패", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg, "서버 연결 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });
        
        btnBox.add(cancel);
        btnBox.add(save);
        root.add(btnBox, BorderLayout.SOUTH);
        
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    void addNavButton(JPanel menu, String text, String card) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(FONT_BOLD);
        b.setForeground(MUTED);
        b.setBackground(WHITE);
        b.setBorder(new EmptyBorder(14, 14, 14, 8));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> showCard(card));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        menu.add(b);
        menu.add(Box.createVerticalStrut(4));
    }

    JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(0, 58));
        top.setBackground(WHITE);
        top.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JLabel title = new JLabel("홈 대시보드");
        title.setFont(FONT_TITLE);
        title.setBorder(new EmptyBorder(0, 28, 0, 0));
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 9));
        right.setBackground(WHITE);

        JButton friendsBtn = primaryButton("친구 관리");
        friendsBtn.addActionListener(e -> {
            if (homePanel != null) {
                homePanel.showFriendsDialog();
            }
        });

        JButton bell = new JButton("♟");
        styleIconButton(bell);
        bell.addActionListener(e -> showNotifications());

        JButton setting = primaryButton("설정");
        setting.addActionListener(e -> showSettings());

        JButton logout = flatButton("로그아웃");
        logout.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(content, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_LOGIN");
            }
        });

        right.add(friendsBtn);
        right.add(bell);
        right.add(setting);
        right.add(logout);
        right.setBorder(new EmptyBorder(0, 0, 0, 20));
        top.add(right, BorderLayout.EAST);
        return top;
    }

    void styleIconButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(WHITE);
        b.setForeground(MUTED);
        b.setBorder(new CircleBorder(BORDER, 38));
        b.setPreferredSize(new Dimension(40, 40));
        b.setFocusPainted(false);
    }

    void showCard(String name) {
        ((CardLayout) content.getLayout()).show(content, name);
        String title = "";
        switch (name) {
            case "HOME": 
                title = "홈 대시보드"; 
                break;
            case "CALENDAR": 
                title = "달력"; 
                break;
            case "STATS": 
                title = "통계 및 분석"; 
                break;
            case "COMMUNITY": 
                title = "커뮤니티 (챌린지)"; 
                break;
            case "STORE": 
                title = "마이 / 포인트 상점"; 
                break;
            default: 
                title = ""; 
                break;
        }
        // Topbar title is the first component in its center/left area.
        JPanel main = (JPanel) content.getParent();
        JPanel top = (JPanel) main.getComponent(0);
        JLabel label = (JLabel) top.getComponent(0);
        label.setText(title);
        refreshAll();
    }

    void refreshAll() {
        pointLabel.setText("  ● " + state.points + " P");
        homePanel.refresh();
        calendarPanel.refresh();
        statisticsPanel.refresh();
        communityPanel.refresh();
        myStorePanel.refresh();
        content.revalidate();
        content.repaint();
    }

    void showNotifications() {
        String msg = state.fixedExpenseCandidateCount() > 0
                ? "최근 3개월 반복 지출 후보가 " + state.fixedExpenseCandidateCount() + "건 발견되었습니다."
                : state.budgetUsage() >= 1.0
                ? "예산을 초과했습니다.\n지출 내역을 확인해 보세요."
                : "새로운 알림이 없습니다.";
        JOptionPane.showMessageDialog(this, msg, "알림", JOptionPane.INFORMATION_MESSAGE);
    }

    void showSettings() {
        // 목표 예산은 홈 화면에 별도 입력칸을 만들지 않고
        // 기존 설정 버튼을 통해 변경하도록 구성한다.
        JDialog dlg = new JDialog(this, "앱 설정", true);
        dlg.setSize(430, 330);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(WHITE);
        root.setBorder(new EmptyBorder(24, 24, 20, 24));

        JLabel title = new JLabel("앱 설정");
        title.setFont(BASE_FONT.deriveFont(Font.BOLD, 22f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel budgetRow = new JPanel(new BorderLayout(10, 0));
        budgetRow.setBackground(WHITE);
        budgetRow.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel budgetTitle = new JLabel("목표 예산");
        budgetTitle.setFont(FONT_BOLD);
        JLabel budgetHint = new JLabel("이번 달 소비 목표 금액");
        budgetHint.setForeground(MUTED);
        JPanel budgetText = new JPanel();
        budgetText.setBackground(WHITE);
        budgetText.setLayout(new BoxLayout(budgetText, BoxLayout.Y_AXIS));
        budgetText.add(budgetTitle);
        budgetText.add(Box.createVerticalStrut(3));
        budgetText.add(budgetHint);

        JTextField budgetField = new JTextField(String.valueOf(state.budget));
        budgetField.setFont(FONT_BOLD);
        budgetField.setHorizontalAlignment(SwingConstants.RIGHT);
        budgetField.setPreferredSize(new Dimension(150, 38));
        budgetField.setMaximumSize(new Dimension(150, 38));
        budgetRow.add(budgetText, BorderLayout.CENTER);
        budgetRow.add(budgetField, BorderLayout.EAST);
        form.add(budgetRow);

        JLabel budgetInfo = new JLabel("현재 목표 예산: " + won(state.budget));
        budgetInfo.setForeground(GREEN_DARK);
        budgetInfo.setBorder(new EmptyBorder(0, 0, 16, 0));
        form.add(budgetInfo);

        JCheckBox push = new JCheckBox("예산 초과 알림", state.alertThreshold <= 100);
        JCheckBox fixed = new JCheckBox("고정지출 자동 알림", true);
        push.setBackground(WHITE);
        fixed.setBackground(WHITE);
        form.add(push);
        form.add(Box.createVerticalStrut(6));
        form.add(fixed);

        root.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setBackground(WHITE);
        JButton cancel = flatButton("취소");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = primaryButton("저장");
        // 설정 저장
        save.addActionListener(e -> {
            String raw = budgetField.getText().replace(",", "").trim();
            try {
                long newBudget = Long.parseLong(raw);
                if (newBudget <= 0) throw new NumberFormatException();
                
                int newThreshold = push.isSelected() ? 80 : 999;
                
                save.setEnabled(false);
                save.setText("저장 중...");
                new SwingWorker<JsonObject, Void>() {
                    @Override
                    protected JsonObject doInBackground() throws Exception {
                        JsonObject body = new JsonObject();
                        body.addProperty("userId", currentUserId);
                        body.addProperty("limitAmount", newBudget);
                        body.addProperty("budgetScope", "TOTAL");
                        httpPost("http://localhost:8080/api/budgets/total", body.toString()); // 예산 업데이트 API
                        
                        JsonObject settingBody = new JsonObject();
                        settingBody.addProperty("userId", currentUserId);
                        settingBody.addProperty("alertThreshold", newThreshold);
                        settingBody.addProperty("alertWeekday", fixed.isSelected() ? "월" : ""); settingBody.addProperty("currentSkin", state.currentSkin);
                        return httpPost("http://localhost:8080/api/settings", settingBody.toString()); // 알림 설정 API
                    }
                    @Override
                    protected void done() {
                        state.budget = newBudget;
                        state.alertThreshold = newThreshold;
                        dlg.dispose();
                        refreshAll();
                        JOptionPane.showMessageDialog(GeojiTalchulApp.this, "설정이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
                    }
                }.execute();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "목표 예산은 0보다 큰 숫자로 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                budgetField.requestFocus();
                budgetField.selectAll();
            }
        });

        bottom.add(cancel);
        bottom.add(save);
        root.add(bottom, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ---------- HOME ----------
    class HomePanel extends JPanel {
        JLabel remainLabel = new JLabel();
        JLabel budgetLabel = new JLabel();
        JLabel spentLabel = new JLabel();
        JLabel alertLabel = new JLabel();
        JPanel recentList = new JPanel();
        PieChart homePie = new PieChart();
        JPanel alertBanner;
        JLabel homeCharacter; 
        // friendId -> friendUserName 매핑 (DB에서 불러온 데이터)
        Map<Integer, String> friendMap = new java.util.LinkedHashMap<>();

        HomePanel() {
            setLayout(new BorderLayout(0, 20)); 
            setBackground(BG);
            setBorder(new EmptyBorder(30, 30, 30, 30)); 

            alertBanner = buildAlertBanner();
            add(alertBanner, BorderLayout.NORTH);

            // 2. 메인 대시보드 뼈대 (위 1줄, 아래 1줄)
            JPanel centerGrid = new JPanel(new GridLayout(2, 1, 0, 20)); // 위아래 간격 20px
            centerGrid.setOpaque(false);

            //  [첫 번째 줄] 예산 카드 (넓게) + 간편 등록 (고정 너비 400px)
            JPanel topRow = new JPanel(new BorderLayout(20, 0)); // 좌우 카드 간격 20px
            topRow.setOpaque(false);
            topRow.add(buildBudgetCard(), BorderLayout.CENTER); 
            
            JPanel quickWrapper = new JPanel(new BorderLayout());
            quickWrapper.setOpaque(false);
            quickWrapper.add(buildQuickExpenseCard(), BorderLayout.CENTER);
            quickWrapper.setPreferredSize(new Dimension(400, 0)); // 간편 등록이 무식하게 커지지 않게 방어
            topRow.add(quickWrapper, BorderLayout.EAST);

            //  [두 번째 줄] 파이 차트 (고정 너비 450px) + 최근 지출 (넓게)
            JPanel bottomRow = new JPanel(new BorderLayout(20, 0));
            bottomRow.setOpaque(false);
            
            JPanel pieWrapper = new JPanel(new BorderLayout());
            pieWrapper.setOpaque(false);
            pieWrapper.add(buildHomePieCard(), BorderLayout.CENTER);
            pieWrapper.setPreferredSize(new Dimension(450, 0)); // 파이 차트 크기 방어
            bottomRow.add(pieWrapper, BorderLayout.WEST);
            
            bottomRow.add(buildRecentCard(), BorderLayout.CENTER);

            centerGrid.add(topRow);
            centerGrid.add(bottomRow);

            add(centerGrid, BorderLayout.CENTER);
        }
        
        void showFriendsDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "친구 관리", true);
            d.setSize(450, 550);
            d.setLocationRelativeTo(this);
            d.getContentPane().setBackground(BG);
            
            JTabbedPane tabs = new JTabbedPane();
            
            // --- 첫번째 탭: 내 친구 목록 ---
            JPanel myFriendsTab = new JPanel(new BorderLayout(0, 15));
            myFriendsTab.setOpaque(false);
            myFriendsTab.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JPanel addBox = new JPanel(new BorderLayout(5, 0));
            addBox.setOpaque(false);
            JTextField addTf = new JTextField(10);
            addTf.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
            addTf.setToolTipText("친구의 로그인 아이디를 입력하세요");
            JButton addBtn = primaryButton("친구신청");
            addBtn.setPreferredSize(new Dimension(95, 32));
            addBtn.setMargin(new Insets(2, 4, 2, 4));
            addBox.add(addTf, BorderLayout.CENTER);
            addBox.add(addBtn, BorderLayout.EAST);
            
            myFriendsTab.add(addBox, BorderLayout.NORTH);
            
            JPanel listContainer = new JPanel();
            listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
            listContainer.setBackground(WHITE);
            JScrollPane scroll = new JScrollPane(listContainer);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            myFriendsTab.add(scroll, BorderLayout.CENTER);
            
            // --- 두번째 탭: 받은 요청 ---
            JPanel requestsTab = new JPanel(new BorderLayout(0, 15));
            requestsTab.setOpaque(false);
            requestsTab.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JPanel reqContainer = new JPanel();
            reqContainer.setLayout(new BoxLayout(reqContainer, BoxLayout.Y_AXIS));
            reqContainer.setBackground(WHITE);
            JScrollPane reqScroll = new JScrollPane(reqContainer);
            reqScroll.setBorder(BorderFactory.createLineBorder(BORDER));
            reqScroll.getVerticalScrollBar().setUnitIncrement(16);
            requestsTab.add(reqScroll, BorderLayout.CENTER);
            
            tabs.addTab("내 친구", myFriendsTab);
            tabs.addTab("받은 요청", requestsTab);
            d.add(tabs);

            Runnable[] loadData = new Runnable[1];
            loadData[0] = () -> {
                new SwingWorker<Void, Void>() {
                    com.google.gson.JsonArray reqArr = new com.google.gson.JsonArray();
                    @Override
                    protected Void doInBackground() throws Exception {
                        com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/friends?userId=" + currentUserId);
                        if (el != null && el.isJsonArray()) {
                            friendMap.clear();
                            for (com.google.gson.JsonElement item : el.getAsJsonArray()) {
                                com.google.gson.JsonObject obj = item.getAsJsonObject();
                                int fId = obj.has("friendId") ? obj.get("friendId").getAsInt() : -1;
                                String fName = obj.has("friendUserName") ? obj.get("friendUserName").getAsString() : "알수없음";
                                if (fId >= 0) friendMap.put(fId, fName);
                            }
                        }
                        com.google.gson.JsonElement reqEl = httpGetElement("http://localhost:8080/api/friends/requests?userId=" + currentUserId);
                        if (reqEl != null && reqEl.isJsonArray()) {
                            reqArr = reqEl.getAsJsonArray();
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        // 1. 내 친구 렌더링
                        listContainer.removeAll();
                        if (friendMap.isEmpty()) {
                            JLabel empty = new JLabel("등록된 친구가 없습니다.");
                            empty.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                            empty.setForeground(MUTED);
                            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
                            listContainer.add(empty);
                        } else {
                            for (Map.Entry<Integer, String> entry : friendMap.entrySet()) {
                                int fId = entry.getKey();
                                String fName = entry.getValue();
                                JPanel row = new JPanel(new BorderLayout());
                                row.setBackground(WHITE);
                                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                                row.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                                    new EmptyBorder(10, 15, 10, 15)
                                ));
                                JLabel name = new JLabel(fName);
                                name.setFont(BASE_FONT.deriveFont(Font.BOLD, 15f));
                                JButton del = new JButton("삭제");
                                del.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
                                del.setBackground(new Color(255, 235, 235));
                                del.setForeground(RED);
                                del.setBorder(new EmptyBorder(4, 10, 4, 10));
                                del.setFocusPainted(false);
                                del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                del.addActionListener(ev -> {
                                    int res = JOptionPane.showConfirmDialog(d, "'" + fName + "' 님을 친구 목록에서 삭제하시겠습니까?", "친구 삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                    if (res == JOptionPane.YES_OPTION) {
                                        del.setEnabled(false);
                                        new SwingWorker<Boolean, Void>() {
                                            @Override
                                            protected Boolean doInBackground() throws Exception {
                                                com.google.gson.JsonObject res = httpDelete("http://localhost:8080/api/friends?friendId=" + fId);
                                                return res != null && res.has("success") && res.get("success").getAsBoolean();
                                            }
                                            @Override
                                            protected void done() {
                                                loadData[0].run();
                                            }
                                        }.execute();
                                    }
                                });
                                row.add(name, BorderLayout.WEST);
                                row.add(del, BorderLayout.EAST);
                                listContainer.add(row);
                            }
                        }
                        listContainer.revalidate();
                        listContainer.repaint();
                        
                        // 2. 받은 요청 렌더링
                        reqContainer.removeAll();
                        if (reqArr.size() == 0) {
                            tabs.setTitleAt(1, "받은 요청");
                            JLabel empty = new JLabel("받은 요청이 없습니다.");
                            empty.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                            empty.setForeground(MUTED);
                            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
                            reqContainer.add(empty);
                        } else {
                            tabs.setTitleAt(1, "받은 요청 (" + reqArr.size() + ")");
                            for (com.google.gson.JsonElement item : reqArr) {
                                com.google.gson.JsonObject obj = item.getAsJsonObject();
                                int fId = obj.has("friendId") ? obj.get("friendId").getAsInt() : -1;
                                int reqUserId = obj.has("userId") ? obj.get("userId").getAsInt() : -1;
                                String fName = obj.has("friendUserName") ? obj.get("friendUserName").getAsString() : "알수없음";
                                
                                JPanel row = new JPanel(new BorderLayout());
                                row.setBackground(WHITE);
                                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                                row.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                                    new EmptyBorder(10, 15, 10, 15)
                                ));
                                JLabel name = new JLabel(fName);
                                name.setFont(BASE_FONT.deriveFont(Font.BOLD, 15f));
                                
                                JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                                btnBox.setOpaque(false);
                                
                                JButton acceptBtn = new JButton("수락");
                                acceptBtn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
                                acceptBtn.setBackground(new Color(230, 245, 238));
                                acceptBtn.setForeground(GREEN_DARK);
                                acceptBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
                                acceptBtn.setFocusPainted(false);
                                acceptBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                
                                JButton rejectBtn = new JButton("거절");
                                rejectBtn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
                                rejectBtn.setBackground(new Color(255, 235, 235));
                                rejectBtn.setForeground(RED);
                                rejectBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
                                rejectBtn.setFocusPainted(false);
                                rejectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                
                                acceptBtn.addActionListener(ev -> {
                                    acceptBtn.setEnabled(false);
                                    rejectBtn.setEnabled(false);
                                    new SwingWorker<Boolean, Void>() {
                                        @Override
                                        protected Boolean doInBackground() throws Exception {
                                            com.google.gson.JsonObject res = httpPost(
                                                "http://localhost:8080/api/friends/accept?friendId=" + fId + "&myUserId=" + currentUserId + "&requesterUserId=" + reqUserId, "");
                                            return res != null && res.has("success") && res.get("success").getAsBoolean();
                                        }
                                        @Override
                                        protected void done() {
                                            loadData[0].run();
                                            JOptionPane.showMessageDialog(d, fName + " 님의 친구 요청을 수락했습니다.");
                                        }
                                    }.execute();
                                });
                                
                                rejectBtn.addActionListener(ev -> {
                                    acceptBtn.setEnabled(false);
                                    rejectBtn.setEnabled(false);
                                    new SwingWorker<Boolean, Void>() {
                                        @Override
                                        protected Boolean doInBackground() throws Exception {
                                            com.google.gson.JsonObject res = httpDelete("http://localhost:8080/api/friends?friendId=" + fId);
                                            return res != null && res.has("success") && res.get("success").getAsBoolean();
                                        }
                                        @Override
                                        protected void done() {
                                            loadData[0].run();
                                        }
                                    }.execute();
                                });
                                
                                btnBox.add(acceptBtn);
                                btnBox.add(rejectBtn);
                                row.add(name, BorderLayout.WEST);
                                row.add(btnBox, BorderLayout.EAST);
                                reqContainer.add(row);
                            }
                        }
                        reqContainer.revalidate();
                        reqContainer.repaint();
                    }
                }.execute();
            };
            
            addBtn.addActionListener(e -> {
                String loginId = addTf.getText().trim();
                if (loginId.isEmpty()) return;
                if (loginId.equals(userLabel.getText().replace(" 님", ""))) {
                    JOptionPane.showMessageDialog(d, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
                    return;
                }
                addBtn.setEnabled(false);
                addBtn.setText("요청 중");
                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/friends/search?loginId=" + URLEncoder.encode(loginId, "UTF-8"));
                        if (el == null || !el.isJsonObject()) return "존재하지 않는 아이디입니다.";
                        com.google.gson.JsonObject res = el.getAsJsonObject();
                        if (!res.has("userId")) return "존재하지 않는 아이디입니다.";
                        int targetUserId = res.get("userId").getAsInt();
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("userId", currentUserId);
                        body.addProperty("friendUserId", targetUserId);
                        com.google.gson.JsonObject addRes = httpPost("http://localhost:8080/api/friends", body.toString());
                        if (addRes != null && addRes.has("success") && addRes.get("success").getAsBoolean()) {
                            return null;
                        }
                        return "친구 요청에 실패했습니다. 이미 요청했거나 친구 상태입니다.";
                    }
                    @Override
                    protected void done() {
                        addBtn.setEnabled(true);
                        addBtn.setText("친구요청");
                        try {
                            String err = get();
                            if (err == null) {
                                addTf.setText("");
                                JOptionPane.showMessageDialog(d, loginId + " 님에게 친구 요청을 보냈습니다!");
                            } else {
                                JOptionPane.showMessageDialog(d, err, "요청 실패", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(d, "서버 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            });
            
            loadData[0].run();
            d.setVisible(true);
        }

        JPanel buildAlertBanner() {
            //  직각 JPanel 대신 토스 감성 둥근 패널(알약 쉐입)로 교체!
            JPanel banner = new RoundedPanel(RED, 40); 
            banner.setLayout(new BorderLayout());
            banner.setBorder(new EmptyBorder(15, 25, 15, 25)); // 넉넉한 여백
            
            alertLabel.setFont(FONT_BOLD);
            banner.add(alertLabel, BorderLayout.CENTER);

            JButton detail = new JButton("상세 보기");
            detail.setFocusPainted(false);
            //  여기서도 setBorder 삭제!
            detail.setBackground(WHITE);
            detail.setForeground(RED);
            detail.addActionListener(e -> {
                boolean activeFixedAlert = state.fixedExpenseCandidateCount() > 0 && !state.fixedCandidateKey().equals(dismissedFixedCandidateKey);
                if (activeFixedAlert) {
                    showFixedExpenseCandidateDialog();
                } else if (state.budgetUsage() >= 1.0 || state.budgetUsage() >= state.alertThreshold / 100.0) {
                    showCard("STATS");
                }
            });
            banner.add(detail, BorderLayout.EAST);
            return banner;
        }

        JPanel buildHomePieCard() {
            JPanel card = roundedPanel(WHITE, 60);
            card.setLayout(new BorderLayout());

            card.add(homePie, BorderLayout.CENTER);

            return card;
        }

        void showFixedExpenseCandidateDialog() {
            List<Expense> candidates = state.fixedCandidates();
            if (candidates.isEmpty()) {
                dismissedFixedCandidateKey = state.fixedCandidateKey();
                refreshAll();
                return;
            }

            JDialog dlg = new JDialog(GeojiTalchulApp.this, "고정 지출 후보 확인", true);
            dlg.setSize(620, 520);
            dlg.setLocationRelativeTo(GeojiTalchulApp.this);

            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(new EmptyBorder(20, 20, 20, 20));
            root.setBackground(WHITE);

            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("최근 3개월 반복 지출 후보");
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 22f));
            JLabel desc = new JLabel("같은 중분류와 같은 금액이 반복되어 고정 지출로 추정된 항목입니다.");
            desc.setForeground(MUTED);
            desc.setBorder(new EmptyBorder(6, 0, 10, 0));
            header.add(title);
            header.add(desc);
            root.add(header, BorderLayout.NORTH);

            JPanel list = new JPanel();
            list.setBackground(WHITE);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

            for (Expense e : candidates) {
                long repeatCount = state.fixedCandidateRepeatCount(e);
                JPanel row = new JPanel(new BorderLayout(12, 0));
                row.setBackground(WHITE);
                row.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER),
                        new EmptyBorder(14, 8, 14, 8)));

                JPanel info = new JPanel();
                info.setOpaque(false);
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                JLabel name = new JLabel(esc(e.item));
                name.setFont(FONT_BOLD);
                JLabel category = new JLabel(e.large + " > " + e.medium + " > " + e.small);
                category.setForeground(MUTED);
                JLabel repeat = new JLabel("최근 3개월 내 " + repeatCount + "회 반복 · 자동 추정 후보");
                repeat.setForeground(GREEN_DARK);
                info.add(name);
                info.add(Box.createVerticalStrut(4));
                info.add(category);
                info.add(Box.createVerticalStrut(3));
                info.add(repeat);

                JLabel amount = new JLabel(won(e.amount));
                amount.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
                amount.setForeground(RED);
                row.add(info, BorderLayout.CENTER);
                row.add(amount, BorderLayout.EAST);
                list.add(row);
            }

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            root.add(scroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            JLabel hint = new JLabel("확인하면 홈 화면의 이 알림은 사라집니다. 고정 지출 여부는 마이 / 고정지출 관리에서 직접 설정할 수 있습니다.");
            hint.setForeground(MUTED);
            JButton confirm = primaryButton("확인");
            confirm.addActionListener(e -> {
                dismissedFixedCandidateKey = state.fixedCandidateKey();
                dlg.dispose();
                refreshAll();
            });
            bottom.add(hint, BorderLayout.CENTER);
            bottom.add(confirm, BorderLayout.EAST);
            root.add(bottom, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setVisible(true);
        }

        JPanel buildBudgetCard() {
            JPanel card = roundedPanel(GREEN, 18);
            card.setPreferredSize(new Dimension(0, 235));
            card.setLayout(new BorderLayout());

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(new EmptyBorder(42, 34, 30, 20));

            JLabel title = new JLabel("이번 달 소비 금액");
            title.setForeground(WHITE);
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));

            // 소비 금액을 가장 크게 표시하고, 아래에 목표 예산에서 차감한 잔여 금액을 표시
            spentLabel.setForeground(WHITE);
            spentLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 45f));
            remainLabel.setForeground(new Color(240, 245, 234));
            remainLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 21f));
            budgetLabel.setForeground(new Color(224, 235, 211));
            budgetLabel.setFont(FONT_BOLD);

            left.add(title);
            left.add(Box.createVerticalStrut(2));
            left.add(spentLabel);
            left.add(Box.createVerticalStrut(10));
            left.add(remainLabel);
            left.add(Box.createVerticalStrut(7));
            left.add(budgetLabel);

            card.add(left, BorderLayout.WEST);

            //  [우] 내 캐릭터 및 말풍선 영역
            JPanel rightBox = new JPanel(new BorderLayout());
            rightBox.setOpaque(false);
            rightBox.setPreferredSize(new Dimension(280, 235));

            //  1. 꼬리가 달린 말풍선 패널 생성
            JPanel bubblePanel = new SpeechBubblePanel(WHITE, 30);
            bubblePanel.setLayout(new BorderLayout());
            // 말풍선 꼬리(12px) 공간을 위해 아래쪽 여백을 22로 늘림
            bubblePanel.setBorder(new EmptyBorder(10, 15, 22, 15)); 
            
            javax.swing.JTextPane bubbleText = new javax.swing.JTextPane();
            bubbleText.setOpaque(false);
            bubbleText.setEditable(false);
            bubbleText.setFocusable(false);
            bubbleText.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
            bubbleText.setForeground(TEXT);
            
            // 텍스트 가운데 정렬
            javax.swing.text.StyledDocument doc = bubbleText.getStyledDocument();
            javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
            javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            
            // 텍스트가 양옆으로 너무 늘어나지 않게 좌우 패딩을 주어 강제 줄바꿈 유도
            JPanel textWrapper = new JPanel(new BorderLayout());
            textWrapper.setOpaque(false);
            textWrapper.setBorder(new EmptyBorder(0, 30, 0, 30));
            textWrapper.add(bubbleText, BorderLayout.CENTER);
            
            bubblePanel.add(textWrapper, BorderLayout.CENTER);
            bubblePanel.setVisible(false);

            //  [핵심 부분] 말풍선이 나타날 때 캐릭터가 안 밀리게 함!
            JPanel bubbleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            bubbleWrapper.setOpaque(false);
            //  빈 공간을 확보해서 말풍선이 나타나도 세로 80px을 미리 차지하게 합니다.
            bubbleWrapper.setPreferredSize(new Dimension(280, 80)); 
            bubbleWrapper.add(bubblePanel);
            rightBox.add(bubbleWrapper, BorderLayout.NORTH);

            //  2. 현재 선택된 스킨 이미지
            homeCharacter = new JLabel();
            homeCharacter.setHorizontalAlignment(SwingConstants.CENTER);
            homeCharacter.setPreferredSize(new Dimension(140, 140));
            homeCharacter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            homeCharacter.setIcon(loadSkinIcon(state.currentSkin, 140, 140));

            //  3. 거지 찰진 랜덤 대사 목록
            String[] quotes = {
                "오늘 점심은 삼각김밥이다...",
                "숨만 쉬어도 돈이 나가네...",
                "이러다간 진짜 길바닥 나앉아!",
                "내 지갑은 양파 같아... 열 때마다 눈물이 나거든.",
                "물배 채우는 것도 하루 이틀이지, 이러다 영양실조 걸리겠어....",
                "누가 길가다 만 원짜리 하나 안 떨어뜨리나 바닥만 보고 걷는다니까.",
                "로또 4등이라도 당첨되면 소원이 없겠네, 진짜.",
                "이번 달 월급은 통장에 로그인했다가 흔적도 없이 로그아웃했어!",
                "광합성으로 배를 채울 수 있으면 얼마나 좋을까...",
                "통장 잔고가 내 시력보다 더 떨어졌어....",
                "내일은 동네 박스 줍는 할아버지랑 구역 경쟁이라도 해야 할 판이야...",
                "누가 나 좀 안 주워가나? 밥만 주면 집 진짜 잘 지키는데."
            };

            // 말풍선 사라지는 타이머를 담을 변수
            final javax.swing.Timer[] hideTimer = {null};

            //  4. 캐릭터 클릭 시 대사 띄우기!
            homeCharacter.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 패딩 영역 클릭 방지
                    int margin = 8;
                    if (e.getX() < margin || e.getX() > homeCharacter.getWidth() - margin || e.getY() < margin
                            || e.getY() > homeCharacter.getHeight() - margin) {
                        return;
                    }

                    // 랜덤으로 멘트 뽑아서 말풍선에 꽂기
                    int r = (int)(Math.random() * quotes.length);
                    bubbleText.setText(quotes[r]);
                    // JTextPane은 setText 후 정렬이 풀릴 수 있으므로 다시 적용
                    doc.setParagraphAttributes(0, doc.getLength(), center, false);
                    bubblePanel.setVisible(true); // 말풍선 뿅!
                    
                    // 폭풍 광클(연타) 시 기존 타이머 끄고 새로 2.5초 리셋
                    if (hideTimer[0] != null && hideTimer[0].isRunning()) {
                        hideTimer[0].stop(); 
                    }
                    
                    hideTimer[0] = new javax.swing.Timer(2500, evt -> {
                        bubblePanel.setVisible(false); // 2.5초 뒤 스르륵 숨김
                    });
                    hideTimer[0].setRepeats(false); // 한 번만 실행
                    hideTimer[0].start();
                }
            });

            rightBox.add(homeCharacter, BorderLayout.CENTER);

            //  5. 사진 아래 스킨 설정 버튼
            JButton skinButton = flatButton("스킨 설정");
            skinButton.setForeground(GREEN_DARK);
            skinButton.addActionListener(e -> showSkinSelectionDialog());
            JPanel skinButtonBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            skinButtonBox.setOpaque(false);
            skinButtonBox.setBorder(new EmptyBorder(0, 0, 4, 0));
            skinButtonBox.add(skinButton);
            rightBox.add(skinButtonBox, BorderLayout.SOUTH);

            card.add(rightBox, BorderLayout.EAST);

            return card;
        }

        JPanel buildQuickExpenseCard() {
            JPanel card = roundedPanel(WHITE, 18);
            card.setPreferredSize(new Dimension(355, 235));
            card.setLayout(new BorderLayout());
            JLabel title = new JLabel("지출 간편 등록");
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            title.setBorder(new EmptyBorder(22, 22, 10, 10));
            card.add(title, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
            grid.setBackground(WHITE);
            grid.setBorder(new EmptyBorder(4, 22, 22, 22));

            addQuickButton(grid, "카페", "식비", "카페/간식");
            addQuickButton(grid, "식비", "식비", "외식비");
            addQuickButton(grid, "교통", "교통/차량", "대중교통");
            JButton detail = new JButton("+\n상세 입력");
            detail.setFont(FONT_BOLD);
            detail.setForeground(WHITE); // 하얀색 글씨
            detail.setBackground(GREEN_PALE); //  칙칙한 회색 대신 예쁜 뮤트 그린 컬러 적용!
            detail.setFocusPainted(false);
            detail.addActionListener(e -> openExpenseDialog(null, null));
            grid.add(detail);
            card.add(grid, BorderLayout.CENTER);
            return card;
        }

        void addQuickButton(JPanel grid, String text, String large, String medium) {
            JButton b = new JButton(text);
            b.setFont(FONT_BOLD);
            b.setForeground(NAVY); //  글씨는 또렷한 네이비/다크그린
            
            //  배경을 칙칙한 흰색 대신, 테마와 어울리는 아주 연하고 부드러운 민트 베이지 톤으로 변경
            b.setBackground(new Color(238, 244, 241)); 
            
            b.setFocusPainted(false);
            b.addActionListener(e -> openExpenseDialog(large, medium));
            grid.add(b);
        }

        void addQuickButton(JPanel p, String text) {
            JButton b = new JButton(text);
            b.setFont(FONT_BOLD);
            
            //  배경을 칙칙한 흰색 대신, 테마와 어울리는 아주 연하고 부드러운 민트 베이지 톤으로 변경
            b.setBackground(new Color(238, 244, 241)); 
            b.setForeground(NAVY); // 글씨는 또렷한 네이비/다크그린
            b.setFocusPainted(false);
            
            // b.setBorder(...)가 혹시 있다면 무조건 삭제!
            p.add(b);
        }

        JPanel buildRecentCard() {
            JPanel card = roundedPanel(WHITE, 18);
            card.setLayout(new BorderLayout());
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel t = new JLabel("최근 지출 내역");
            t.setFont(BASE_FONT.deriveFont(Font.BOLD, 19f));
            JButton all = flatButton("전체 보기");
            all.addActionListener(e -> showCard("STATS"));
            head.setBorder(new EmptyBorder(22, 22, 10, 22));
            head.add(t, BorderLayout.WEST);
            head.add(all, BorderLayout.EAST);
            card.add(head, BorderLayout.NORTH);

            recentList.setBackground(WHITE);
            recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
            javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(recentList); scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.getVerticalScrollBar().setUnitIncrement(16); card.add(scroll, java.awt.BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            if (homeCharacter != null) {
                homeCharacter.setIcon(loadSkinIcon(state.currentSkin, 140, 140));
            }

            long spent = state.totalSpent();
            long remain = state.budget - spent;
            spentLabel.setText(won(spent));
            remainLabel.setText("목표 예산까지: " + won(remain));
            budgetLabel.setText("목표 예산: " + won(state.budget));

            double usage = state.budget == 0 ? 0 : spent / (double) state.budget;
            boolean activeBudgetAlert = usage >= state.alertThreshold / 100.0;
            boolean activeFixedAlert = state.fixedExpenseCandidateCount() > 0
                    && !state.fixedCandidateKey().equals(dismissedFixedCandidateKey);

            if (activeFixedAlert) {
                alertLabel.setText("●  고정 지출 후보 " + state.fixedExpenseCandidateCount() + "건을 발견했습니다. 확인해 보세요.");
            } else if (usage >= 1) {
                alertLabel.setText("이번 달 예산을 초과했습니다. 남은 기간 동안 지출을 줄이세요.");
            } else if (activeBudgetAlert) {
                alertLabel.setText("비용비용! 이번 달 예산 " + (int)(usage * 100) + "% 사용. 남은 기간 동안 지출을 줄이세요.");
            } else {
                alertLabel.setText("알림 확인 완료");
            }

            // 고정지출 후보 알림을 확인한 뒤에는 홈 배경색과 동일하게 만들어 눈에 띄지 않게 처리
            if (alertBanner != null) {
                boolean visibleAlert = activeBudgetAlert || activeFixedAlert;
                alertBanner.setVisible(visibleAlert);
                alertLabel.setForeground(visibleAlert ? WHITE : BG);
                Component east = alertBanner.getComponentCount() > 1 ? alertBanner.getComponent(1) : null;
                if (east instanceof JButton) {
                    JButton b = (JButton) east;
                    b.setForeground(visibleAlert ? WHITE : BG);
                    b.setBackground(visibleAlert ? RED : BG);
                }
            }

            recentList.removeAll();
            List<Expense> list = state.expenses.stream()
                    .sorted(Comparator.comparing((Expense x) -> x.date).reversed())
                    .limit(30).collect(Collectors.toList());
            for (Expense e : list) recentList.add(expenseRow(e));
            recentList.revalidate();
            recentList.repaint();
            homePie.repaint();
        }

        JPanel expenseRow(Expense e) {
            // 최근 지출 내역: 분류와 소분류를 두 줄로 표시
            // 예) 08.15 (토)   식비 > 배달음식
            //                  버거킹
            JPanel row = new JPanel(new BorderLayout(15, 0));
            row.setBackground(WHITE);
            row.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(242,244,246)),
                    new EmptyBorder(12, 22, 12, 22)));

            JLabel date = new JLabel(
                    e.date.format(DateTimeFormatter.ofPattern("MM. dd (E)", Locale.KOREAN)));
            date.setPreferredSize(new Dimension(120, 52));
            date.setVerticalAlignment(SwingConstants.TOP);
            date.setForeground(MUTED);

            JLabel cat = new JLabel(e.large + " > " + e.medium);
            cat.setForeground(MUTED);
            cat.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));

            JLabel item = new JLabel(
                    e.item == null || e.item.trim().isEmpty() ? "(내용 없음)" : e.item);
            item.setFont(FONT_BOLD);
            item.setForeground(TEXT);

            JPanel categoryBox = new JPanel();
            categoryBox.setOpaque(false);
            categoryBox.setLayout(new BoxLayout(categoryBox, BoxLayout.Y_AXIS));
            categoryBox.add(cat);
            categoryBox.add(Box.createVerticalStrut(3));
            categoryBox.add(item);

            JLabel amount = new JLabel("-" + won(e.amount));
            amount.setForeground(RED);
            amount.setFont(FONT_BOLD);
            amount.setVerticalAlignment(SwingConstants.TOP);

            row.add(date, BorderLayout.WEST);
            row.add(categoryBox, BorderLayout.CENTER);
            row.add(amount, BorderLayout.EAST);
            
            // 더블 클릭 시 상세 정보 창 띄우기
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent ev) {
                    if (ev.getClickCount() == 2) {
                        statisticsPanel.showExpenseDetail(e);
                    }
                }
            });
            
            return row;
        }
    }

    // ---------- CALENDAR ----------
    class CalendarPanel extends JPanel {
        // YearMonth month = YearMonth.now();
        YearMonth month = YearMonth.of(2026, 8);
        JPanel grid = new JPanel(new GridLayout(0, 7, 6, 6));
        JLabel monthLabel = new JLabel();

        CalendarPanel() {
            setLayout(new BorderLayout(0, 15));
            setBackground(BG);
            setBorder(new EmptyBorder(28, 28, 28, 28));
            add(buildCalendarHeader(), BorderLayout.NORTH);
            add(buildCalendarCard(), BorderLayout.CENTER);
        }

        JPanel buildCalendarHeader() {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            JButton prev = navButton("‹");
            JButton next = navButton("›");
            prev.addActionListener(e -> { month = month.minusMonths(1); refresh(); });
            next.addActionListener(e -> { month = month.plusMonths(1); refresh(); });
            monthLabel.setFont(FONT_TITLE);
            left.add(prev); left.add(monthLabel); left.add(next);

            JButton add = primaryButton("+ 지출 입력");
            add.addActionListener(e -> openExpenseDialog(null, null));
            p.add(left, BorderLayout.WEST);
            p.add(add, BorderLayout.EAST);
            return p;
        }

        JPanel buildCalendarCard() {
            JPanel card = roundedPanel(WHITE, 18);
            card.setLayout(new BorderLayout());
            JPanel days = new JPanel(new GridLayout(1,7));
            days.setBackground(WHITE);
            String[] names = {"일","월","화","수","목","금","토"};
            for (String s : names) {
                JLabel l = new JLabel(s, SwingConstants.CENTER);
                l.setFont(FONT_BOLD);
                l.setForeground(MUTED);
                days.add(l);
            }
            card.add(days, BorderLayout.NORTH);
            grid.setBackground(WHITE);
            grid.setBorder(new EmptyBorder(8, 8, 8, 8));
            card.add(grid, BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            monthLabel.setText(month.getYear() + "년 " + month.getMonthValue() + "월");
            grid.removeAll();

            LocalDate first = month.atDay(1);
            int offset = first.getDayOfWeek().getValue() % 7;
            int total = month.lengthOfMonth();

            for (int i=0; i<offset; i++) grid.add(new JLabel(""));
            for (int d=1; d<=total; d++) {
                LocalDate date = month.atDay(d);
                grid.add(dayCell(date));
            }
            int cells = offset + total;
            while (cells++ % 7 != 0) grid.add(new JLabel(""));
            grid.revalidate();
            grid.repaint();
        }

        JPanel dayCell(LocalDate date) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(WHITE);
            cell.setBorder(new LineBorder(BORDER, 1, true));
            long total = state.expenses.stream().filter(e -> e.date.equals(date)).mapToLong(e -> e.amount).sum();

            JLabel day = new JLabel(String.valueOf(date.getDayOfMonth()));
            day.setBorder(new EmptyBorder(8, 8, 4, 8));
            day.setForeground(date.getDayOfWeek() == DayOfWeek.SUNDAY ? RED : TEXT);
            day.setFont(FONT_BOLD);
            cell.add(day, BorderLayout.NORTH);

            JLabel amount = new JLabel(total == 0 ? "" : won(total), SwingConstants.RIGHT);
            amount.setForeground(total > 0 ? RED : MUTED);
            amount.setBorder(new EmptyBorder(4, 8, 8, 8));
            cell.add(amount, BorderLayout.SOUTH);

            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showDateDetail(date);
                }
            });
            return cell;
        }

        void showDateDetail(LocalDate date) {
            JDialog dlg = new JDialog(GeojiTalchulApp.this,
                    date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 지출 내역", true);
            dlg.setSize(620, 500);
            dlg.setLocationRelativeTo(GeojiTalchulApp.this);

            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(new EmptyBorder(18, 18, 18, 18));
            root.setBackground(WHITE);

            List<Expense> items = state.expenses.stream().filter(e -> e.date.equals(date)).collect(Collectors.toList());
            JLabel total = new JLabel("총 지출 " + won(items.stream().mapToLong(e -> e.amount).sum()));
            total.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            root.add(total, BorderLayout.NORTH);

            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(WHITE);
            list.setBorder(new EmptyBorder(5, 10, 5, 10));

            for (Expense x : items) {

                JPanel row = new JPanel(new BorderLayout(15, 0));
                row.setBackground(WHITE);

                row.setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 0, BORDER),
                        new EmptyBorder(12, 8, 12, 8)
                ));

                // 지출 이름 + 분류
                JPanel info = new JPanel();
                info.setOpaque(false);
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

                JLabel itemLabel = new JLabel(esc(x.item));
                itemLabel.setFont(FONT_BOLD);

                JLabel categoryLabel = new JLabel(
                        esc(x.large) + " > " + esc(x.medium)
                );
                categoryLabel.setFont(
                        BASE_FONT.deriveFont(Font.PLAIN, 12f)
                );
                categoryLabel.setForeground(MUTED);

                info.add(itemLabel);
                info.add(Box.createVerticalStrut(4));
                info.add(categoryLabel);

                // 금액
                JLabel amountLabel = new JLabel("-" + won(x.amount));
                amountLabel.setFont(FONT_BOLD);
                amountLabel.setForeground(RED);

                // 삭제 버튼
                JButton del = new JButton("삭제");
                del.setForeground(RED);
                del.setBackground(WHITE);
                del.setFocusPainted(false);
                del.setBorder(new LineBorder(
                        new Color(240, 190, 190), 1, true
                ));

                // 백엔드 서버에 DELETE 보내기
                del.addActionListener(e -> {
                    int res = JOptionPane.showConfirmDialog(dlg, "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        new SwingWorker<JsonObject, Void>() {
                            @Override
                            protected JsonObject doInBackground() throws Exception {
                                // 해당 지출의 고유 ID(x.id)를 서버로 보내서 삭제
                                return httpDelete("http://localhost:8080/api/expenses/" + x.id);
                            }
                            @Override
                            protected void done() {
                                state.expenses.remove(x); // 화면에서도 삭제
                                dlg.dispose();
                                refreshAll();
                                showDateDetail(date); // 창 새로고침
                            }
                        }.execute();
                    }
                });

                // 오른쪽 영역
                JPanel right = new JPanel(new FlowLayout(
                        FlowLayout.RIGHT, 10, 0
                ));
                right.setOpaque(false);

                right.add(amountLabel);
                right.add(del);

                row.add(info, BorderLayout.CENTER);
                row.add(right, BorderLayout.EAST);

                // 한 지출 = 한 줄
                row.setMaximumSize(new Dimension(
                        Integer.MAX_VALUE,
                        65
                ));

                list.add(row);
            }

            if (items.isEmpty()) {
                JLabel empty = new JLabel("이 날짜에는 지출 내역이 없습니다.", SwingConstants.CENTER);
                empty.setForeground(MUTED);
                list.add(empty);
            }

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            root.add(scroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton add = primaryButton("+ 이 날짜에 지출 추가");
            add.addActionListener(e -> {
                dlg.dispose();
                openExpenseDialog(null, null, date);
            });
            bottom.add(add);
            root.add(bottom, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setVisible(true);
        }
    }

    // ---------- STATISTICS ----------
    class StatisticsPanel extends JPanel {
        PieChart pie = new PieChart();
        TrendChart trend = new TrendChart();
        JComboBox<String> largeCombo;
        javax.swing.table.DefaultTableModel tableModel;
        JTable detailTable;

        StatisticsPanel() {
            setLayout(new BorderLayout(15,15));
            setBackground(BG);
            setBorder(new EmptyBorder(28,28,28,28));

            JPanel top = new JPanel(new GridLayout(1,2,15,0));
            top.setOpaque(false);
            
            JPanel pieCard = roundedPanel(WHITE, 18);
            pieCard.setLayout(new BorderLayout());
            JPanel pieHead = new JPanel(new BorderLayout());
            pieHead.setOpaque(false);
            pieHead.setBorder(new EmptyBorder(18,18,8,18));
            JLabel pieTitle = new JLabel("카테고리별 지출 비중");
            pieTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            pieHead.add(pieTitle, BorderLayout.WEST);
            
            JComboBox<String> pieCombo = new JComboBox<>(new String[]{"대분류", "중분류"});
            pieCombo.setPreferredSize(new Dimension(100, 28));
            pieCombo.addActionListener(e -> {
                pie.setMode((String)pieCombo.getSelectedItem());
                pie.repaint();
            });
            pieHead.add(pieCombo, BorderLayout.EAST);
            pieCard.add(pieHead, BorderLayout.NORTH);
            pieCard.add(pie, BorderLayout.CENTER);
            
            top.add(pieCard);
            JPanel trendCard = roundedPanel(WHITE, 18);
            trendCard.setLayout(new BorderLayout());
            JPanel trendHead = new JPanel(new BorderLayout());
            trendHead.setOpaque(false);
            trendHead.setBorder(new EmptyBorder(18,18,8,18));
            JLabel trendTitle = new JLabel("최근 지출 추이");
            trendTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            trendHead.add(trendTitle, BorderLayout.WEST);
            
            JComboBox<String> trendCombo = new JComboBox<>(new String[]{"1개월", "6개월", "1년"});
            trendCombo.setSelectedIndex(0);
            trendCombo.setPreferredSize(new Dimension(100, 28));
            trendCombo.addActionListener(e -> {
                String sel = (String)trendCombo.getSelectedItem();
                int m = 6;
                if (sel.equals("1개월")) m = 1;
                else if (sel.equals("6개월")) m = 6;
                else m = 12;
                trend.setMode(m);
                pie.setTimeMode(m);
                trend.repaint();
                pie.repaint();
            });
            trendHead.add(trendCombo, BorderLayout.EAST);
            trendCard.add(trendHead, BorderLayout.NORTH);
            trendCard.add(trend, BorderLayout.CENTER);
            
            top.add(trendCard);
            add(top, BorderLayout.NORTH);

            JPanel detail = roundedPanel(WHITE, 18);
            detail.setLayout(new BorderLayout(10,10));
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title = new JLabel("지출 내역 상세");
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 19f));
            JLabel hint = new JLabel("날짜와 대·중·소분류별 실제 지출 내역을 확인할 수 있습니다.");
            hint.setForeground(MUTED);
            JPanel titleBox = new JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
            titleBox.add(title);
            titleBox.add(Box.createVerticalStrut(4));
            titleBox.add(hint);

            String[] filters = new String[state.largeCategories.size() + 1];
            filters[0] = "전체 지출 내역";
            for (int i = 0; i < state.largeCategories.size(); i++) filters[i + 1] = state.largeCategories.get(i);
            largeCombo = new JComboBox<>(filters);
            largeCombo.setSelectedIndex(0);
            largeCombo.setPreferredSize(new Dimension(180, 34));
            largeCombo.addActionListener(e -> rebuildExpenseTable());
            head.add(titleBox, BorderLayout.WEST);
            head.add(largeCombo, BorderLayout.EAST);
            detail.add(head, BorderLayout.NORTH);

            String[] columns = {"날짜", "대분류", "중분류", "소분류", "가격"};
            tableModel = new javax.swing.table.DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            detailTable = new JTable(tableModel);
            detailTable.setRowHeight(42);
            detailTable.setFont(FONT);
            detailTable.getTableHeader().setFont(FONT_BOLD);
            detailTable.getTableHeader().setBackground(new Color(247,249,251));
            detailTable.getTableHeader().setForeground(TEXT);
            detailTable.setGridColor(new Color(242,244,246));
            detailTable.setSelectionBackground(GREEN_PALE);
            detailTable.setSelectionForeground(TEXT);
            detailTable.getColumnModel().getColumn(0).setPreferredWidth(115);
            detailTable.getColumnModel().getColumn(1).setPreferredWidth(130);
            detailTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            detailTable.getColumnModel().getColumn(3).setPreferredWidth(180);
            detailTable.getColumnModel().getColumn(4).setPreferredWidth(120);
            detailTable.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                    label.setForeground(RED);
                    label.setFont(FONT_BOLD);
                    return label;
                }
            });
            detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            detailTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = detailTable.getSelectedRow();
                        if (row >= 0 && row < currentList.size()) {
                            showExpenseDetail(currentList.get(row));
                        }
                    }
                }
            });
            JScrollPane sp = new JScrollPane(detailTable);
            sp.setBorder(BorderFactory.createEmptyBorder());
            detail.add(sp, BorderLayout.CENTER);
            add(detail, BorderLayout.CENTER);
        }

        // 현재 테이블에 표시 중인 Expense 리스트 (더블클릭 시 참조용)
        List<Expense> currentList = new ArrayList<>();

        void showExpenseDetail(Expense e) {
            JDialog dlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "지출 상세 정보", true);
            dlg.setSize(520, 460);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(true);

            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.setBackground(WHITE);

            // ── 상단 헤더 ──
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(GREEN_DARK);
            header.setBorder(new EmptyBorder(20, 24, 20, 24));
            JLabel headerTitle = new JLabel(e.item == null || e.item.trim().isEmpty() ? e.small : e.item);
            headerTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            headerTitle.setForeground(WHITE);
            JLabel headerAmount = new JLabel("-" + won(e.amount));
            headerAmount.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            headerAmount.setForeground(new Color(255, 200, 200));
            header.add(headerTitle, BorderLayout.WEST);
            header.add(headerAmount, BorderLayout.EAST);
            root.add(header, BorderLayout.NORTH);

            // ── 상세 항목들 ──
            JPanel body = new JPanel();
            body.setBackground(WHITE);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(8, 0, 8, 0));

            body.add(detailRow("날짜",     e.date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN))));
            body.add(detailRow("대분류",   e.large));
            body.add(detailRow("중분류",   e.medium));
            body.add(detailRow("소분류",   e.small));
            body.add(detailRow("내용",     e.item == null || e.item.trim().isEmpty() ? "(없음)" : e.item));
            body.add(detailRow("금액",     won(e.amount)));
            body.add(detailRow("고정지출", e.fixed ? "✔ 고정지출로 등록됨" : "일반 지출"));

            JScrollPane bodyScroll = new JScrollPane(body);
            bodyScroll.setBorder(BorderFactory.createEmptyBorder());
            bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
            root.add(bodyScroll, BorderLayout.CENTER);

            // ── 하단 닫기 및 삭제 버튼 ──
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
            bottom.setBackground(WHITE);
            bottom.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
            
            JButton del = flatButton("삭제");
            del.setForeground(RED);
            del.addActionListener(ev -> {
                int res = JOptionPane.showConfirmDialog(dlg, "정말 이 지출 내역을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    new SwingWorker<JsonObject, Void>() {
                        @Override
                        protected JsonObject doInBackground() throws Exception {
                            // 백엔드 API (DELETE /api/expenses/{id}) 호출
                            return httpDelete("http://localhost:8080/api/expenses/" + e.id);
                        }
                        @Override
                        protected void done() {
                            state.expenses.remove(e);
                            dlg.dispose();
                            refreshAll();
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this, "삭제되었습니다.", "삭제 완료", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }.execute();
                }
            });
            
            JButton close = primaryButton("닫기");
            close.addActionListener(ev -> dlg.dispose());
            
            bottom.add(del);
            bottom.add(close);
            root.add(bottom, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setVisible(true);
        }

        /** 상세 다이얼로그 한 줄 행 (라벨 + 값) */
        JPanel detailRow(String label, String value) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setBackground(WHITE);
            row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(242, 244, 246)),
                new EmptyBorder(14, 24, 14, 24)
            ));
            JLabel lbl = new JLabel(label);
            lbl.setFont(FONT_BOLD);
            lbl.setForeground(MUTED);
            lbl.setPreferredSize(new Dimension(80, 24));
            JLabel val = new JLabel("<html>" + (value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")) + "</html>");
            val.setFont(FONT);
            val.setForeground(TEXT);
            row.add(lbl, BorderLayout.WEST);
            row.add(val, BorderLayout.CENTER);
            return row;
        }

        JPanel chartCard(String title, JComponent chart) {
            JPanel p = roundedPanel(WHITE, 18);
            p.setLayout(new BorderLayout());
            JLabel l = new JLabel(title);
            l.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            l.setBorder(new EmptyBorder(18,18,8,18));
            p.add(l, BorderLayout.NORTH);
            p.add(chart, BorderLayout.CENTER);
            return p;
        }

        void rebuildExpenseTable() {
            if (tableModel == null) return;
            tableModel.setRowCount(0);
            currentList.clear();
            String selected = (String) largeCombo.getSelectedItem();
            List<Expense> list = state.expenses.stream()
                    .filter(x -> "전체 지출 내역".equals(selected) || x.large.equals(selected))
                    .sorted(Comparator.comparing((Expense x) -> x.date).reversed())
                    .collect(Collectors.toList());

            for (Expense e : list) {
                tableModel.addRow(new Object[]{
                        e.date.format(DateTimeFormatter.ofPattern("MM. dd (E)", Locale.KOREAN)),
                        e.large,
                        e.medium,
                        e.small,
                        "-" + won(e.amount)
                });
                currentList.add(e);
            }
            if (list.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "지출 내역 없음", "", "", ""});
            }
        }

        void refresh() {
            Object selected = largeCombo.getSelectedItem();
            javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
            model.addElement("전체 지출 내역");
            for (String cat : state.largeCategories) {
                model.addElement(cat);
            }
            if (selected != null && model.getIndexOf(selected) >= 0) {
                model.setSelectedItem(selected);
            } else {
                model.setSelectedItem("전체 지출 내역");
            }
            largeCombo.setModel(model);

            pie.repaint();
            trend.repaint();
            rebuildExpenseTable();
        }
    }

    class PieChart extends JPanel {
        String mode = "대분류";
        int timeMode = 1; // 기본값 1개월
        
        public void setMode(String mode) { this.mode = mode; }
        public void setTimeMode(int timeMode) { this.timeMode = timeMode; }
        
        //  마우스 호버 상태를 저장할 변수들
        String hoveredCategory = null;
        List<SliceInfo> slices = new ArrayList<>();
        java.awt.geom.Ellipse2D.Double innerHole = null;

        // 각 파이 조각의 영역(도형)과 카테고리 이름을 묶어둘 클래스
        class SliceInfo {
            Shape arc;
            String category;
            SliceInfo(Shape arc, String category) {
                this.arc = arc;
                this.category = category;
            }
        }

        PieChart() { 
            setPreferredSize(new Dimension(400, 300)); 
            setBackground(WHITE); 
            
            // 마우스 움직임 감지 리스너
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    String found = null;
                    Point p = e.getPoint();
                    
                    if (innerHole != null && !innerHole.contains(p)) {
                        for (SliceInfo info : slices) {
                            if (info.arc.contains(p)) {
                                found = info.category;
                                break;
                            }
                        }
                    }
                    
                    if (!Objects.equals(found, hoveredCategory)) {
                        hoveredCategory = found;
                        repaint();
                    }
                }
            });

            // 마우스가 차트 밖으로 나가면 호버 상태 초기화
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoveredCategory != null) {
                        hoveredCategory = null;
                        repaint();
                    }
                }
            });
        }
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            slices.clear(); 

            Map<String,Long> map = mode.equals("대분류") ? state.largeTotals(timeMode) : state.mediumTotals(timeMode);
            long total = map.values().stream().mapToLong(Long::longValue).sum();
            if (total == 0) {
                g2.setColor(MUTED);
                g2.drawString("지출 데이터가 없습니다.", getWidth()/2-60, getHeight()/2);
                g2.dispose();
                return;
            }

            Color[] colors = {GREEN, BLUE, ORANGE, PURPLE, new Color(90,150,130), new Color(205,128,120), new Color(150,150,95)};

            int diameter = Math.min(200, getHeight() - 110); 
            int x = (getWidth() - diameter) / 2;
            int y = 20; 
            
            double start = 0;
            int i = 0;
            
            //  지출 1위 카테고리를 찾기 위한 변수 추가
            String maxCategory = "";
            long maxVal = -1;
            
            for (Map.Entry<String,Long> en : map.entrySet()) {
                double angle = 360.0 * en.getValue() / total;
                Arc2D.Double arc = new Arc2D.Double(x, y, diameter, diameter, start, angle, Arc2D.PIE);
                
                g2.setColor(colors[i++ % colors.length]);
                g2.fill(arc);
                
                slices.add(new SliceInfo(arc, en.getKey()));
                start += angle;
                
                //  파이 조각을 그리면서 최댓값과 그 카테고리 이름 저장!
                if (en.getValue() > maxVal) {
                    maxVal = en.getValue();
                    maxCategory = en.getKey();
                }
            }

            int thickness = 25; 
            int innerDiameter = diameter - (thickness * 2);
            int innerX = x + thickness;
            int innerY = y + thickness;
            
            innerHole = new java.awt.geom.Ellipse2D.Double(innerX, innerY, innerDiameter, innerDiameter);
            g2.setColor(WHITE);
            g2.fill(innerHole);

            // 마우스를 올리지 않았을 때는 '대분류' 글자 대신 'maxCategory(1위 지출)' 띄우기!
            String centerTitle = (hoveredCategory != null) ? hoveredCategory : maxCategory;

            g2.setColor(TEXT);
            g2.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            FontMetrics fm = g2.getFontMetrics();

            int textX = x + (diameter - fm.stringWidth(centerTitle)) / 2;
            int textY = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(centerTitle, textX, textY);

            // 하단 범례(Legend) 2열 배치
            int legendTop = y + diameter + 25; 
            int colWidth = getWidth() / 2; 
            int row = 0, col = 0;
            i = 0;
            
            for (Map.Entry<String,Long> en : map.entrySet()) {
                int lx = 40 + col * colWidth; 
                int ly = legendTop + row * 28; 
                
                g2.setColor(colors[i++ % colors.length]);
                g2.fillRoundRect(lx, ly - 12, 14, 14, 4, 4);
                
                // 마우스 호버 중인 항목은 범례 글씨 강조
                if (en.getKey().equals(hoveredCategory)) {
                    g2.setColor(GREEN_DARK);
                    g2.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
                } else {
                    g2.setColor(TEXT);
                    g2.setFont(FONT);
                }
                
                int pct = (int)Math.round(en.getValue() * 100.0 / total);
                g2.drawString(en.getKey() + " " + pct + "%", lx + 22, ly);

                col++;
                if (col >= 2) { 
                    col = 0;
                    row++;
                }
            }
            g2.dispose();
        }
    }

    class TrendChart extends JPanel {
        int mode = 1; // 1, 6, 12
        public void setMode(int mode) { this.mode = mode; }

        TrendChart() { setPreferredSize(new Dimension(400, 230)); setBackground(WHITE); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left=55, right=25, top=30, bottom=45;
            int w=getWidth()-left-right, h=getHeight()-top-bottom;
            g2.setColor(new Color(235,238,240));
            for(int i=0;i<=4;i++){
                int yy=top+h*i/4;
                g2.drawLine(left,yy,left+w,yy);
            }

            // YearMonth now = YearMonth.now();

            YearMonth now = YearMonth.of(2026, 8);
            long max = 1;
            int count = mode == 1 ? now.lengthOfMonth() : mode; // 1개월은 해당 월의 일수
            long[] vals = new long[count];
            
            if (mode == 1) {
                // 1개월 모드: 1일부터 월말까지 일별 지출
                for(int i=0; i<count; i++){
                    LocalDate d = now.atDay(i + 1);
                    vals[i] = state.expenses.stream().filter(e -> e.date.equals(d)).mapToLong(e->e.amount).sum();
                    max = Math.max(max, vals[i]);
                }
            } else {
                // 6개월, 1년 모드: 월별 지출
                for(int i=0; i<count; i++){
                    YearMonth ym = now.minusMonths(count - 1 - i);
                    vals[i] = state.expenses.stream().filter(e -> YearMonth.from(e.date).equals(ym)).mapToLong(e->e.amount).sum();
                    max = Math.max(max, vals[i]);
                }
            }

            g2.setColor(BLUE);
            g2.setStroke(new BasicStroke(3));
            int px=left, py=top+h-(int)(vals[0]*1.0/max*h);
            
            for(int i=0; i<count; i++){
                int xx = left + (w * i / Math.max(1, count - 1));
                int yy = top + h - (int)(vals[i]*1.0/max*h);
                if(i > 0) g2.drawLine(px, py, xx, yy);
                
                // 1개월 모드는 점을 작게, 나머지는 원래 크기로
                if (mode == 1) {
                    g2.fillOval(xx-2, yy-2, 4, 4);
                } else {
                    g2.fillOval(xx-4, yy-4, 8, 8);
                }
                
                // 라벨 렌더링 (글자 겹침 방지 및 중앙 정렬)
                String label = null;
                if (mode == 1) {
                    int day = i + 1;
                    // 1개월 모드: 1일, 10일, 20일, 그리고 마지막 날(말일)만 깔끔하게 표시
                    if (day == 1 || day == 10 || day == 20 || i == count - 1) {
                        label = day + "일";
                    }
                } else if (mode == 12) {
                    // 12개월 모드: 현재 월부터 역순으로 2개월씩 건너뛰어 표시 (겹침 완벽 방지)
                    if ((count - 1 - i) % 2 == 0) {
                        YearMonth ym = now.minusMonths(count - 1 - i);
                        label = ym.getMonthValue() + "월";
                    }
                } else {
                    // 6개월 모드: 간격이 넓어 모두 표시
                    YearMonth ym = now.minusMonths(count - 1 - i);
                    label = ym.getMonthValue() + "월";
                }
                
                if (label != null) {
                    g2.setColor(TEXT);
                    g2.setFont(BASE_FONT.deriveFont(Font.PLAIN, 11f));
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(label);
                    // 점(xx)을 기준으로 글자를 완벽히 가운데 정렬
                    g2.drawString(label, xx - (textWidth / 2), top + h + 24);
                }
                
                g2.setColor(BLUE);
                px = xx; py = yy;
            }
            g2.setColor(TEXT);
            g2.setFont(FONT_BOLD);
            String titleStr = mode == 1 ? "이번 달 지출 추이" : (mode == 6 ? "최근 6개월 지출 추이" : "최근 1년 지출 추이");
            g2.drawString(titleStr, left, 18);
            g2.dispose();
        }
    }

    // ---------- COMMUNITY ----------
    class CommunityPanel extends JPanel {
        JTabbedPane tabs = new JTabbedPane();
        DefaultListModel<String> rankModel = new DefaultListModel<>();
        JLabel mineRankLabel = new JLabel("  내 순위 정보를 불러오는 중...");
        DefaultListModel<String> challengeModel = new DefaultListModel<>();
        
        //  칙칙한 리스트(feedModel) 삭제하고, 카드를 세로로 쌓을 새로운 컨테이너 장착!
        JPanel feedContainer = new JPanel();

        CommunityPanel() {
            setLayout(new BorderLayout());
            setBackground(BG);
            setBorder(new EmptyBorder(28,28,28,28));
            tabs.addTab("글로벌 랭킹", buildRanking());
            tabs.addTab("그룹 지출 챌린지", buildChallenges());
            tabs.addTab("SNS 피드", buildFeed());
            add(tabs, BorderLayout.CENTER);
        }

        JPanel buildRanking() {
            JPanel p = roundedPanel(WHITE,18);
            p.setLayout(new BorderLayout(12,12));
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title = new JLabel("이달의 절약 랭킹");
            title.setFont(FONT_TITLE);
            JLabel sub = new JLabel("지출 금액이 적은 순서를 최우선으로 하며, 누적 포인트를 보조 기준으로 순위를 계산합니다.");
            sub.setForeground(MUTED);
            JPanel htext = new JPanel();
            htext.setOpaque(false); htext.setLayout(new BoxLayout(htext,BoxLayout.Y_AXIS));
            htext.add(title); htext.add(Box.createVerticalStrut(6)); htext.add(sub);
            JButton rule = flatButton("랭킹 기준");
            rule.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "기본 점수 = (100만) - (지출 금액 / 10) + 보유 포인트\n\n* 돈을 적게 쓸수록 랭킹이 높아지며, 포인트는 보조적인 역할(1포인트=10원)만 수행합니다.",
                    "랭킹 기준", JOptionPane.INFORMATION_MESSAGE));
            head.add(htext,BorderLayout.WEST); head.add(rule,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);

            JList<String> list = new JList<>(rankModel);
            list.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
            list.setFixedCellHeight(62);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    label.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
                    label.setBorder(new EmptyBorder(10, 18, 10, 18));

                    if (index == 0) {
                        label.setBackground(new Color(230, 240, 235));
                        label.setForeground(NAVY);
                    } else if (index == 1) {
                        label.setBackground(new Color(240, 245, 248));
                        label.setForeground(NAVY);
                    } else if (index == 2) {
                        label.setBackground(new Color(248, 245, 240));
                        label.setForeground(NAVY);
                    } else {
                        label.setBackground(WHITE);
                        label.setForeground(TEXT);
                    }

                    if (isSelected) {
                        label.setBorder(new CompoundBorder(
                                BorderFactory.createLineBorder(BLUE, 2),
                                new EmptyBorder(8, 16, 8, 16)));
                    }
                    return label;
                }
            });
            p.add(new JScrollPane(list),BorderLayout.CENTER);

            mineRankLabel.setOpaque(true); 
            mineRankLabel.setBackground(GREEN_PALE);
            mineRankLabel.setBorder(new EmptyBorder(16,16,16,16));
            mineRankLabel.setFont(FONT_BOLD);
            p.add(mineRankLabel,BorderLayout.SOUTH);
            return p;
        }

        // ── 챌린지 데이터 모델 ──
        class ChallengeData {
            int roomId;
            int ownerId;
            String name, ownerName, startDate, endDate;
            int goalAmount, rewardPoint, memberCount;
            List<String> members = new ArrayList<>();
            boolean isMine;
            boolean isParticipating;
            ChallengeData(int roomId, int ownerId, String name, String ownerName, String start, String end,
                          int goal, int reward, int cnt, boolean isMine, boolean isParticipating) {
                this.roomId = roomId; this.ownerId = ownerId;
                this.isParticipating = isParticipating;
                this.name = name; this.ownerName = ownerName;
                this.startDate = start; this.endDate = end;
                this.goalAmount = goal; this.rewardPoint = reward;
                this.memberCount = cnt; this.isMine = isMine;
            }
        }
        List<ChallengeData> challengeList = new ArrayList<>();
        JPanel challengeCardContainer = new JPanel();

        JPanel buildChallenges() {
            JPanel p = roundedPanel(WHITE, 18);
            p.setLayout(new BorderLayout(10, 14));
            p.setBorder(new EmptyBorder(20, 20, 20, 20));

            // 헤더
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JPanel titleBox = new JPanel();
            titleBox.setOpaque(false);
            titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("그룹 지출 챌린지");
            title.setFont(FONT_TITLE);
            JLabel sub = new JLabel("함께 절약 목표를 세우고 달성해 보세요!");
            sub.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            sub.setForeground(MUTED);
            titleBox.add(title);
            titleBox.add(Box.createVerticalStrut(3));
            titleBox.add(sub);
            JButton create = primaryButton("+ 챌린지 생성");
            create.addActionListener(e -> createChallenge());
            head.add(titleBox, BorderLayout.WEST);
            head.add(create, BorderLayout.EAST);
            p.add(head, BorderLayout.NORTH);

            // 카드 컨테이너
            challengeCardContainer.setLayout(new BoxLayout(challengeCardContainer, BoxLayout.Y_AXIS));
            challengeCardContainer.setBackground(new Color(248, 249, 251));
            challengeCardContainer.setBorder(new EmptyBorder(8, 0, 8, 0));
            JScrollPane scroll = new JScrollPane(challengeCardContainer);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            p.add(scroll, BorderLayout.CENTER);

            loadChallengesFromServer();
            return p;
        }

        void loadPostsFromServer() {
            new SwingWorker<Void, Void>() {
                com.google.gson.JsonArray arr;
                @Override
                protected Void doInBackground() throws Exception {
                    com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/posts?userId=" + currentUserId);
                    if (el != null && el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    feedContainer.removeAll();
                    if (arr != null) {
                        for (int i = 0; i < arr.size(); i++) {
                            com.google.gson.JsonObject item = arr.get(i).getAsJsonObject();
                            int postId = item.get("postId").getAsInt();
                            int authorUserId = item.has("userId") ? item.get("userId").getAsInt() : 0;
                            String userName = item.has("userName") ? item.get("userName").getAsString() : "익명";
                            String profileBase64 = item.has("profileImage") && !item.get("profileImage").isJsonNull() ? item.get("profileImage").getAsString() : "";
                            String content = item.get("content").getAsString();
                            String imgBase64 = item.has("imageData") && !item.get("imageData").isJsonNull() ? item.get("imageData").getAsString() : "";
                            int likeCount = item.get("likeCount").getAsInt();
                            boolean isLiked = item.has("liked") && item.get("liked").getAsBoolean();
                            
                            ImageIcon profileImage = null;
                            if (profileBase64 != null && !profileBase64.isEmpty()) {
                                try {
                                    byte[] bytes = java.util.Base64.getDecoder().decode(profileBase64);
                                    profileImage = new ImageIcon(bytes);
                                } catch (Exception e) {}
                            }

                            ImageIcon image = null;
                            if (imgBase64 != null && !imgBase64.isEmpty()) {
                                try {
                                    byte[] bytes = java.util.Base64.getDecoder().decode(imgBase64);
                                    image = new ImageIcon(bytes);
                                } catch (Exception e) {}
                            }
                            
                            String createdAt = item.has("createdAt") && !item.get("createdAt").isJsonNull() ? item.get("createdAt").getAsString() : "";
                            if (createdAt.contains("T")) {
                                createdAt = createdAt.replace("T", " ");
                            }
                            if (createdAt.length() > 16) {
                                createdAt = createdAt.substring(0, 16);
                            }
                            
                            feedContainer.add(buildInstaCard(postId, authorUserId, userName, profileImage, content, image, likeCount, isLiked, createdAt));
                            feedContainer.add(Box.createVerticalStrut(20));
                        }
                    }
                    feedContainer.revalidate();
                    feedContainer.repaint();
                    SwingUtilities.invokeLater(() -> {
                        feedContainer.scrollRectToVisible(new java.awt.Rectangle(0, 0, 1, 1));
                    });
                }
            }.execute();
        }

        void loadRankingsFromServer() {
            new SwingWorker<Void, Void>() {
                com.google.gson.JsonArray arr;
                @Override
                protected Void doInBackground() throws Exception {
                    com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/rankings");
                    if (el != null && el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    rankModel.clear();
                    mineRankLabel.setText("  아직 순위 집계 중입니다...");
                    if (arr != null) {
                        for (int i = 0; i < arr.size(); i++) {
                            com.google.gson.JsonObject item = arr.get(i).getAsJsonObject();
                            int rank = item.get("rank").getAsInt();
                            String name = item.get("userName").getAsString();
                            int goal = item.get("goalAmount").getAsInt();
                            int actual = item.get("actualAmount").getAsInt();
                            double rate = item.get("achievementRate").getAsDouble();
                            int score = item.get("score").getAsInt();
                            
                            String text = String.format("    %d위    %-8s     목표 %,d원    실제 %,d원    달성률 %.1f%%    (점수: %d)", 
                                rank, name, goal, actual, rate, score);
                            rankModel.addElement(text);
                            
                            String currentName = userLabel.getText().replace(" 님", "");
                            if (name.equals(currentName)) {
                                mineRankLabel.setText(String.format("  내 순위  %d위    목표 %,d원   /   현재 %,d원", rank, goal, actual));
                            }
                        }
                    }
                }
            }.execute();
        }

        void loadChallengesFromServer() {
            new SwingWorker<Void, Void>() {
                com.google.gson.JsonArray arr;
                @Override
                protected Void doInBackground() throws Exception {
                    com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/challenges");
                    if (el != null && el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    }
                    return null;
                }
                
                private String formatTimestampIfNeeded(String dateStr) {
                    if (dateStr == null || dateStr.isEmpty()) return "";
                    if (dateStr.matches("\\d+")) {
                        try {
                            long ts = Long.parseLong(dateStr);
                            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(ts));
                        } catch (Exception e) {}
                    }
                    return dateStr;
                }

                @Override
                protected void done() {
                    challengeList.clear();
                    if (arr != null) {
                        for (com.google.gson.JsonElement itemEl : arr) {
                            com.google.gson.JsonObject item = itemEl.getAsJsonObject();
                            int roomId = item.has("roomId") ? item.get("roomId").getAsInt() : 0;
                            int ownerId = item.has("ownerId") ? item.get("ownerId").getAsInt() : 0;
                            String name = item.has("roomName") ? item.get("roomName").getAsString() : "";
                            String ownerName = item.has("ownerName") ? item.get("ownerName").getAsString() : "";
                            String startDateRaw = item.has("startDate") ? item.get("startDate").getAsString() : "";
                            String endDateRaw = item.has("endDate") ? item.get("endDate").getAsString() : "";
                            
                            String startDate = formatTimestampIfNeeded(startDateRaw);
                            String endDate = formatTimestampIfNeeded(endDateRaw);
                            
                            boolean isMine = (ownerId == currentUserId);
                            
                            int memberCount = 1;
                            java.util.List<String> loadedMembers = new java.util.ArrayList<>();
                            if (item.has("members") && item.get("members").isJsonArray()) {
                                com.google.gson.JsonArray mArr = item.get("members").getAsJsonArray();
                                memberCount = mArr.size();
                                for (com.google.gson.JsonElement me : mArr) {
                                    String mName = me.getAsString();
                                    if (mName.equals(ownerName)) {
                                        loadedMembers.add(mName + " (방장)");
                                    } else {
                                        loadedMembers.add(mName);
                                    }
                                }
                            }
                            
                            boolean isParticipating = isMine;
                            String currentName = userLabel.getText().replace(" 님", "");
                            if (item.has("members") && item.get("members").isJsonArray()) {
                                for (com.google.gson.JsonElement me : item.get("members").getAsJsonArray()) {
                                    if (me.getAsString().equals(currentName)) {
                                        isParticipating = true;
                                    }
                                }
                            }
                            ChallengeData cd = new ChallengeData(roomId, ownerId, name, ownerName, startDate, endDate, 300000, 1000, memberCount, isMine, isParticipating);
                            if (loadedMembers.isEmpty()) {
                                cd.members.add(ownerName + " (방장)");
                            } else {
                                cd.members.addAll(loadedMembers);
                            }
                            challengeList.add(cd);
                        }
                    }
                    refreshChallengeCards();
                }
            }.execute();
        }

        void refreshChallengeCards() {
            challengeCardContainer.removeAll();
            if (challengeList.isEmpty()) {
                JLabel empty = new JLabel("참여 중인 챌린지가 없습니다. 챌린지를 생성하거나 참여해 보세요!", SwingConstants.CENTER);
                empty.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                empty.setForeground(MUTED);
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                empty.setBorder(new EmptyBorder(40, 0, 0, 0));
                challengeCardContainer.add(empty);
            } else {
                for (ChallengeData cd : challengeList) {
                    challengeCardContainer.add(buildChallengeCard(cd));
                    challengeCardContainer.add(Box.createVerticalStrut(12));
                }
            }
            challengeCardContainer.revalidate();
            challengeCardContainer.repaint();
        }

        JPanel buildChallengeCard(ChallengeData cd) {
            JPanel card = new RoundedPanel(WHITE, 16);
            card.setLayout(new BorderLayout(12, 0));
            card.setBorder(new EmptyBorder(16, 18, 16, 18));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel badge = new JPanel();
            badge.setBackground(cd.isMine ? GREEN_DARK : BLUE);
            badge.setPreferredSize(new Dimension(6, 0));
            badge.setOpaque(true);
            card.add(badge, BorderLayout.WEST);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row1.setOpaque(false);
            JLabel nameLabel = new JLabel(cd.name);
            nameLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
            nameLabel.setForeground(TEXT);
            row1.add(nameLabel);
            if (cd.isParticipating) {
                JLabel ownerBadge = new JLabel(cd.isMine ? "  방장" : "  참여중");
                ownerBadge.setFont(BASE_FONT.deriveFont(Font.BOLD, 11f));
                ownerBadge.setForeground(cd.isMine ? GREEN_DARK : BLUE);
                ownerBadge.setOpaque(true);
                ownerBadge.setBackground(cd.isMine ? new Color(230, 245, 238) : new Color(230, 238, 255));
                ownerBadge.setBorder(new EmptyBorder(2, 7, 2, 7));
                row1.add(ownerBadge);
            }

            JLabel dateLabel = new JLabel(cd.startDate + "  ~  " + cd.endDate);
            dateLabel.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
            dateLabel.setForeground(MUTED);
            dateLabel.setBorder(new EmptyBorder(3, 0, 0, 0));

            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
            row3.setOpaque(false);
            JLabel goalLbl = new JLabel("목표 " + String.format("%,d", cd.goalAmount) + "원");
            goalLbl.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            JLabel rewardLbl = new JLabel("보상 " + String.format("%,d", cd.rewardPoint) + "P");
            rewardLbl.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            rewardLbl.setForeground(new Color(200, 140, 30));
            JLabel memberLbl = new JLabel(cd.memberCount + "명 참여");
            memberLbl.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            memberLbl.setForeground(MUTED);
            row3.add(goalLbl); row3.add(rewardLbl); row3.add(memberLbl);

            info.add(row1);
            info.add(Box.createVerticalStrut(4));
            info.add(dateLabel);
            info.add(Box.createVerticalStrut(4));
            info.add(row3);
            card.add(info, BorderLayout.CENTER);

            JLabel arrow = new JLabel("›");
            arrow.setFont(BASE_FONT.deriveFont(Font.BOLD, 28f));
            arrow.setForeground(MUTED);
            card.add(arrow, BorderLayout.EAST);

            card.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 248, 255)); card.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { card.setBackground(WHITE); card.repaint(); }
                @Override public void mouseClicked(MouseEvent e) { showChallengeDetail(cd); }
            });
            return card;
        }

        void showChallengeDetail(ChallengeData cd) {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "챌린지 상세", true);
            d.setSize(520, 560);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());
            d.getContentPane().setBackground(BG);

            // 배너
            JPanel banner = new JPanel(new BorderLayout());
            banner.setBackground(GREEN_DARK);
            banner.setBorder(new EmptyBorder(22, 24, 22, 24));
            JLabel bannerTitle = new JLabel("[챌린지]  " + cd.name);
            bannerTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            bannerTitle.setForeground(WHITE);
            JLabel bannerOwner = new JLabel("방장: " + cd.ownerName);
            bannerOwner.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            bannerOwner.setForeground(new Color(200, 235, 220));
            JPanel bannerText = new JPanel();
            bannerText.setOpaque(false);
            bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
            bannerText.add(bannerTitle);
            bannerText.add(Box.createVerticalStrut(4));
            bannerText.add(bannerOwner);
            banner.add(bannerText, BorderLayout.CENTER);
            d.add(banner, BorderLayout.NORTH);

            // 본문
            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(BG);
            body.setBorder(new EmptyBorder(20, 24, 10, 24));

            JPanel infoCard = new RoundedPanel(WHITE, 14);
            infoCard.setLayout(new GridLayout(3, 2, 10, 12));
            infoCard.setBorder(new EmptyBorder(16, 20, 16, 20));
            infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
            addDetailRow(infoCard, "시작일", cd.startDate);
            addDetailRow(infoCard, "종료일", cd.endDate);
            addDetailRow(infoCard, "목표 지출", String.format("%,d원", cd.goalAmount));
            addDetailRow(infoCard, "보상 포인트", String.format("%,dP", cd.rewardPoint));
            addDetailRow(infoCard, "참여 인원", cd.memberCount + "명");
            addDetailRow(infoCard, "상태", "진행 중");
            body.add(infoCard);
            body.add(Box.createVerticalStrut(16));

            JPanel memberCard = new RoundedPanel(WHITE, 14);
            memberCard.setLayout(new BorderLayout(0, 8));
            memberCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            memberCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

            JPanel memberHead = new JPanel(new BorderLayout());
            memberHead.setOpaque(false);
            JLabel memberTitle = new JLabel("참여 멤버");
            memberTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 15f));
            memberHead.add(memberTitle, BorderLayout.WEST);
            if (cd.isMine) {
                JButton invBtn = flatButton("+초대");
                invBtn.setForeground(GREEN_DARK);
                invBtn.setMargin(new Insets(2, 4, 2, 4));
                invBtn.addActionListener(ev -> { d.dispose(); showInviteFriendDialog(cd); });
                memberHead.add(invBtn, BorderLayout.EAST);
            }

            JPanel memberList = new JPanel();
            memberList.setLayout(new BoxLayout(memberList, BoxLayout.Y_AXIS));
            memberList.setOpaque(false);
            List<String> displayMembers = cd.members.isEmpty()
                ? java.util.Arrays.asList(cd.ownerName + " (방장)") : cd.members;
            for (String m : displayMembers) {
                JPanel mrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
                mrow.setOpaque(false);
                JLabel lbl = new JLabel("\u25CF  " + m);   // ● 불릿 포인트
                lbl.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                lbl.setForeground(TEXT);
                mrow.add(lbl);
                memberList.add(mrow);
            }
            JScrollPane memberScroll = new JScrollPane(memberList);
            memberScroll.setBorder(null);
            memberCard.add(memberHead, BorderLayout.NORTH);
            memberCard.add(memberScroll, BorderLayout.CENTER);
            body.add(memberCard);

            JScrollPane bodyScroll = new JScrollPane(body);
            bodyScroll.setBorder(null);
            d.add(bodyScroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            bottom.setBackground(new Color(248, 249, 251));
            bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
            JButton close = styledSecondaryButton("닫기");
            close.addActionListener(ev -> d.dispose());
            if (cd.isMine) {
                JButton delBtn = styledSecondaryButton("챌린지 삭제");
                delBtn.setForeground(RED);
                delBtn.addActionListener(ev -> {
                    int ans = JOptionPane.showConfirmDialog(d, "정말 이 챌린지를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                    if (ans == JOptionPane.YES_OPTION) {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                httpDelete("http://localhost:8080/api/challenges?roomId=" + cd.roomId + "&ownerId=" + currentUserId);
                                return null;
                            }
                            @Override
                            protected void done() {
                                d.dispose();
                                loadChallengesFromServer();
                            }
                        }.execute();
                    }
                });
                bottom.add(delBtn);
            }
            bottom.add(close);
            d.add(bottom, BorderLayout.SOUTH);
            d.setVisible(true);
        }

        private void addDetailRow(JPanel parent, String key, String val) {
            JLabel k = new JLabel(key);
            k.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            k.setForeground(MUTED);
            JLabel v = new JLabel(val);
            v.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
            v.setForeground(TEXT);
            parent.add(k); parent.add(v);
        }

        void showInviteFriendDialog(ChallengeData cd) {
            new SwingWorker<List<String>, Void>() {
                @Override
                protected List<String> doInBackground() throws Exception {
                    List<String> friends = new ArrayList<>();
                    com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/friends?userId=" + currentUserId);
                    if (el != null && el.isJsonArray()) {
                        homePanel.friendMap.clear();
                        for (com.google.gson.JsonElement item : el.getAsJsonArray()) {
                            com.google.gson.JsonObject obj = item.getAsJsonObject();
                            int fId = obj.has("friendId") ? obj.get("friendId").getAsInt() : -1;
                            String fName = obj.has("friendUserName") ? obj.get("friendUserName").getAsString() : "알수없음";
                            if (fId >= 0) {
                                homePanel.friendMap.put(fId, fName);
                                friends.add(fName);
                            }
                        }
                    }
                    return friends;
                }
                
                @Override
                protected void done() {
                    try {
                        List<String> friends = get();
                        if (friends.isEmpty()) {
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this,
                                "등록된 친구가 없습니다.\n홈 화면의 [친구 관리]에서 먼저 친구를 추가해 주세요.",
                                "친구 초대", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        
                        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(CommunityPanel.this), "친구 초대", true);
                        d.setSize(380, 420);
                        d.setLocationRelativeTo(CommunityPanel.this);
                        d.setLayout(new BorderLayout());
                        d.getContentPane().setBackground(BG);

                        JLabel title = new JLabel("  초대할 친구를 선택하세요", SwingConstants.LEFT);
                        title.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
                        title.setBorder(new EmptyBorder(18, 18, 10, 18));
                        d.add(title, BorderLayout.NORTH);

                        JPanel listPanel = new JPanel();
                        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                        listPanel.setBackground(WHITE);
                        
                        List<JCheckBox> boxes = new ArrayList<>();
                        for (String f : friends) {
                            JCheckBox cb = new JCheckBox(f);
                            cb.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                            cb.setOpaque(false);
                            cb.setBorder(new EmptyBorder(8, 10, 8, 10));
                            if (cd.members.stream().anyMatch(m -> m.contains(f))) {
                                cb.setText(f + " (참여중)");
                                cb.setEnabled(false);
                            }
                            boxes.add(cb);
                            listPanel.add(cb);
                        }

                        JScrollPane scroll = new JScrollPane(listPanel);
                        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
                        d.add(scroll, BorderLayout.CENTER);

                        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
                        bottom.setBackground(new Color(248, 249, 251));
                        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
                        JButton cancel = styledSecondaryButton("취소");
                        cancel.addActionListener(ev -> d.dispose());
                        JButton ok = primaryButton("초대 보내기");
                        ok.addActionListener(ev -> {
                            List<String> invited = new ArrayList<>();
                            for (int i = 0; i < boxes.size(); i++)
                                if (boxes.get(i).isSelected()) invited.add(friends.get(i));
                            if (invited.isEmpty()) { JOptionPane.showMessageDialog(d, "초대할 친구를 선택해주세요."); return; }
                            for (String inv : invited) {
                                boolean dup = cd.members.stream().anyMatch(m -> m.contains(inv));
                                if (!dup) { cd.members.add(inv); cd.memberCount++; }
                            }
                            d.dispose();
                            refreshChallengeCards();
                            JOptionPane.showMessageDialog(GeojiTalchulApp.this,
                                String.join(", ", invited) + " 님께 초대를 보냈습니다.",
                                "초대 완료", JOptionPane.INFORMATION_MESSAGE);
                        });
                        bottom.add(cancel); bottom.add(ok);
                        d.add(bottom, BorderLayout.SOUTH);

                        d.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        }

        void createChallenge() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "그룹 챌린지 생성", true);
            d.setSize(480, 560);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());
            d.getContentPane().setBackground(BG);

            JLabel header = new JLabel("  새 챌린지 만들기", SwingConstants.LEFT);
            header.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            header.setBorder(new EmptyBorder(20, 20, 12, 20));
            d.add(header, BorderLayout.NORTH);

            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(BG);
            form.setBorder(new EmptyBorder(0, 20, 0, 20));

            form.add(formLabel("챌린지 이름"));
            JTextField nameField = new JTextField();
            nameField.setFont(FONT);
            nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            nameField.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
            form.add(nameField);
            form.add(Box.createVerticalStrut(12));

            form.add(formLabel("목표 지출금액 (원)"));
            JTextField goalField = new JTextField("300000");
            goalField.setFont(FONT);
            goalField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            goalField.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
            form.add(goalField);
            form.add(Box.createVerticalStrut(12));

            form.add(formLabel("보상 포인트 (P)"));
            JTextField rewardField = new JTextField("1000");
            rewardField.setFont(FONT);
            rewardField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            rewardField.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
            form.add(rewardField);
            form.add(Box.createVerticalStrut(16));

            form.add(formLabel("시작일"));
            JTextField[] startRef = {null};
            JPanel startRow = buildDateInputRow(d, startRef, "시작일");
            startRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            form.add(startRow);
            form.add(Box.createVerticalStrut(12));

            form.add(formLabel("종료일"));
            JTextField[] endRef = {null};
            JPanel endRow = buildDateInputRow(d, endRef, "종료일");
            endRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            form.add(endRow);
            form.add(Box.createVerticalStrut(6));

            JLabel hint = new JLabel("  직접 입력: 20260827 / 2026-08-27 / 2026/08/27  또는 달력으로 선택");
            hint.setFont(BASE_FONT.deriveFont(Font.PLAIN, 11f));
            hint.setForeground(MUTED);
            form.add(hint);

            JScrollPane formScroll = new JScrollPane(form);
            formScroll.setBorder(null);
            d.add(formScroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            bottom.setBackground(new Color(248, 249, 251));
            bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
            JButton cancel = styledSecondaryButton("취소");
            cancel.addActionListener(ev -> d.dispose());
            JButton ok = primaryButton("생성하기");
            ok.addActionListener(ev -> {
                String nm = nameField.getText().trim();
                if (nm.isEmpty()) { JOptionPane.showMessageDialog(d, "챌린지 이름을 입력해 주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE); return; }
                String parsedStart = parseDateInput(startRef[0] != null ? startRef[0].getText().trim() : "");
                String parsedEnd   = parseDateInput(endRef[0]   != null ? endRef[0].getText().trim()   : "");
                if (parsedStart == null) { JOptionPane.showMessageDialog(d, "시작일 형식이 올바르지 않습니다.\n(예: 20260827, 2026-08-27, 2026/08/27)", "입력 오류", JOptionPane.WARNING_MESSAGE); return; }
                if (parsedEnd   == null) { JOptionPane.showMessageDialog(d, "종료일 형식이 올바르지 않습니다.\n(예: 20260827, 2026-08-27, 2026/08/27)", "입력 오류", JOptionPane.WARNING_MESSAGE); return; }
                
                ok.setEnabled(false);
                ok.setText("생성 중...");
                
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("ownerId", currentUserId);
                        body.addProperty("roomName", nm);
                        body.addProperty("startDate", parsedStart);
                        body.addProperty("endDate", parsedEnd);
                        com.google.gson.JsonObject res = httpPost("http://localhost:8080/api/challenges", body.toString());
                        return res != null && res.has("success") && res.get("success").getAsBoolean();
                    }
                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                earnPointAsync("CHALLENGE", 50);
                                loadChallengesFromServer();
                                d.dispose();
                                JOptionPane.showMessageDialog(CommunityPanel.this, "\"" + nm + "\" 챌린지가 생성되었습니다!\n포인트 50P가 적립되었습니다.", "챌린지 생성 완료", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(d, "챌린지 생성에 실패했습니다.", "생성 오류", JOptionPane.WARNING_MESSAGE);
                                ok.setEnabled(true);
                                ok.setText("생성하기");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(d, "서버 오류: " + ex.getMessage(), "생성 오류", JOptionPane.ERROR_MESSAGE);
                            ok.setEnabled(true);
                            ok.setText("생성하기");
                        }
                    }
                }.execute();
            });
            bottom.add(cancel); bottom.add(ok);
            d.add(bottom, BorderLayout.SOUTH);
            d.setVisible(true);
        }

        private JPanel buildDateInputRow(JDialog parent, JTextField[] resultRef, String label) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setOpaque(false);
            JTextField tf = new JTextField();
            tf.setFont(FONT);
            tf.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
            tf.setToolTipText("20260827, 2026-08-27, 2026/08/27 형태로 입력");
            resultRef[0] = tf;
            JButton calBtn = new JButton("달력");
            calBtn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 13f));
            calBtn.setPreferredSize(new Dimension(52, 36));
            calBtn.setFocusPainted(false);
            calBtn.setBackground(WHITE);
            calBtn.setBorder(new LineBorder(BORDER, 1));
            calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            calBtn.addActionListener(e -> {
                String picked = showDatePickerDialog(parent, label);
                if (picked != null) tf.setText(picked);
            });
            row.add(tf, BorderLayout.CENTER);
            row.add(calBtn, BorderLayout.EAST);
            return row;
        }

        private String showDatePickerDialog(JDialog parent, String titleStr) {
            JDialog cal = new JDialog(parent, titleStr + " 선택", true);
            cal.setSize(400, 420);  // 충분히 크게
            cal.setLocationRelativeTo(parent);
            cal.setLayout(new BorderLayout());
            cal.getContentPane().setBackground(WHITE);

            // LocalDate[] cursor = {LocalDate.now()};

            LocalDate[] cursor = {LocalDate.of(2026, 8, 30)};
            String[] picked = {null};

            // ── 헤더 (월 네비게이션) ──
            JPanel nav = new JPanel(new BorderLayout(8, 0));
            nav.setBackground(GREEN_DARK);
            nav.setBorder(new EmptyBorder(12, 16, 12, 16));

            JButton prev = new JButton("< 이전");
            prev.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
            prev.setForeground(WHITE);
            prev.setBackground(new Color(63, 95, 88));
            prev.setBorder(new EmptyBorder(6, 12, 6, 12));
            prev.setFocusPainted(false);
            prev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JButton next = new JButton("다음 >");
            next.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
            next.setForeground(WHITE);
            next.setBackground(new Color(63, 95, 88));
            next.setBorder(new EmptyBorder(6, 12, 6, 12));
            next.setFocusPainted(false);
            next.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 17f));
            monthLabel.setForeground(WHITE);
            nav.add(prev, BorderLayout.WEST);
            nav.add(monthLabel, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            cal.add(nav, BorderLayout.NORTH);

            // ── 달력 그리드 ──
            JPanel calPanel = new JPanel(new BorderLayout(0, 0));
            calPanel.setBackground(WHITE);
            calPanel.setBorder(new EmptyBorder(12, 14, 8, 14));

            // 요일 헤더 행
            JPanel dayHeaderRow = new JPanel(new GridLayout(1, 7, 4, 0));
            dayHeaderRow.setBackground(new Color(245, 246, 248));
            dayHeaderRow.setBorder(new EmptyBorder(6, 0, 6, 0));
            String[] dayNames = {"일","월","화","수","목","금","토"};
            Color[] dayColors = {RED, TEXT, TEXT, TEXT, TEXT, TEXT, BLUE};
            for (int i = 0; i < 7; i++) {
                JLabel h = new JLabel(dayNames[i], SwingConstants.CENTER);
                h.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
                h.setForeground(dayColors[i]);
                dayHeaderRow.add(h);
            }
            calPanel.add(dayHeaderRow, BorderLayout.NORTH);

            // 날짜 그리드 (6행 × 7열 고정)
            JPanel grid = new JPanel(new GridLayout(6, 7, 4, 4));
            grid.setBackground(WHITE);
            grid.setBorder(new EmptyBorder(6, 0, 0, 0));
            calPanel.add(grid, BorderLayout.CENTER);

            cal.add(calPanel, BorderLayout.CENTER);

            Runnable[] buildGrid = {null};
            buildGrid[0] = () -> {
                grid.removeAll();
                YearMonth ym = YearMonth.of(cursor[0].getYear(), cursor[0].getMonth());
                monthLabel.setText(ym.getYear() + "년  " + ym.getMonthValue() + "월");
                int startDow = ym.atDay(1).getDayOfWeek().getValue() % 7; // 0=일
                int total = startDow + ym.lengthOfMonth();
                int cells = (int) Math.ceil(total / 7.0) * 7;
                if (cells < 42) cells = 42;

                for (int i = 0; i < cells; i++) {
                    int dayNum = i - startDow + 1;
                    if (dayNum < 1 || dayNum > ym.lengthOfMonth()) {
                        JLabel empty = new JLabel("");
                        grid.add(empty);
                        continue;
                    }
                    final LocalDate ld = ym.atDay(dayNum);
                    JButton btn = new JButton(String.valueOf(dayNum));
                    btn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
                    btn.setMargin(new Insets(0, 0, 0, 0));
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.setOpaque(true);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    DayOfWeek dow = ld.getDayOfWeek();
                    // if (ld.equals(LocalDate.now())) {
                    if (ld.equals(LocalDate.of(2026, 8, 30))) {
                        // 오늘
                        btn.setBackground(new Color(230, 243, 237));
                        btn.setForeground(GREEN_DARK);
                        btn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
                    } else if (ld.equals(cursor[0])) {
                        // 선택된 날짜
                        btn.setBackground(GREEN_DARK);
                        btn.setForeground(WHITE);
                        btn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
                    } else {
                        btn.setBackground(WHITE);
                        if (dow == DayOfWeek.SUNDAY) btn.setForeground(RED);
                        else if (dow == DayOfWeek.SATURDAY) btn.setForeground(BLUE);
                        else btn.setForeground(TEXT);
                    }

                    btn.addMouseListener(new MouseAdapter() {
                        Color orig = btn.getBackground();
                        @Override public void mouseEntered(MouseEvent e) {
                            if (!ld.equals(cursor[0])) btn.setBackground(new Color(235, 248, 242));
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            if (!ld.equals(cursor[0])) btn.setBackground(orig);
                        }
                    });
                    btn.addActionListener(ev -> {
                        picked[0] = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        cal.dispose();
                    });
                    grid.add(btn);
                }
                grid.revalidate();
                grid.repaint();
            };

            prev.addActionListener(e -> { cursor[0] = cursor[0].minusMonths(1); buildGrid[0].run(); });
            next.addActionListener(e -> { cursor[0] = cursor[0].plusMonths(1); buildGrid[0].run(); });
            buildGrid[0].run();

            // ── 하단 취소 버튼 ──
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            btnRow.setBackground(new Color(248, 249, 251));
            btnRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
            JButton cancelCal = styledSecondaryButton("취소");
            cancelCal.addActionListener(e -> cal.dispose());
            btnRow.add(cancelCal);
            cal.add(btnRow, BorderLayout.SOUTH);
            cal.setVisible(true);
            return picked[0];
        }

        private String parseDateInput(String raw) {
            if (raw == null || raw.trim().isEmpty()) return null;
            raw = raw.trim();
            try {
                if (raw.matches("\\d{8}")) {
                    String r = raw.substring(0,4) + "-" + raw.substring(4,6) + "-" + raw.substring(6,8);
                    LocalDate.parse(r); return r;
                } else if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate.parse(raw); return raw;
                } else if (raw.matches("\\d{4}/\\d{2}/\\d{2}")) {
                    String r = raw.replace("/", "-"); LocalDate.parse(r); return r;
                }
            } catch (Exception ignored) {}
            return null;
        }

        private JLabel formLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
            l.setForeground(new Color(70, 80, 90));
            l.setBorder(new EmptyBorder(0, 0, 4, 0));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }

        // 피드 화면
        JPanel buildFeed() {
            JPanel p=roundedPanel(WHITE,18);
            p.setLayout(new BorderLayout(10,10));
            JPanel head=new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title=new JLabel("SNS 피드");
            title.setFont(FONT_TITLE);
            JButton write=primaryButton("+ 글쓰기");
            
            // 팝업 연결
            write.addActionListener(e->showWritePostDialog()); 
            
            head.add(title,BorderLayout.WEST); head.add(write,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);
            
            feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));
            feedContainer.setBackground(WHITE);
            feedContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(feedContainer);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16); 
            
            p.add(scroll,BorderLayout.CENTER);
            return p;
        }

        //  2. 힙한 새 글 쓰기 커스텀 팝업 (완벽 복구!)
        private void showWritePostDialog() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "새 게시물 작성", true);
            dialog.setSize(450, 420);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(WHITE);

            JLabel title = new JLabel("새 게시물 만들기", SwingConstants.CENTER);
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            title.setBorder(new EmptyBorder(20, 0, 15, 0));
            dialog.add(title, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

            final ImageIcon[] selectedImage = {null};
            final String[] selectedBase64 = {null}; 

            JButton attachBtn = new JButton("갤러리에서 사진 첨부하기");
            attachBtn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
            attachBtn.setBackground(new Color(245, 245, 245));
            attachBtn.setBorder(new EmptyBorder(15, 0, 15, 0));
            attachBtn.setFocusPainted(false);
            attachBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            attachBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일 (*.jpg, *.png, *.gif)", "jpg", "png", "gif"));
                if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(chooser.getSelectedFile().toPath());
                        selectedBase64[0] = java.util.Base64.getEncoder().encodeToString(bytes);
                        selectedImage[0] = new ImageIcon(bytes);
                        attachBtn.setText("" + chooser.getSelectedFile().getName() + " 첨부 완료!");
                        attachBtn.setForeground(GREEN_DARK);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JTextArea textArea = new JTextArea("오늘의 지출 내역이나 다짐을 공유해 보세요!");
            textArea.setFont(BASE_FONT.deriveFont(Font.PLAIN, 15f));
            textArea.setLineWrap(true);
            textArea.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (textArea.getText().equals("오늘의 지출 내역이나 다짐을 공유해 보세요!")) textArea.setText("");
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

            centerPanel.add(attachBtn, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);
            dialog.add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            bottomPanel.setOpaque(false);
            bottomPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

            JButton cancelBtn = new JButton("취소");
            cancelBtn.setBackground(WHITE);
            cancelBtn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
            cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cancelBtn.addActionListener(e -> dialog.dispose());
            
            JButton submitBtn = new JButton("게시하기");
            submitBtn.setBackground(GREEN_DARK); 
            submitBtn.setForeground(WHITE);
            submitBtn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
            submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            submitBtn.addActionListener(e -> {
                String text = textArea.getText().trim();
                boolean isDefaultText = text.equals("오늘의 지출 내역이나 다짐을 공유해 보세요!");
                String postText = isDefaultText ? "" : text;
                
                if(!postText.isEmpty() || selectedImage[0] != null) {
                    new SwingWorker<Void, Void>() {
                        boolean ok = false;
                        @Override
                        protected Void doInBackground() throws Exception {
                            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                            payload.addProperty("userId", currentUserId);
                            payload.addProperty("content", postText);
                            if (selectedBase64[0] != null) {
                                payload.addProperty("imageData", selectedBase64[0]);
                            }
                            com.google.gson.JsonObject res = httpPost("http://localhost:8080/api/posts", payload.toString());
                            if (res != null && res.has("success") && res.get("success").getAsBoolean()) {
                                ok = true;
                                // Wait, the requirement says "+30P for SNS post writing API integration"
                                // We also need to add point earn logic in the backend, but for now we can call PointMgr API?
                                // Actually we can just update state.points manually after success, or add points in the backend
                                // 9. 지출/게시글/챌린지 생성 시 포인트 적립 API 연동 (+20P, +30P, +50P)
                                // Let's leave that to the 9th feature! For now just load feeds!
                            }
                            return null;
                        }
                        @Override
                        protected void done() {
                            if (ok) {
                                earnPointAsync("POST", 30);
                                dialog.dispose();
                            } else {
                                JOptionPane.showMessageDialog(dialog, "게시물 등록에 실패했습니다.", "작성 오류", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                } else {
                    JOptionPane.showMessageDialog(dialog, "내용을 입력하거나 사진을 첨부해 주세요.", "작성 오류", JOptionPane.WARNING_MESSAGE);
                }
            });

            bottomPanel.add(cancelBtn);
            bottomPanel.add(submitBtn);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        private void showEditPostDialog(int postId, String initialText) {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "게시물 수정", true);
            dialog.setSize(450, 480);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(WHITE);

            JLabel title = new JLabel("게시물 수정", SwingConstants.CENTER);
            title.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
            title.setBorder(new EmptyBorder(20, 0, 15, 0));
            dialog.add(title, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

            final String[] selectedBase64 = {null}; 

            JButton attachBtn = new JButton("사진 변경하기 (선택)");
            attachBtn.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
            attachBtn.setBackground(new Color(245, 245, 245));
            attachBtn.setBorder(new EmptyBorder(15, 0, 15, 0));
            attachBtn.setFocusPainted(false);
            attachBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            attachBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("이미지 파일 (*.jpg, *.png, *.gif)", "jpg", "png", "gif"));
                if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(chooser.getSelectedFile().toPath());
                        selectedBase64[0] = java.util.Base64.getEncoder().encodeToString(bytes);
                        attachBtn.setText("새 사진: " + chooser.getSelectedFile().getName());
                        attachBtn.setForeground(GREEN_DARK);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            centerPanel.add(attachBtn, BorderLayout.NORTH);

            JTextArea contentArea = new JTextArea(initialText);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(BASE_FONT.deriveFont(Font.PLAIN, 15f));
            contentArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setBorder(new LineBorder(BORDER, 1));
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            dialog.add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            bottomPanel.setOpaque(false);
            bottomPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

            JButton cancelBtn = flatButton("취소");
            cancelBtn.addActionListener(e -> dialog.dispose());

            JButton submitBtn = primaryButton("수정 완료");
            submitBtn.addActionListener(e -> {
                String text = contentArea.getText().trim();
                if (!text.isEmpty()) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                            body.addProperty("userId", currentUserId);
                            body.addProperty("content", text);
                            if (selectedBase64[0] != null) {
                                body.addProperty("imageData", selectedBase64[0]);
                            }
                            httpPost("http://localhost:8080/api/posts/" + postId, body.toString(), "PUT");
                            return null;
                        }
                        @Override
                        protected void done() {
                            loadPostsFromServer();
                            dialog.dispose();
                        }
                    }.execute();
                } else {
                    JOptionPane.showMessageDialog(dialog, "내용을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                }
            });

            bottomPanel.add(cancelBtn);
            bottomPanel.add(submitBtn);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        void refresh() {
            loadRankingsFromServer();
            loadChallengesFromServer(); // 로그인/아웃 시 챌린지 갱신
            loadPostsFromServer();
        }

        //  4. 댓글 기능 완벽하게 적출된 인스타 카드!
        private JPanel buildInstaCard(int postId, int authorUserId, String author, ImageIcon profileImage, String text, ImageIcon image, int likes, boolean isLikedInitial, String createdAt) {
            JPanel card = new RoundedPanel(WHITE, 20); 
            card.setLayout(new BorderLayout(0, 10));
            card.setBorder(new EmptyBorder(15, 15, 15, 15));
            card.setMaximumSize(new Dimension(800, image != null ? 380 : 180)); 

            JPanel headerWrapper = new JPanel(new BorderLayout());
            headerWrapper.setOpaque(false);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            header.setOpaque(false);
            JLabel profilePic = new JLabel(); 
            profilePic.setPreferredSize(new Dimension(36, 36));
            profilePic.setHorizontalAlignment(SwingConstants.CENTER);
            profilePic.setVerticalAlignment(SwingConstants.CENTER);
            if (profileImage != null) {
                Image scaledProfile = profileImage.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                profilePic.setIcon(new ImageIcon(scaledProfile));
            } else {
                try {
                    ImageIcon pIcon = new ImageIcon(getClass().getResource("/com/richman/ui/poorman.png"));
                    Image scaledProfile = pIcon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                    profilePic.setIcon(new ImageIcon(scaledProfile));
                } catch (Exception e) {
                    profilePic.setText("?");
                }
            }

            JLabel nameLabel = new JLabel(author);
            nameLabel.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
            
            JLabel dateLabel = new JLabel(createdAt);
            dateLabel.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
            dateLabel.setForeground(MUTED);
            
            JPanel nameDatePanel = new JPanel();
            nameDatePanel.setLayout(new BoxLayout(nameDatePanel, BoxLayout.Y_AXIS));
            nameDatePanel.setOpaque(false);
            nameDatePanel.add(nameLabel);
            nameDatePanel.add(dateLabel);

            header.add(profilePic);
            header.add(nameDatePanel);
            headerWrapper.add(header, BorderLayout.WEST);

            if (currentUserId == authorUserId) {
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                actionPanel.setOpaque(false);
                
                JButton editBtn = new JButton("수정");
                editBtn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
                editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                editBtn.setFocusPainted(false);
                editBtn.addActionListener(e -> {
                    showEditPostDialog(postId, text);
                });
                
                JButton delBtn = new JButton("삭제");
                delBtn.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
                delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                delBtn.setForeground(RED);
                delBtn.setFocusPainted(false);
                delBtn.addActionListener(e -> {
                    int ans = JOptionPane.showConfirmDialog(GeojiTalchulApp.this, "정말 삭제하시겠습니까?", "게시물 삭제", JOptionPane.YES_NO_OPTION);
                    if (ans == JOptionPane.YES_OPTION) {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                String url = "http://localhost:8080/api/posts/" + postId + "?userId=" + currentUserId;
                                java.net.URL u = new java.net.URL(url);
                                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                                conn.setRequestMethod("DELETE");
                                conn.getResponseCode();
                                return null;
                            }
                            @Override
                            protected void done() {
                                loadPostsFromServer();
                            }
                        }.execute();
                    }
                });
                
                actionPanel.add(editBtn);
                actionPanel.add(delBtn);
                headerWrapper.add(actionPanel, BorderLayout.EAST);
            }

            card.add(headerWrapper, BorderLayout.NORTH);
            JPanel bottom = new JPanel(new BorderLayout(0, 8));
            bottom.setOpaque(false);
            
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            actions.setOpaque(false);
            
            // --- 좋아요 버튼 ---
            int[] currentLikes = {likes};
            boolean[] isLiked = {isLikedInitial};
            
            JPanel likeBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            likeBtn.setOpaque(false);
            likeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel likeIcon = new JLabel(isLiked[0] ? "\u2665" : "\u2661");
            likeIcon.setFont(BASE_FONT.deriveFont(Font.PLAIN, 18f));
            likeIcon.setPreferredSize(new Dimension(24, 24));
            likeIcon.setHorizontalAlignment(SwingConstants.CENTER);
            likeIcon.setVerticalAlignment(SwingConstants.CENTER);
            likeIcon.setForeground(isLiked[0] ? RED : TEXT);
            JLabel likeText = new JLabel("좋아요 " + currentLikes[0]);
            likeText.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
            likeText.setForeground(isLiked[0] ? RED : TEXT);
            likeBtn.add(likeIcon);
            likeBtn.add(likeText);
            
            likeBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            httpPost("http://localhost:8080/api/posts/" + postId + "/like?userId=" + currentUserId, "");
                            return null;
                        }
                        @Override
                        protected void done() {
                            if (isLiked[0]) {
                                isLiked[0] = false;
                                currentLikes[0]--;
                                likeIcon.setText("\u2661");
                                likeIcon.setForeground(TEXT);
                                likeText.setText("좋아요 " + currentLikes[0]);
                                likeText.setForeground(TEXT);
                            } else {
                                isLiked[0] = true;
                                currentLikes[0]++;
                                likeIcon.setText("\u2665");
                                likeIcon.setForeground(RED);
                                likeText.setText("좋아요 " + currentLikes[0]);
                                likeText.setForeground(RED);
                            }
                        }
                    }.execute();
                }
            });

            actions.add(likeBtn); //  댓글 버튼(commentBtn)은 추가 안 하고 버림!

            JTextArea contentArea = new JTextArea(text);
            contentArea.setFont(BASE_FONT.deriveFont(Font.PLAIN, 14f));
            contentArea.setLineWrap(true);       
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false);

            bottom.add(actions, BorderLayout.NORTH);
            bottom.add(contentArea, BorderLayout.CENTER);

            
            
            if (image != null) {
                JPanel imageBox = new JPanel(new BorderLayout());
                imageBox.setBackground(new Color(240, 240, 240));
                imageBox.setPreferredSize(new Dimension(0, 200)); 
                
                Image scaled = image.getImage().getScaledInstance(400, 200, Image.SCALE_SMOOTH);
                JLabel imgIcon = new JLabel(new ImageIcon(scaled));
                imageBox.add(imgIcon, BorderLayout.CENTER);
                
                card.add(imageBox, BorderLayout.CENTER);
                card.add(bottom, BorderLayout.SOUTH);
            } else {
                card.add(bottom, BorderLayout.CENTER);
            }

            return card;
        }

    }

    // ---------- MY / STORE ----------
    class MyStorePanel extends JPanel {
        JLabel myPoints = new JLabel();

        MyStorePanel() {
            setLayout(new BorderLayout());
            setBackground(BG);
            setBorder(new EmptyBorder(28,28,28,28));

            add(buildStore(), BorderLayout.CENTER);
        }

        JPanel buildStore() {
            JPanel p=roundedPanel(WHITE,18);
            p.setLayout(new BorderLayout(15,15));
            JPanel head=new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title=new JLabel("포인트 상점");
            title.setFont(FONT_TITLE);
            myPoints.setFont(BASE_FONT.deriveFont(Font.BOLD, 20f));
            myPoints.setForeground(GREEN_DARK);
            head.add(title,BorderLayout.WEST); head.add(myPoints,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);

            JPanel items=new JPanel(new GridLayout(0,3,14,14));
            items.setBackground(WHITE);
            p.add(items,BorderLayout.CENTER);
            
            // 상점 아이템을 서버에서 불러오기
            new SwingWorker<Void, Void>() {
                com.google.gson.JsonArray arr;
                @Override
                protected Void doInBackground() throws Exception {
                    com.google.gson.JsonElement el = httpGetElement("http://localhost:8080/api/store/items");
                    if (el != null && el.isJsonArray()) {
                        arr = el.getAsJsonArray();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    if (arr != null) {
                        for (com.google.gson.JsonElement itemEl : arr) {
                            com.google.gson.JsonObject item = itemEl.getAsJsonObject();
                            int itemId = item.has("itemId") ? item.get("itemId").getAsInt() : 0;
                            String name = item.has("productName") ? item.get("productName").getAsString() : "알수없음";
                            String type = item.has("productType") ? item.get("productType").getAsString() : "기타";
                            int price = item.has("pricePoint") ? item.get("pricePoint").getAsInt() : 0;
                            
                            if ("스킨".equals(type)) {
                                String skinFile = name.contains("거지") ? "poorman.png" : "richman.png";
                                addSkinShopItem(items, itemId, name, "캐릭터 꾸미기", String.format("%,dP", price), price, skinFile);
                            } else {
                                String subtitle = name.contains("식사") ? "식사 할인권" :
                                                  name.contains("상품권") ? "상품권" : "";
                                addShopItem(items, itemId, name, subtitle, String.format("%,dP", price), price);
                            }
                        }
                        items.revalidate();
                        items.repaint();
                    }
                }
            }.execute();

            return p;
        }

        void addSkinShopItem(JPanel parent, int itemId, String name, String subtitle, String price, int cost, String skinFile) {
            JPanel c = roundedPanel(new Color(249, 250, 251), 14);
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

            JLabel i = new JLabel(loadSkinIcon(skinFile, 110, 110), SwingConstants.CENTER);
            i.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel n = new JLabel(name);
            n.setFont(FONT_BOLD);
            n.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel sub = new JLabel(subtitle);
            sub.setForeground(MUTED);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel pr = new JLabel(price);
            pr.setForeground(GREEN_DARK);
            pr.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton buy = primaryButton(state.ownedSkins.contains(skinFile) ? "보유 중" : "구매");
            buy.setAlignmentX(Component.CENTER_ALIGNMENT);
            buy.setEnabled(!state.ownedSkins.contains(skinFile));

            buy.addActionListener(e -> {
                if (state.ownedSkins.contains(skinFile)) {
                    JOptionPane.showMessageDialog(this, "이미 보유한 스킨입니다.", "포인트 상점", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                if (state.points >= cost) {
                    buy.setEnabled(false);
                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                            body.addProperty("userId", currentUserId);
                            body.addProperty("itemId", itemId);
                            com.google.gson.JsonObject res = httpPost("http://localhost:8080/api/store/buy", body.toString());
                            return res != null && res.has("success") && res.get("success").getAsBoolean();
                        }
                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    state.points -= cost;
                                    state.ownedSkins.add(skinFile);
                                    refreshAll();
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, name + " 구매 완료!\n\n이제 홈 화면의 [스킨 설정]에서 사용할 수 있습니다.", "포인트 상점", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, "포인트가 부족하거나 구매에 실패했습니다.", "포인트 상점", JOptionPane.WARNING_MESSAGE);
                                    buy.setEnabled(true);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(GeojiTalchulApp.this, "서버 오류: " + ex.getMessage(), "포인트 상점", JOptionPane.ERROR_MESSAGE);
                                buy.setEnabled(true);
                            }
                        }
                    }.execute();
                } else {
                    JOptionPane.showMessageDialog(this, "포인트가 부족합니다.", "포인트 상점", JOptionPane.WARNING_MESSAGE);
                }
            });

            c.add(Box.createVerticalGlue());
            c.add(i);
            c.add(Box.createVerticalStrut(8));
            c.add(n);
            c.add(Box.createVerticalStrut(3));
            c.add(sub);
            c.add(pr);
            c.add(Box.createVerticalStrut(8));
            c.add(buy);
            c.add(Box.createVerticalGlue());

            parent.add(c);
        }

        void addShopItem(JPanel parent, int itemId, String icon, String name, String price, int cost){
            JPanel c=roundedPanel(new Color(249,251,251),14);
            c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));

            JLabel i=new JLabel(icon,SwingConstants.CENTER);
            i.setFont(BASE_FONT.deriveFont(Font.PLAIN, 30f));

            JLabel n=new JLabel(name);
            n.setFont(FONT_BOLD);

            JLabel pr=new JLabel(price);
            pr.setForeground(GREEN_DARK);

            JButton buy=primaryButton("교환");
            buy.setAlignmentX(Component.CENTER_ALIGNMENT);

            buy.addActionListener(e->{
                if(state.points>=cost){
                    buy.setEnabled(false);
                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                            body.addProperty("userId", currentUserId);
                            body.addProperty("itemId", itemId);
                            com.google.gson.JsonObject res = httpPost("http://localhost:8080/api/store/buy", body.toString());
                            return res != null && res.has("success") && res.get("success").getAsBoolean();
                        }
                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    state.points -= cost;
                                    refreshAll();
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, name+" 교환 완료!", "포인트 상점", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, "포인트가 부족하거나 구매에 실패했습니다.", "포인트 상점", JOptionPane.WARNING_MESSAGE);
                                    buy.setEnabled(true);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(GeojiTalchulApp.this, "서버 오류: " + ex.getMessage(), "포인트 상점", JOptionPane.ERROR_MESSAGE);
                                buy.setEnabled(true);
                            }
                        }
                    }.execute();
                }else{
                    JOptionPane.showMessageDialog(
                        this,
                        "포인트가 부족합니다.",
                        "포인트 상점",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            });

            i.setAlignmentX(Component.CENTER_ALIGNMENT);
            n.setAlignmentX(Component.CENTER_ALIGNMENT);
            pr.setAlignmentX(Component.CENTER_ALIGNMENT);

            c.add(Box.createVerticalGlue());
            c.add(i);
            c.add(Box.createVerticalStrut(10));
            c.add(n);
            c.add(Box.createVerticalStrut(5));
            c.add(pr);
            c.add(Box.createVerticalStrut(10));
            c.add(buy);
            c.add(Box.createVerticalGlue());

            parent.add(c);
        }

        void refresh() {
            myPoints.setText("내 포인트  " + state.points + " P");
            removeAll();
            add(buildStore(), BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    // ---------- Expense dialog ----------
    void openExpenseDialog(String presetLarge, String presetMedium) {
        // openExpenseDialog(presetLarge,presetMedium,LocalDate.now());
        openExpenseDialog(presetLarge,presetMedium,LocalDate.of(2026, 8, 30));
    }

    void openExpenseDialog(String presetLarge, String presetMedium, LocalDate presetDate) {
        JDialog dlg=new JDialog(this,"지출 기록",true);
        dlg.setSize(560,650);
        dlg.setLocationRelativeTo(this);

        JPanel root=new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setBackground(WHITE);

        JPanel form=new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(7,5,7,5);
        g.fill=GridBagConstraints.HORIZONTAL;
        g.weightx=1;

        JTextField amount=new JTextField();
        JTextField item=new JTextField();
        JTextField small=new JTextField();
        JTextField date=new JTextField(presetDate.toString());

        JComboBox<String> large=new JComboBox<>(state.largeCategories.toArray(new String[0]));
        if(presetLarge!=null) large.setSelectedItem(presetLarge);
        JComboBox<String> medium=new JComboBox<>();
        Runnable updateMedium=()->{
            medium.removeAllItems();
            String l=(String)large.getSelectedItem();
            for(String m:state.mediumMap.getOrDefault(l,new ArrayList<>())) medium.addItem(m);
            if(presetMedium!=null && Objects.equals(l,presetLarge)) medium.setSelectedItem(presetMedium);
        };
        large.addActionListener(e->updateMedium.run());
        updateMedium.run();

        int r=0;
        addFormRow(form,g,r++,"금액",amount);
        addFormRow(form,g,r++,"대분류",large);
        addFormRow(form,g,r++,"중분류",medium);
        addFormRow(form,g,r++,"소분류",small);
        addFormRow(form,g,r++,"내용",item);
        addFormRow(form,g,r++,"날짜(yyyy-MM-dd)",date);

        JLabel hint=new JLabel("대분류 → 중분류는 시스템 기본값입니다. 소분류는 사용자가 직접 입력하며, 내용은 선택 입력입니다.");
        hint.setForeground(MUTED);
        root.add(hint,BorderLayout.NORTH);
        root.add(form,BorderLayout.CENTER);

        JButton save=primaryButton("지출 저장");
        // 진짜 서버로 지출 내역 쏘는 로직으로 교체
        save.addActionListener(e -> {
            try {
                long won = Long.parseLong(amount.getText().replace(",", "").trim());
                LocalDate d = LocalDate.parse(date.getText().trim());
                String l = (String) large.getSelectedItem();
                String m = (String) medium.getSelectedItem();
                String s = small.getText().trim().isEmpty() ? item.getText() : small.getText();
                if (s.trim().isEmpty()) throw new IllegalArgumentException("소분류를 입력하세요.");
                String content = item.getText().trim();

                // 서버 통신 중 버튼 막기 (연타 방지)
                save.setEnabled(false);
                save.setText("저장 중...");

                new SwingWorker<JsonObject, Void>() {
                    @Override
                    protected JsonObject doInBackground() throws Exception {
                        // 백엔드 API 규격에 맞춰 JSON 조립
                        JsonObject body = new JsonObject();
                        body.addProperty("userId", currentUserId); // 현재 로그인한 내 아이디
                        body.addProperty("largeCategory", l);
                        body.addProperty("mediumCategory", m);
                        body.addProperty("smallCategory", s);
                        body.addProperty("item", content);
                        body.addProperty("amount", won);
                        body.addProperty("expenseDate", d.toString());
                        
                        // 서버로 POST 발사!
                        return httpPost("http://localhost:8080/api/expenses", body.toString());
                    }

                    @Override
                    protected void done() {
                        save.setEnabled(true);
                        save.setText("지출 저장");
                        try {
                            JsonObject res = get();
                            if (res != null) {
                                // DB에 잘 들어갔으므로 방금 쓴 올바른 expenseId를 다시 불러오기 위함
                                dlg.dispose();
                                loadMyExpensesFromServer();
                                earnPointAsync("EXPENSE", 20);
                                
                                if (state.budgetUsage() >= 1) {
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, "지출이 서버에 저장되었습니다.\n\n 예산을 초과했습니다.", "예산 경고", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(GeojiTalchulApp.this, "지출이 저장되었습니다! +20P", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dlg, "서버 저장 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton cancel=new JButton("취소");
        cancel.setFocusPainted(false);
        cancel.addActionListener(e->dlg.dispose());
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(WHITE);
        bottom.add(cancel); bottom.add(save);
        root.add(bottom,BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ---------- State / Model ----------
    static class AppState {
        long budget=500_000;
        int alertThreshold=80;
        int points=14500;
        int nextId=100;
        
        // 기본적으로 거지 스킨을 보유하고 시작합니다.
        LinkedHashSet<String> ownedSkins = new LinkedHashSet<>(Arrays.asList("poorman.png"));
        String currentSkin = "poorman.png";

        List<String> largeCategories = new ArrayList<>();
        Map<String, List<String>> mediumMap = new LinkedHashMap<>();
        
        List<Expense> expenses=new ArrayList<>();
        Set<Integer> fixedExpenseIds=new HashSet<>();

        void addExpense(String large,String medium,String small,String item,long amount,LocalDate date){
            expenses.add(new Expense(nextId++,large,medium,small,item,amount,date,false));
        }
        
        void addDbExpense(int id, String large,String medium,String small,String item,long amount,LocalDate date){
            expenses.add(new Expense(id,large,medium,small,item,amount,date,false));
        }

        // long totalSpent(){ return expenses.stream().filter(e->YearMonth.from(e.date).equals(YearMonth.now())).mapToLong(e->e.amount).sum(); }

        long totalSpent(){ return expenses.stream().filter(e->YearMonth.from(e.date).equals(YearMonth.of(2026, 8))).mapToLong(e->e.amount).sum(); }
        double budgetUsage(){ return budget==0?0:totalSpent()/(double)budget; }

        Map<String,Long> largeTotals(){ return largeTotals(1); }
        Map<String,Long> largeTotals(int months){
            // LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
            LocalDate startDate = LocalDate.of(2026, 8, 30).minusMonths(months - 1).withDayOfMonth(1);
            return expenses.stream().filter(e->!e.date.isBefore(startDate))
                    .collect(Collectors.groupingBy(e->e.large,LinkedHashMap::new,Collectors.summingLong(e->e.amount)));
        }

        Map<String,Long> mediumTotals(){ return mediumTotals(1); }
        Map<String,Long> mediumTotals(int months){
            // LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
            LocalDate startDate = LocalDate.of(2026, 8, 30).minusMonths(months - 1).withDayOfMonth(1);
            return expenses.stream().filter(e->!e.date.isBefore(startDate))
                    .collect(Collectors.groupingBy(e->e.medium,LinkedHashMap::new,Collectors.summingLong(e->e.amount)));
        }

        List<Expense> fixedCandidates(){
            // 최근 3개월 범위를 동적으로 계산
            // LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3).withDayOfMonth(1);
            LocalDate threeMonthsAgo = LocalDate.of(2026, 8, 30).minusMonths(3).withDayOfMonth(1);
            // LocalDate nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            LocalDate nextMonthStart = LocalDate.of(2026, 8, 30).plusMonths(1).withDayOfMonth(1);
            Map<String,List<Expense>> map=expenses.stream()
                    .filter(e->!e.date.isBefore(threeMonthsAgo) && e.date.isBefore(nextMonthStart))
                    .collect(Collectors.groupingBy(e->e.large+"|"+e.medium+"|"+e.amount,LinkedHashMap::new,Collectors.toList()));
            return map.values().stream()
                    .filter(v -> v.stream().map(e -> e.date.getYear() * 12 + e.date.getMonthValue()).distinct().count() >= 3)
                    .map(v->v.get(0))
                    .collect(Collectors.toList());
        }

        int fixedExpenseCandidateCount(){ return fixedCandidates().size(); }

        long fixedCandidateRepeatCount(Expense candidate){
            // LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3).withDayOfMonth(1);
            LocalDate threeMonthsAgo = LocalDate.of(2026, 8, 30).minusMonths(3).withDayOfMonth(1);
            // LocalDate nextMonthStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            LocalDate nextMonthStart = LocalDate.of(2026, 8, 30).plusMonths(1).withDayOfMonth(1);
            return expenses.stream()
                    .filter(e -> !e.date.isBefore(threeMonthsAgo) && e.date.isBefore(nextMonthStart))
                    .filter(e -> e.medium.equals(candidate.medium) && e.amount == candidate.amount)
                    .count();
        }


        String fixedCandidateKey(){
            return fixedCandidates().stream()
                    .map(e -> String.valueOf(e.id))
                    .sorted()
                    .collect(Collectors.joining(","));
        }
    }

    static class Expense {
        int id; String large,medium,small,item; long amount; LocalDate date; boolean fixed;
        Expense(int id,String large,String medium,String small,String item,long amount,LocalDate date,boolean fixed){
            this.id=id;this.large=large;this.medium=medium;this.small=small;this.item=item;this.amount=amount;this.date=date;this.fixed=fixed;
        }
    }

    // ---------- UI helpers ----------
    JPanel roundedPanel(Color bg,int radius){
        //  기존에 전달받은 얕은 숫자(18 등)를 깡그리 무시하고,
        // 토스 감성 충만한 곡률 '40'으로 전체 앱의 카드를 강제 둥글림 처리합니다!
        int tossRadius = 40; 
        
        JPanel p = new RoundedPanel(bg, tossRadius);
        // 글자가 곡선에 씹히지 않도록 카드 내부 기본 여백(Padding)도 빵빵하게 강제 주입
        p.setBorder(new EmptyBorder(25, 25, 25, 25)); 
        return p;
    }

    JButton primaryButton(String text){
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD); b.setForeground(WHITE); b.setBackground(GREEN_DARK);
        b.setFocusPainted(false); 
        //  setBorder 삭제 (FlatLaf 알약 쉐입 유지)
        return b;
    }

    JButton flatButton(String text){
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD); b.setForeground(MUTED); b.setBackground(WHITE);
        b.setFocusPainted(false);
        return b;
    }

    /** 취소/닫기 등 보조 버튼 - 테두리 있는 스타일 */
    JButton styledSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
        b.setForeground(new Color(70, 80, 90));
        b.setBackground(WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(7, 20, 7, 20)
        ));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(245, 246, 248)); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(WHITE); }
        });
        return b;
    }

    JButton navButton(String text){
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setBackground(WHITE); 
        b.setForeground(MUTED);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(42, 42));
        
        //  [추가] 달력 화살표 버튼만 여백을 0으로 덮어써서 글자 짤림 방지!
        b.setMargin(new Insets(0, 0, 0, 0)); 
        
        return b;
    }

    JLabel pillLabel(String text,Color bg,Color fg){
        JLabel l=new JLabel(text);
        l.setOpaque(true); l.setBackground(bg); l.setForeground(fg);
        l.setBorder(new EmptyBorder(7,12,7,12));
        return l;
    }

    void addFormRow(JPanel p,GridBagConstraints g,int row,String label,Component c){
        g.gridx=0;g.gridy=row;g.weightx=0.25;
        JLabel l=new JLabel(label);l.setFont(FONT_BOLD);p.add(l,g);
        g.gridx=1;g.weightx=0.75;p.add(c,g);
    }

    // ---------- SKIN ----------
    ImageIcon loadSkinIcon(String skinFile, int width, int height) {
        java.net.URL resource = getClass().getResource("/com/richman/ui/" + skinFile);
        if (resource == null) {
            return new ImageIcon();
        }

        ImageIcon original = new ImageIcon(resource);
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    String skinDisplayName(String skinFile) {
        if ("poorman.png".equals(skinFile)) return "거지 스킨";
        if ("richman.png".equals(skinFile)) return "프로 거지 스킨";
        return skinFile;
    }

    void showSkinSelectionDialog() {
        JDialog dlg = new JDialog(this, "스킨 설정", true);
        dlg.setSize(620, 470);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("현재 보유중인 스킨");
        title.setFont(BASE_FONT.deriveFont(Font.BOLD, 22f));
        title.setForeground(TEXT);
        root.add(title, BorderLayout.NORTH);

        JPanel skinList = new JPanel(new GridLayout(1, Math.max(1, state.ownedSkins.size()), 14, 14));
        skinList.setOpaque(false);

        for (String skinFile : state.ownedSkins) {
            JPanel card = roundedPanel(WHITE, 18);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            JLabel image = new JLabel(loadSkinIcon(skinFile, 150, 150), SwingConstants.CENTER);
            image.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel name = new JLabel(skinDisplayName(skinFile), SwingConstants.CENTER);
            name.setFont(FONT_BOLD);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);

            boolean current = skinFile.equals(state.currentSkin);
            JLabel status = new JLabel(current ? "현재 사용 중" : "", SwingConstants.CENTER);
            status.setForeground(GREEN_DARK);
            status.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
            status.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton select = primaryButton(current ? "착용 중" : "착용하기");
            select.setAlignmentX(Component.CENTER_ALIGNMENT);
            select.setEnabled(!current);
            select.addActionListener(e -> {
                state.currentSkin = skinFile;
                
                // 서버에 착용 상태 저장
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                        body.addProperty("userId", currentUserId);
                        body.addProperty("alertThreshold", state.alertThreshold);
                        body.addProperty("currentSkin", state.currentSkin);
                        httpPost("http://localhost:8080/api/settings", body.toString());
                        return null;
                    }
                }.execute();
                
                dlg.dispose();
                refreshAll();
            });

            card.add(Box.createVerticalGlue());
            card.add(image);
            card.add(Box.createVerticalStrut(8));
            card.add(name);
            card.add(Box.createVerticalStrut(3));
            card.add(status);
            card.add(Box.createVerticalStrut(8));
            card.add(select);
            card.add(Box.createVerticalGlue());

            skinList.add(card);
        }

        root.add(skinList, BorderLayout.CENTER);

        JButton close = flatButton("닫기");
        close.addActionListener(e -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(close);
        root.add(bottom, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ---------- HTTP 유틸리티 ----------
    /** JSON Body를 POST하고 응답을 JsonObject로 반환. 오류 시 null 반환. */
    
    /** DELETE 요청 */
    JsonObject httpDelete(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void earnPointAsync(String pointType, int amount) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("userId", currentUserId);
                body.addProperty("pointType", pointType);
                body.addProperty("amount", amount);
                httpPost("http://localhost:8080/api/points/earn", body.toString());
                return null;
            }
            @Override
            protected void done() {
                state.points += amount;
                refreshAll();
            }
        }.execute();
    }

    JsonObject httpPost(String urlStr, String jsonBody) {
        return httpPost(urlStr, jsonBody, "POST");
    }

    JsonObject httpPost(String urlStr, String jsonBody, String method) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** GET 요청을 보내고 응답을 JsonObject로 반환. 오류 시 null 반환. */
    JsonObject httpGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return JsonParser.parseString(sb.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static String won(long n){
        return String.format("%,d원",n);
    }

    static String esc(String s){
        return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    // ---------- Custom components ----------
    static class RoundedPanel extends JPanel {
        int radius;
        RoundedPanel(Color bg, int radius){
            this.radius = radius;
            setBackground(bg); //  색상을 컴포넌트 내부에 제대로 저장!
            setOpaque(false);
        }
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground()); //  동적으로 바뀌는 배경색 적용
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
            g2.setColor(new Color(228, 232, 237));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class SpeechBubblePanel extends JPanel {
        Color bg;
        int radius;
        int tailSize = 12;

        SpeechBubblePanel(Color bg, int radius) {
            this.bg = bg;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 1;
            int height = getHeight() - 1;
            int bubbleHeight = height - tailSize;

            java.awt.geom.RoundRectangle2D.Float rect = new java.awt.geom.RoundRectangle2D.Float(0, 0, width, bubbleHeight, radius, radius);
            
            int tailX = width / 2;
            int tailY = bubbleHeight;
            java.awt.Polygon tail = new java.awt.Polygon(
                new int[]{tailX - 10, tailX + 10, tailX},
                new int[]{tailY, tailY, tailY + tailSize},
                3
            );

            java.awt.geom.Area area = new java.awt.geom.Area(rect);
            area.add(new java.awt.geom.Area(tail));

            g2.setColor(bg);
            g2.fill(area);

            g2.setColor(new Color(228, 232, 237));
            g2.draw(area);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class CircleBorder extends AbstractBorder {
        Color color; int diameter;
        CircleBorder(Color color,int diameter){this.color=color;this.diameter=diameter;}
        public Insets getBorderInsets(Component c){return new Insets(2,2,2,2);}
        public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setColor(color);g2.drawOval(x,y,w-1  ,h-1);g2.dispose();
        }
    }

    public static void main(String[] args){
        try {
            //  모서리 둥글게 (기존에 추가하신 코드)
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("TextComponent.arc", 999);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 999);

            //  [추가] 나머지 컴포넌트들 숨통 틔우기
            UIManager.put("ComboBox.padding", new Insets(8, 14, 8, 14)); // 콤보박스 여백
            UIManager.put("CheckBox.iconTextGap", 10); // 체크박스 네모와 글자 사이 간격
            UIManager.put("RadioButton.iconTextGap", 10); // 동그라미와 글자 사이 간격
            UIManager.put("List.cellMargins", new Insets(12, 16, 12, 16)); // 커뮤니티, 랭킹 리스트 여백
            UIManager.put("Table.cellMargins", new Insets(10, 14, 10, 14)); // 통계 표 안쪽 여백
            
            //  [새로 추가] 글씨와 테두리 사이의 여백(Padding) 넉넉하게 주기!
            UIManager.put("Button.margin", new Insets(10, 20, 10, 20)); // 버튼 위, 좌, 아래, 우 여백
            UIManager.put("TextComponent.margin", new Insets(10, 14, 10, 14)); // 텍스트 입력창 여백
            // ... (기존 버튼, 콤보박스 여백 설정들 유지) ...
            
            //  [여기서부터 추가/수정] 탭 메뉴를 토스 감성의 둥근 알약 스타일로 튜닝!
            UIManager.put("TabbedPane.tabType", "card"); // 탭 모양을 깔끔한 독립 카드형으로 변경
            UIManager.put("TabbedPane.tabArc", 999); // 탭 모서리 완전 둥글게 (알약 쉐입)
            UIManager.put("TabbedPane.selectedBackground", WHITE); // 선택된 탭은 하얗게 강조
            UIManager.put("TabbedPane.background", new Color(238, 244, 241)); // 안 선택된 탭은 연한 민트로 배경과 융화
            UIManager.put("TabbedPane.selectedForeground", new Color(42, 48, 56)); // 선택된 글씨는 진하게
            UIManager.put("TabbedPane.unselectedForeground", new Color(112, 124, 141)); // 안 선택된 글씨는 연하게
            UIManager.put("TabbedPane.tabInsets", new Insets(12, 24, 12, 24));
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 20, 0)); 
            UIManager.put("TabbedPane.contentAreaColor", new Color(0, 0, 0, 0));
            UIManager.put("TabbedPane.contentAreaColor", new Color(0, 0, 0, 0)); // 탭 아래 칙칙한 회색 테두리 선 완전 제거
            UIManager.put("TabbedPane.focusColor", new Color(0, 0, 0, 0)); // 클릭 시 생기는 촌스러운 포커스 선 제거
            
            UIManager.setLookAndFeel(new FlatLightLaf());

            // FlatLaf가 기본 폰트를 덮어씌우는 것을 방지하기 위해 다시 한 번 커스텀 폰트 적용
            UIManager.put("defaultFont", BASE_FONT.deriveFont(Font.PLAIN, 14f));
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            Font defaultFont = BASE_FONT.deriveFont(Font.PLAIN, 14f);
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource || value instanceof Font) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource(defaultFont));
                }
            }
        } catch (Exception ex) {
            System.err.println("FlatLaf 초기화 실패");
        }

        SwingUtilities.invokeLater(()->{
            new GeojiTalchulApp().setVisible(true);
        });
    }
}



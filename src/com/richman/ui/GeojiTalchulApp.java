package com.richman.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeojiTalchulApp extends JFrame {

    // ---------- Theme ----------
    // 🌟 배경은 가장 밝고 따뜻한 베이지색 적용 (#F7EDDA)
    static final Color BG = Color.decode("#F7EDDA"); 
    static final Color WHITE = Color.WHITE;
    
    // 🌟 글씨는 가독성을 위해 어둡게 유지
    static final Color TEXT = new Color(42, 48, 56); 
    static final Color MUTED = new Color(112, 124, 141);
    
    // 🌟 테두리(Border)는 가장 연한 민트/그레이 톤으로 부드럽게 (#C5DEDA)
    static final Color BORDER = Color.decode("#C5DEDA"); 

    // 🌟 앱의 메인 브랜드 컬러 (버튼, 차트, 강조 효과에 사용)
    static final Color NAVY = Color.decode("#4F7670"); // 기존 칙칙한 네이비 대신 딥 그린 적용
    static final Color GREEN_DARK = Color.decode("#4F7670"); // 프라이머리 버튼 (가장 진하고 묵직한 색)
    static final Color GREEN = Color.decode("#659F7C"); // 서브 메인 컬러
    static final Color GREEN_PALE = Color.decode("#92BEA9"); // 옅은 하이라이트 색상
    static final Color BLUE = Color.decode("#9CB4D4"); // 뮤트 블루 (차트 및 포인트)

    // 차트 및 지출(마이너스) 표시에 필요한 기본 색상은 유지
    static final Color RED = new Color(219, 87, 91); 
    static final Color ORANGE = new Color(232, 155, 72); 
    static final Color PURPLE = new Color(132, 103, 175); 

    static final Font FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
    static final Font FONT_BOLD = new Font("Malgun Gothic", Font.BOLD, 14);
    static final Font FONT_TITLE = new Font("Malgun Gothic", Font.BOLD, 25);

    final AppState state = new AppState();
    String dismissedFixedCandidateKey = "";
    final JPanel content = new JPanel(new CardLayout());
    final JLabel pointLabel = new JLabel();
    final JLabel userLabel = new JLabel("프로거지 님");

    HomePanel homePanel;
    CalendarPanel calendarPanel;
    StatisticsPanel statisticsPanel;
    CommunityPanel communityPanel;
    MyStorePanel myStorePanel;

    // 🌟 최상위 화면(라우팅) 관리 변수
    final JPanel rootContainer = new JPanel(new CardLayout());
    JPanel loginPanel;
    JPanel signupPanel;
    JPanel mainShell;

    // 🌟 로그인 세션 정보
    int currentUserId = -1;

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
    }

    // 🌟 여기서부터 복사해서 기존 buildLoginPanel, buildSignupPanel 덮어쓰기!
    
    JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG);

        // 중앙 로그인 박스 (둥근 테두리)
        JPanel card = roundedPanel(WHITE, 20);
        card.setLayout(new BorderLayout(0, 25));
        card.setBorder(new EmptyBorder(50, 60, 50, 60));

        JLabel title = new JLabel("거지탈출", SwingConstants.CENTER);
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 32));
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
        pwField.setFont(FONT);
        
        form.add(idField);
        form.add(pwField);
        card.add(form, BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnBox = new JPanel(new GridLayout(2, 1, 0, 10));
        btnBox.setOpaque(false);
        btnBox.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton loginBtn = primaryButton("로그인");
        loginBtn.setForeground(Color.WHITE); // 🌟 글자는 흰색으로 명확하게
        loginBtn.setBackground(new Color(41, 128, 185)); // 🌟 눈에 확 띄는 쨍한 파란색 계열 적용
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
                            String userName = res.has("userName") ? res.get("userName").getAsString() : loginId;
                            userLabel.setText(userName + " 님");
                            idField.setText("");
                            pwField.setText("");
                            ((CardLayout) rootContainer.getLayout()).show(rootContainer, "MAIN_APP");
                            refreshAll();
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
        JPasswordField signupPwField = new JPasswordField(15);
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
        JLabel avatar = new JLabel("U", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(232, 237, 241));
        avatar.setForeground(MUTED);
        avatar.setBorder(new CircleBorder(new Color(232,237,241), 28));
        avatar.setPreferredSize(new Dimension(42, 42));

        JPanel userText = new JPanel();
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        userText.setBackground(WHITE);
        userText.add(userLabel);
        pointLabel.setForeground(GREEN_DARK);
        pointLabel.setFont(FONT_BOLD);
        userText.add(pointLabel);
        // ... (avatar, userText 세팅 코드 유지) ...
        user.add(avatar, BorderLayout.WEST);
        user.add(userText, BorderLayout.CENTER);

        // 🌟 [추가] 마우스 커서를 손가락 모양으로 바꾸고 클릭 이벤트 달기!
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

    // 🌟 내 정보 수정 팝업창 (사진의 부드러운 테마 적용)
    void showUserInfoEditDialog() {
        JDialog dlg = new JDialog(this, "내 정보 수정", true);
        dlg.setSize(420, 350);
        dlg.setLocationRelativeTo(this);

        // 팝업 배경도 우리의 감성적인 베이지색(BG)으로 통일
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG); 
        root.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 둥근 알약 느낌의 하얀색 카드 패널
        JPanel card = roundedPanel(WHITE, 40); 
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 20, 30, 20)); // 안쪽 여백 빵빵하게
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 10, 12, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int r = 0;
        // 기존 닉네임("프로거지 님"에서 " 님" 제거)을 기본값으로 세팅
        addFormRow(card, g, r++, "닉네임", new JTextField(userLabel.getText().replace(" 님", "")));
        addFormRow(card, g, r++, "기존 비밀번호", new JPasswordField());
        addFormRow(card, g, r++, "새 비밀번호", new JPasswordField());
        
        root.add(card, BorderLayout.CENTER);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton cancel = flatButton("취소");
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = primaryButton("정보 저장");
        save.addActionListener(e -> {
            JOptionPane.showMessageDialog(dlg, "회원 정보가 성공적으로 수정되었습니다.", "수정 완료", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
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

        JButton bell = new JButton("♟");
        styleIconButton(bell);
        bell.addActionListener(e -> showNotifications());

        // 🌟 원흉이었던 setBorder 삭제 완료 (완벽한 알약 쉐입 적용)
        JButton setting = primaryButton("설정");
        setting.addActionListener(e -> showSettings());

        // 🌟 로그아웃 버튼 (FlatLaf 알약 쉐입 적용)
        JButton logout = flatButton("로그아웃");
        logout.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(content, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                ((CardLayout) rootContainer.getLayout()).show(rootContainer, "AUTH_LOGIN");
            }
        });

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
        String msg = state.budgetUsage() >= 1.0
                ? "예산을 초과했습니다.\n지출 내역을 확인해 보세요."
                : state.fixedExpenseCandidateCount() > 0
                ? "최근 3개월 반복 지출 후보가 " + state.fixedExpenseCandidateCount() + "건 발견되었습니다."
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
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 22));
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

        JCheckBox push = new JCheckBox("예산 경고 알림", true);
        JCheckBox fixed = new JCheckBox("고정지출 후보 자동 알림", true);
        push.setBackground(WHITE);
        fixed.setBackground(WHITE);
        form.add(push);
        form.add(Box.createVerticalStrut(6));
        form.add(fixed);

        root.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setBackground(WHITE);
        JButton cancel = new JButton("취소");
        cancel.setFocusPainted(false);
        JButton save = primaryButton("설정 저장");
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String raw = budgetField.getText().replace(",", "").trim();
            try {
                long newBudget = Long.parseLong(raw);
                if (newBudget <= 0) throw new NumberFormatException();
                state.budget = newBudget;
                dlg.dispose();
                refreshAll();
                JOptionPane.showMessageDialog(this,
                        "목표 예산이 " + won(newBudget) + "으로 설정되었습니다.",
                        "설정 완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "목표 예산은 0보다 큰 숫자로 입력해주세요.",
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
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

        HomePanel() {
            setLayout(new BorderLayout(0, 20)); // 상단 알림 배너와 본문 사이의 여백 20px
            setBackground(BG);
            setBorder(new EmptyBorder(30, 30, 30, 30)); // 화면 전체 외곽 여백

            // 1. 상단 알림 배너
            alertBanner = buildAlertBanner();
            add(alertBanner, BorderLayout.NORTH);

            // 2. 메인 대시보드 뼈대 (위 1줄, 아래 1줄)
            JPanel centerGrid = new JPanel(new GridLayout(2, 1, 0, 20)); // 위아래 간격 20px
            centerGrid.setOpaque(false);

            // 🌟 [첫 번째 줄] 예산 카드 (넓게) + 간편 등록 (고정 너비 400px)
            JPanel topRow = new JPanel(new BorderLayout(20, 0)); // 좌우 카드 간격 20px
            topRow.setOpaque(false);
            topRow.add(buildBudgetCard(), BorderLayout.CENTER); 
            
            JPanel quickWrapper = new JPanel(new BorderLayout());
            quickWrapper.setOpaque(false);
            quickWrapper.add(buildQuickExpenseCard(), BorderLayout.CENTER);
            quickWrapper.setPreferredSize(new Dimension(400, 0)); // 간편 등록이 무식하게 커지지 않게 방어
            topRow.add(quickWrapper, BorderLayout.EAST);

            // 🌟 [두 번째 줄] 파이 차트 (고정 너비 450px) + 최근 지출 (넓게)
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

        JPanel buildAlertBanner() {
            // 🌟 직각 JPanel 대신 토스 감성 둥근 패널(알약 쉐입)로 교체!
            JPanel banner = new RoundedPanel(RED, 40); 
            banner.setLayout(new BorderLayout());
            banner.setBorder(new EmptyBorder(15, 25, 15, 25)); // 넉넉한 여백
            
            alertLabel.setFont(FONT_BOLD);
            banner.add(alertLabel, BorderLayout.CENTER);

            JButton detail = new JButton("상세 보기");
            detail.setFocusPainted(false);
            // 🌟 여기서도 setBorder 삭제!
            detail.setBackground(WHITE);
            detail.setForeground(RED);
            detail.addActionListener(e -> {
                if (state.budgetUsage() >= 1.0 || state.budgetUsage() >= state.alertThreshold / 100.0) {
                    showCard("STATS");
                } else if (state.fixedExpenseCandidateCount() > 0 && !state.fixedCandidateKey().equals(dismissedFixedCandidateKey)) {
                    showFixedExpenseCandidateDialog();
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
            title.setFont(new Font("Malgun Gothic", Font.BOLD, 22));
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
                amount.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
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
            title.setFont(new Font("Malgun Gothic", Font.BOLD, 18));

            // 소비 금액을 가장 크게 표시하고, 아래에 목표 예산에서 차감한 잔여 금액을 표시
            spentLabel.setForeground(WHITE);
            spentLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 45));
            remainLabel.setForeground(new Color(240, 245, 234));
            remainLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 21));
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

            ImageIcon icon = new ImageIcon(
                    getClass().getResource("/com/richman/ui/poorman.png")
            );

            // 이미지 크기 조절
            Image image = icon.getImage().getScaledInstance(
                    160, 160, Image.SCALE_SMOOTH
            );

            JLabel character = new JLabel(new ImageIcon(image));
            character.setHorizontalAlignment(SwingConstants.CENTER);
            character.setPreferredSize(new Dimension(220, 180));

            card.add(character, BorderLayout.EAST);
            return card;
        }

        JPanel buildQuickExpenseCard() {
            JPanel card = roundedPanel(WHITE, 18);
            card.setPreferredSize(new Dimension(355, 235));
            card.setLayout(new BorderLayout());
            JLabel title = new JLabel("지출 간편 등록");
            title.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
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
            detail.setBackground(GREEN_PALE); // 🌟 칙칙한 회색 대신 예쁜 뮤트 그린 컬러 적용!
            detail.setFocusPainted(false);
            detail.addActionListener(e -> openExpenseDialog(null, null));
            grid.add(detail);
            card.add(grid, BorderLayout.CENTER);
            return card;
        }

        void addQuickButton(JPanel grid, String text, String large, String medium) {
            JButton b = new JButton(text);
            b.setFont(FONT_BOLD);
            b.setForeground(NAVY); // 🌟 글씨는 또렷한 네이비/다크그린
            
            // 🌟 배경을 칙칙한 흰색 대신, 테마와 어울리는 아주 연하고 부드러운 민트 베이지 톤으로 변경
            b.setBackground(new Color(238, 244, 241)); 
            
            b.setFocusPainted(false);
            b.addActionListener(e -> openExpenseDialog(large, medium));
            grid.add(b);
        }

        void addQuickButton(JPanel p, String text) {
            JButton b = new JButton(text);
            b.setFont(FONT_BOLD);
            
            // 🌟 배경을 칙칙한 흰색 대신, 테마와 어울리는 아주 연하고 부드러운 민트 베이지 톤으로 변경
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
            t.setFont(new Font("Malgun Gothic", Font.BOLD, 19));
            JButton all = flatButton("전체 보기");
            all.addActionListener(e -> showCard("STATS"));
            head.setBorder(new EmptyBorder(22, 22, 10, 22));
            head.add(t, BorderLayout.WEST);
            head.add(all, BorderLayout.EAST);
            card.add(head, BorderLayout.NORTH);

            recentList.setBackground(WHITE);
            recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
            card.add(recentList, BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            long spent = state.totalSpent();
            long remain = state.budget - spent;
            spentLabel.setText(won(spent));
            remainLabel.setText("목표 예산까지: " + won(remain));
            budgetLabel.setText("목표 예산: " + won(state.budget));

            double usage = state.budget == 0 ? 0 : spent / (double) state.budget;
            boolean activeBudgetAlert = usage >= state.alertThreshold / 100.0;
            boolean activeFixedAlert = state.fixedExpenseCandidateCount() > 0
                    && !state.fixedCandidateKey().equals(dismissedFixedCandidateKey);

            if (usage >= 1) {
                alertLabel.setText("이번 달 예산을 초과했습니다. 남은 기간 동안 지출을 줄이세요.");
            } else if (activeBudgetAlert) {
                alertLabel.setText("비용비용! 이번 달 예산 " + (int)(usage * 100) + "% 사용. 남은 기간 동안 지출을 줄이세요.");
            } else if (activeFixedAlert) {
                alertLabel.setText("●  고정 지출 후보 " + state.fixedExpenseCandidateCount() + "건을 발견했습니다. 확인해 보세요.");
            } else {
                alertLabel.setText("알림 확인 완료");
            }

            // 고정지출 후보 알림을 확인한 뒤에는 홈 배경색과 동일하게 만들어 눈에 띄지 않게 처리
            if (alertBanner != null) {
                boolean visibleAlert = activeBudgetAlert || activeFixedAlert;
                alertBanner.setBackground(visibleAlert ? RED : BG);
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
                    .limit(6).collect(Collectors.toList());
            for (Expense e : list) recentList.add(expenseRow(e));
            recentList.revalidate();
            recentList.repaint();
            homePie.repaint();
        }

        JPanel expenseRow(Expense e) {
            // 최근 지출 내역: 분류와 소분류를 두 줄로 표시
            // 예) 08.15 (토)   식비 > 배달음식
            //                    버거킹
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
            cat.setFont(new Font("Malgun Gothic", Font.PLAIN, 13));

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
            return row;
        }
    }

    // ---------- CALENDAR ----------
    class CalendarPanel extends JPanel {
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
            total.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
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
                        new Font("Malgun Gothic", Font.PLAIN, 12)
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

                del.addActionListener(e -> {
                    state.expenses.remove(x);
                    dlg.dispose();
                    refreshAll();
                    showDateDetail(date);
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
            top.add(chartCard("카테고리별 지출 비중", pie));
            top.add(chartCard("최근 6개월 지출 추이", trend));
            add(top, BorderLayout.NORTH);

            JPanel detail = roundedPanel(WHITE, 18);
            detail.setLayout(new BorderLayout(10,10));
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title = new JLabel("지출 내역 상세");
            title.setFont(new Font("Malgun Gothic", Font.BOLD, 19));
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
            JScrollPane sp = new JScrollPane(detailTable);
            sp.setBorder(BorderFactory.createEmptyBorder());
            detail.add(sp, BorderLayout.CENTER);
            add(detail, BorderLayout.CENTER);
        }

        JPanel chartCard(String title, JComponent chart) {
            JPanel p = roundedPanel(WHITE, 18);
            p.setLayout(new BorderLayout());
            JLabel l = new JLabel(title);
            l.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
            l.setBorder(new EmptyBorder(18,18,8,18));
            p.add(l, BorderLayout.NORTH);
            p.add(chart, BorderLayout.CENTER);
            return p;
        }

        void rebuildExpenseTable() {
            if (tableModel == null) return;
            tableModel.setRowCount(0);
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
            }
            if (list.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "지출 내역 없음", "", "", ""});
            }
        }

        void refresh() {
            pie.repaint();
            trend.repaint();
            rebuildExpenseTable();
        }
    }

    class PieChart extends JPanel {
        PieChart() { setPreferredSize(new Dimension(400, 300)); setBackground(WHITE); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Map<String,Long> map = state.largeTotals();
            long total = map.values().stream().mapToLong(Long::longValue).sum();
            if (total == 0) {
                g2.setColor(MUTED);
                g2.drawString("지출 데이터가 없습니다.", getWidth()/2-60, getHeight()/2);
                g2.dispose();
                return;
            }

            Color[] colors = {GREEN, BLUE, ORANGE, PURPLE, new Color(90,150,130), new Color(205,128,120), new Color(150,150,95)};

            // 🌟 1. 차트가 잘리지 않게 크기와 위치를 동적으로 조절
            int diameter = Math.min(200, getHeight() - 110); 
            int x = (getWidth() - diameter) / 2; // 가로 중앙 정렬
            int y = 20; // 위쪽 여백
            
            double start = 0;
            int i = 0;
            String maxCategory = "";
            long maxVal = -1;
            
            // 파이 조각 그리기 & 가장 높은 비율 찾기
            for (Map.Entry<String,Long> en : map.entrySet()) {
                double angle = 360.0 * en.getValue() / total;
                g2.setColor(colors[i++ % colors.length]);
                g2.fill(new Arc2D.Double(x, y, diameter, diameter, start, angle, Arc2D.PIE));
                start += angle;
                
                if (en.getValue() > maxVal) {
                    maxVal = en.getValue();
                    maxCategory = en.getKey();
                }
            }

            // 🍩 2. 도넛 모양 만들기 (가운데를 배경색으로 파내기)
            int thickness = 25; // 도넛 두께
            int innerDiameter = diameter - (thickness * 2);
            int innerX = x + thickness;
            int innerY = y + thickness;
            g2.setColor(WHITE);
            g2.fillOval(innerX, innerY, innerDiameter, innerDiameter);

         // 도넛 중앙에 "대분류" 표시
            String centerTitle = "대분류";

            g2.setColor(TEXT);
            g2.setFont(new Font("Malgun Gothic", Font.BOLD, 18));

            FontMetrics fm = g2.getFontMetrics();

            int textX = x + (diameter - fm.stringWidth(centerTitle)) / 2;
            int textY = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(centerTitle, textX, textY);

            // 🎨 4. 하단 범례(Legend) 줄맞춤 및 잘림 방지 (2열 배치)
            int legendTop = y + diameter + 25; // 차트와 텍스트 사이 여백 확보
            int colWidth = getWidth() / 2; // 🌟 3열 -> 2열로 분할하여 긴 글자 겹침 완벽 방지
            int row = 0, col = 0;
            i = 0;
            
            for (Map.Entry<String,Long> en : map.entrySet()) {
                int lx = 40 + col * colWidth; // 셀 좌측 여백 넉넉하게
                int ly = legendTop + row * 28; // 줄 간격 28px
                
                g2.setColor(colors[i++ % colors.length]);
                g2.fillRoundRect(lx, ly - 12, 14, 14, 4, 4);
                
                g2.setColor(TEXT);
                g2.setFont(FONT);
                int pct = (int)Math.round(en.getValue() * 100.0 / total);
                g2.drawString(en.getKey() + " " + pct + "%", lx + 22, ly);

                col++;
                if (col >= 2) { // 🌟 2개 그려지면 다음 줄로 넘김
                    col = 0;
                    row++;
                }
            }
            g2.dispose();
        }
    }

    class TrendChart extends JPanel {
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

            YearMonth now=YearMonth.of(2026,8);
            long max=1;
            long[] vals=new long[6];
            for(int i=0;i<6;i++){
                YearMonth ym=now.minusMonths(5-i);
                vals[i]=state.expenses.stream().filter(e -> YearMonth.from(e.date).equals(ym)).mapToLong(e->e.amount).sum();
                max=Math.max(max, vals[i]);
            }
            g2.setColor(BLUE);
            g2.setStroke(new BasicStroke(3));
            int px=left, py=top+h-(int)(vals[0]*1.0/max*h);
            for(int i=0;i<6;i++){
                int xx=left+(w*i/5);
                int yy=top+h-(int)(vals[i]*1.0/max*h);
                if(i>0) g2.drawLine(px,py,xx,yy);
                g2.fillOval(xx-4,yy-4,8,8);
                g2.setColor(TEXT);
                g2.setFont(new Font("Malgun Gothic",Font.PLAIN,11));
                g2.drawString(now.minusMonths(5-i).getMonthValue()+"월",xx-8,top+h+22);
                g2.setColor(BLUE);
                px=xx; py=yy;
            }
            g2.setColor(TEXT);
            g2.setFont(FONT_BOLD);
            g2.drawString("최근 6개월", left, 18);
            g2.dispose();
        }
    }

    // ---------- COMMUNITY ----------
    class CommunityPanel extends JPanel {
        JTabbedPane tabs = new JTabbedPane();
        DefaultListModel<String> rankModel = new DefaultListModel<>();
        DefaultListModel<String> feedModel = new DefaultListModel<>();
        DefaultListModel<String> challengeModel = new DefaultListModel<>();

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
            JLabel sub = new JLabel("목표 지출금액 달성률 또는 누적 포인트를 기준으로 순위를 계산합니다.");
            sub.setForeground(MUTED);
            JPanel htext = new JPanel();
            htext.setOpaque(false); htext.setLayout(new BoxLayout(htext,BoxLayout.Y_AXIS));
            htext.add(title); htext.add(Box.createVerticalStrut(6)); htext.add(sub);
            JButton rule = flatButton("랭킹 기준");
            rule.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "기본 점수 = 목표 예산 대비 지출 준수율 + 활동 포인트 일부 반영\n\n실제 서버에서는 DB의 USER/BUDGET/EXPENSE/POINT_HISTORY를 사용해 집계합니다.",
                    "랭킹 기준", JOptionPane.INFORMATION_MESSAGE));
            head.add(htext,BorderLayout.WEST); head.add(rule,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);

            JList<String> list = new JList<>(rankModel);
            list.setFont(new Font("Malgun Gothic",Font.BOLD,16));
            list.setFixedCellHeight(62);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // 1위 금메달, 2위 은메달, 3위 동메달 배경색
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    label.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
                    label.setBorder(new EmptyBorder(10, 18, 10, 18));

                    // ... 기존 if-else 블록을 아래 코드로 교체 ...
                    if (index == 0) {
                        label.setBackground(new Color(230, 240, 235)); // 연한 뮤트 그린
                        label.setForeground(NAVY);
                    } else if (index == 1) {
                        label.setBackground(new Color(240, 245, 248)); // 연한 뮤트 블루
                        label.setForeground(NAVY);
                    } else if (index == 2) {
                        label.setBackground(new Color(248, 245, 240)); // 연한 베이지
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

            JLabel mine = new JLabel("  내 순위  17위    목표 700,000원   /   현재 650,000원");
            mine.setOpaque(true); mine.setBackground(GREEN_PALE);
            mine.setBorder(new EmptyBorder(16,16,16,16));
            mine.setFont(FONT_BOLD);
            p.add(mine,BorderLayout.SOUTH);
            return p;
        }

        JPanel buildChallenges() {
            JPanel p=roundedPanel(WHITE,18);
            p.setLayout(new BorderLayout(10,10));
            JPanel head=new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title=new JLabel("그룹 지출 챌린지");
            title.setFont(FONT_TITLE);
            JButton create=primaryButton("+ 챌린지 생성");
            create.addActionListener(e->createChallenge());
            head.add(title,BorderLayout.WEST); head.add(create,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);

            JList<String> list=new JList<>(challengeModel);
            list.setFont(FONT);
            list.setFixedCellHeight(72);
            p.add(new JScrollPane(list),BorderLayout.CENTER);
            return p;
        }

        JPanel buildFeed() {
            JPanel p=roundedPanel(WHITE,18);
            p.setLayout(new BorderLayout(10,10));
            JPanel head=new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title=new JLabel("SNS 피드");
            title.setFont(FONT_TITLE);
            JButton write=primaryButton("+ 글쓰기");
            write.addActionListener(e->writePost());
            head.add(title,BorderLayout.WEST); head.add(write,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);
            JList<String> list=new JList<>(feedModel);
            list.setFont(FONT);
            list.setFixedCellHeight(105);
            p.add(new JScrollPane(list),BorderLayout.CENTER);
            return p;
        }

        void createChallenge() {
            JTextField name=new JTextField();
            JTextField goal=new JTextField("300000");
            JTextField reward=new JTextField("1000");
            JPanel p=new JPanel(new GridLayout(3,2,8,8));
            p.add(new JLabel("챌린지 이름")); p.add(name);
            p.add(new JLabel("목표 지출금액")); p.add(goal);
            p.add(new JLabel("보상 포인트")); p.add(reward);
            int r=JOptionPane.showConfirmDialog(this,p,"그룹 챌린지 생성",JOptionPane.OK_CANCEL_OPTION);
            if(r==JOptionPane.OK_OPTION){
                challengeModel.addElement(" "+name.getText()+"   | 목표 "+goal.getText()+"원 | 보상 "+reward.getText()+"P | 참여 1명");
                state.points += 50;
                refreshAll();
            }
        }

        void writePost() {
            JTextArea area=new JTextArea(6,35);
            int r=JOptionPane.showConfirmDialog(this,new JScrollPane(area),"게시물 작성",JOptionPane.OK_CANCEL_OPTION);
            if(r==JOptionPane.OK_OPTION && !area.getText().trim().isEmpty()){
                feedModel.add(0,"프로거지 님\n"+area.getText()+"\n\n♡ 좋아요 0     댓글 0     +30P");
                state.points += 30;
                refreshAll();
            }
        }

        void refresh() {
            rankModel.clear();
            rankModel.addElement("	 1위   절약왕김씨       목표 500,000원   실제 420,000원   달성률 84%");
            rankModel.addElement("	 2위   거지탈출러       목표 800,000원   실제 700,000원   달성률 87.5%");
            rankModel.addElement("	 3위   소비요정         목표 400,000원   실제 380,000원   달성률 95%");
            rankModel.addElement("   4위   통장지킴이       목표 900,000원   실제 880,000원   달성률 97.8%");
            rankModel.addElement("   5위   절약초보         목표 600,000원   실제 610,000원   달성률 101.7%");

            if(challengeModel.isEmpty()){
                challengeModel.addElement(" 30만원 식비 줄이기 | 목표 300,000원 | 보상 1,000P | 5명 참여");
                challengeModel.addElement(" 교통비 절약전 | 목표 100,000원 | 보상 500P | 8명 참여");
            }
            if(feedModel.isEmpty()){
                feedModel.addElement("프로거지 님\n이번 달 외식비를 20만원 아래로 줄여보겠습니다!\n\n♡ 좋아요 32     댓글 8     +30P");
                feedModel.addElement("절약왕김씨\n고정지출을 정리하니까 생각보다 새는 돈이 많네요.\n\n♡ 좋아요 21     댓글 4     +30P");
            }
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
            myPoints.setFont(new Font("Malgun Gothic",Font.BOLD,20));
            myPoints.setForeground(GREEN_DARK);
            head.add(title,BorderLayout.WEST); head.add(myPoints,BorderLayout.EAST);
            p.add(head,BorderLayout.NORTH);

            JPanel items=new JPanel(new GridLayout(0,3,14,14));
            items.setBackground(WHITE);
            addShopItem(items," 카페 쿠폰","커피 1잔","3,000P",3000);
            addShopItem(items," 식사 쿠폰","식사 할인권","5,000P",5000);
            addShopItem(items," 음원 이용권","1개월","7,000P",7000);
            addShopItem(items," 영화 관람권","영화 1편","10,000P",10000);
            addShopItem(items," 랜덤 박스","랜덤 보상","15,000P",15000);
            addShopItem(items," 프로거지 스킨","캐릭터 꾸미기","20,000P",20000);
            p.add(items,BorderLayout.CENTER);
            return p;
        }

        void addShopItem(JPanel parent,String icon,String name,String price,int cost){
            JPanel c=roundedPanel(new Color(249,250,251),14);
            c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));

            JLabel i=new JLabel(icon,SwingConstants.CENTER);
            i.setFont(new Font("Malgun Gothic",Font.PLAIN,30));

            JLabel n=new JLabel(name);
            n.setFont(FONT_BOLD);

            JLabel pr=new JLabel(price);
            pr.setForeground(GREEN_DARK);

            JButton buy=primaryButton("교환");
            buy.setAlignmentX(Component.CENTER_ALIGNMENT);

            buy.addActionListener(e->{
                if(state.points>=cost){
                    state.points-=cost;
                    refreshAll();

                    JOptionPane.showMessageDialog(
                        this,
                        name+" 교환 완료!",
                        "포인트 상점",
                        JOptionPane.INFORMATION_MESSAGE
                    );
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
            c.add(Box.createVerticalStrut(8));
            c.add(n);
            c.add(pr);
            c.add(Box.createVerticalStrut(8));
            c.add(buy);
            c.add(Box.createVerticalGlue());

            parent.add(c);
        }

        void refresh() {
            myPoints.setText("보유 포인트  " + state.points + " P");
        }
    }

    // ---------- Expense dialog ----------
    void openExpenseDialog(String presetLarge, String presetMedium) {
        openExpenseDialog(presetLarge,presetMedium,LocalDate.now());
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
        save.addActionListener(e->{
            try{
                long won=Long.parseLong(amount.getText().replace(",","").trim());
                LocalDate d=LocalDate.parse(date.getText().trim());
                String l=(String)large.getSelectedItem();
                String m=(String)medium.getSelectedItem();
                String s=small.getText().trim().isEmpty()?item.getText():small.getText();
                if(s.trim().isEmpty()) throw new IllegalArgumentException("소분류를 입력하세요.");

                state.addExpense(l,m,s,item.getText().trim(),won,d);
                state.points += 20;
                dlg.dispose();
                refreshAll();
                if(state.budgetUsage()>=1) JOptionPane.showMessageDialog(this,
                        "지출이 저장되었습니다.\n\n⚠ 예산을 초과했습니다.","예산 경고",JOptionPane.WARNING_MESSAGE);
                else JOptionPane.showMessageDialog(this,"지출이 저장되었습니다. +20P","저장 완료",JOptionPane.INFORMATION_MESSAGE);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(dlg,ex.getMessage(),"입력 오류",JOptionPane.ERROR_MESSAGE);
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
        List<String> largeCategories=Arrays.asList(
                "주거/통신","금융/보험","정기구독","식비","교통/차량",
                "생활/쇼핑","취미/여가","경조사/선물","의료/건강","유지/수리"
        );

        Map<String,List<String>> mediumMap=new LinkedHashMap<>();
        List<Expense> expenses=new ArrayList<>();
        Set<Integer> fixedExpenseIds=new HashSet<>();

        AppState(){
            mediumMap.put("주거/통신",Arrays.asList("월세","관리비","인터넷/휴대폰 요금"));
            mediumMap.put("금융/보험",Arrays.asList("대출 원리금","실손보험","적금/투자"));
            mediumMap.put("정기구독",Arrays.asList("OTT","음원 스트리밍","렌털료"));
            mediumMap.put("식비",Arrays.asList("장보기","외식비","배달음식","카페/간식"));
            mediumMap.put("교통/차량",Arrays.asList("대중교통","택시","주유비"));
            mediumMap.put("생활/쇼핑",Arrays.asList("생필품","의류","미용실/화장품"));
            mediumMap.put("취미/여가",Arrays.asList("문화생활","운동/학원비","여행"));
            mediumMap.put("경조사/선물",Arrays.asList("축의금/부의금","명절 선물","기념일 선물"));
            mediumMap.put("의료/건강",Arrays.asList("병원 진료비","약국","건강검진"));
            mediumMap.put("유지/수리",Arrays.asList("가전/가구 교체","차량 수리비","세금"));

            // Home reference-style mock data.
            addExpense("식비","배달음식","버거킹 몬스터와퍼 세트","버거킹 몬스터와퍼 세트",10500,LocalDate.of(2026,8,15));
            addExpense("주거/통신","인터넷/휴대폰 요금","알뜰폰 통신비 자동이체","알뜰폰 통신비 자동이체",24000,LocalDate.of(2026,8,14));
            addExpense("경조사/선물","축의금/부의금","친구 결혼식 축의금","친구 결혼식 축의금",50000,LocalDate.of(2026,8,12));
            addExpense("식비","카페/간식","아메리카노","아메리카노",4500,LocalDate.of(2026,8,10));
            addExpense("교통/차량","대중교통","버스","버스",1500,LocalDate.of(2026,8,9));
            addExpense("정기구독","OTT","넷플릭스","넷플릭스",17000,LocalDate.of(2026,8,5));
            addExpense("식비","외식비","김치찌개","김치찌개",9000,LocalDate.of(2026,8,3));
            addExpense("교통/차량","대중교통","지하철","지하철",1500,LocalDate.of(2026,8,2));
            // Repeated candidates
            addExpense("정기구독","OTT","넷플릭스","넷플릭스",17000,LocalDate.of(2026,7,5));
            addExpense("정기구독","OTT","넷플릭스","넷플릭스",17000,LocalDate.of(2026,6,5));
            addExpense("주거/통신","인터넷/휴대폰 요금","알뜰폰 통신비","알뜰폰 통신비",24000,LocalDate.of(2026,7,14));
            addExpense("주거/통신","인터넷/휴대폰 요금","알뜰폰 통신비","알뜰폰 통신비",24000,LocalDate.of(2026,6,14));
        }

        void addExpense(String large,String medium,String small,String item,long amount,LocalDate date){
            expenses.add(new Expense(nextId++,large,medium,small,item,amount,date,false));
        }

        long totalSpent(){ return expenses.stream().filter(e->YearMonth.from(e.date).equals(YearMonth.of(2026,8))).mapToLong(e->e.amount).sum(); }
        double budgetUsage(){ return budget==0?0:totalSpent()/(double)budget; }

        Map<String,Long> largeTotals(){
            return expenses.stream().filter(e->YearMonth.from(e.date).equals(YearMonth.of(2026,8)))
                    .collect(Collectors.groupingBy(e->e.large,LinkedHashMap::new,Collectors.summingLong(e->e.amount)));
        }

        List<Expense> fixedCandidates(){
            Map<String,List<Expense>> map=expenses.stream()
                    .filter(e->e.date.isAfter(LocalDate.of(2026,5,31)) && e.date.isBefore(LocalDate.of(2026,9,1)))
                    .collect(Collectors.groupingBy(e->e.medium+"|"+e.amount,LinkedHashMap::new,Collectors.toList()));
            return map.values().stream()
                    .filter(v->v.size()>=3)
                    .map(v->v.get(0))
                    .collect(Collectors.toList());
        }

        int fixedExpenseCandidateCount(){ return fixedCandidates().size(); }

        long fixedCandidateRepeatCount(Expense candidate){
            return expenses.stream()
                    .filter(e -> e.date.isAfter(LocalDate.of(2026,5,31)) && e.date.isBefore(LocalDate.of(2026,9,1)))
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
        // 🌟 기존에 전달받은 얕은 숫자(18 등)를 깡그리 무시하고,
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
        // 🌟 setBorder 삭제 (FlatLaf 알약 쉐입 유지)
        return b;
    }

    JButton flatButton(String text){
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD); b.setForeground(MUTED); b.setBackground(WHITE);
        b.setFocusPainted(false);
        // 🌟 setBorder 삭제
        return b;
    }

    JButton navButton(String text){
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setBackground(WHITE); 
        b.setForeground(MUTED);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(42, 42));
        
        // 🌟 [추가] 달력 화살표 버튼만 여백을 0으로 덮어써서 글자 짤림 방지!
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

    // ---------- HTTP 유틸리티 ----------
    /** JSON Body를 POST하고 응답을 JsonObject로 반환. 오류 시 null 반환. */
    JsonObject httpPost(String urlStr, String jsonBody) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
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
            setBackground(bg); // 🌟 색상을 컴포넌트 내부에 제대로 저장!
            setOpaque(false);
        }
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground()); // 🌟 동적으로 바뀌는 배경색 적용
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
            g2.setColor(new Color(228, 232, 237));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
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
            // 🌟 모서리 둥글게 (기존에 추가하신 코드)
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("TextComponent.arc", 999);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.thumbArc", 999);

            // 🌟 [추가] 나머지 컴포넌트들 숨통 틔우기
            UIManager.put("ComboBox.padding", new Insets(8, 14, 8, 14)); // 콤보박스 여백
            UIManager.put("CheckBox.iconTextGap", 10); // 체크박스 네모와 글자 사이 간격
            UIManager.put("RadioButton.iconTextGap", 10); // 동그라미와 글자 사이 간격
            UIManager.put("List.cellMargins", new Insets(12, 16, 12, 16)); // 커뮤니티, 랭킹 리스트 여백
            UIManager.put("Table.cellMargins", new Insets(10, 14, 10, 14)); // 통계 표 안쪽 여백
            
            // 🌟 [새로 추가] 글씨와 테두리 사이의 여백(Padding) 넉넉하게 주기!
            UIManager.put("Button.margin", new Insets(10, 20, 10, 20)); // 버튼 위, 좌, 아래, 우 여백
            UIManager.put("TextComponent.margin", new Insets(10, 14, 10, 14)); // 텍스트 입력창 여백
            // ... (기존 버튼, 콤보박스 여백 설정들 유지) ...
            
            // 🌟 [여기서부터 추가/수정] 탭 메뉴를 토스 감성의 둥근 알약 스타일로 튜닝!
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
        } catch (Exception ex) {
            System.err.println("FlatLaf 초기화 실패");
        }

        SwingUtilities.invokeLater(()->{
            new GeojiTalchulApp().setVisible(true);
        });
    }

	JPanel list = new JPanel(new GridLayout(0, 2, 12, 12));
}

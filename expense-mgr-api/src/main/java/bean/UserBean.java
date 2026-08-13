package bean;

import java.sql.Date;

/**
 * UserBean - myJava 스타일 회원 객체
 */
public class UserBean {
    private int userId;
    private String loginId;
    private String password;
    private String userName;
    private Date birthDate;
    private String gender;
    private String phone;
    private String job;
    private String address;
    private int income;
    private int pointBalance;

    public UserBean() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getIncome() { return income; }
    public void setIncome(int income) { this.income = income; }

    public int getPointBalance() { return pointBalance; }
    public void setPointBalance(int pointBalance) { this.pointBalance = pointBalance; }
}

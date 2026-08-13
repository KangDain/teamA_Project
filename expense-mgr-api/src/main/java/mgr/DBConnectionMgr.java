package mgr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * DBConnectionMgr - myJava 스타일 싱글톤 커넥션 풀 관리 클래스
 * 
 * [역할]
 *   Connection 객체를 미리 생성하여 관리하며,
 *   con = pool.getConnection() 대여 및 pool.freeConnection(con, pstmt, rs) 반납 구조를 제공합니다.
 */
public class DBConnectionMgr {

    private Vector<Connection> connections = new Vector<>(10);
    private String _driver = "com.mysql.cj.jdbc.Driver";
    private String _url = "jdbc:mysql://cdn.ditanet.duckdns.org:8306/richman?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private String _user = "root";
    private String _password = "dita2414";

    private static DBConnectionMgr instance = null;

    private DBConnectionMgr() {
        try {
            Class.forName(_driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DBConnectionMgr getInstance() {
        if (instance == null) {
            instance = new DBConnectionMgr();
        }
        return instance;
    }

    /**
     * DB 커넥션 대여 (getConnection)
     */
    public synchronized Connection getConnection() throws Exception {
        Connection con = null;
        if (connections.size() > 0) {
            con = connections.firstElement();
            connections.removeElementAt(0);
            if (con.isClosed()) {
                con = getConnection();
            }
        } else {
            con = DriverManager.getConnection(_url, _user, _password);
        }
        return con;
    }

    /**
     * Connection, Statement, ResultSet 자원 반납 (freeConnection)
     */
    public void freeConnection(Connection con, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (con != null) {
                if (connections.size() < 10) {
                    connections.addElement(con);
                } else {
                    con.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection con, PreparedStatement pstmt) {
        freeConnection(con, pstmt, null);
    }

    public void freeConnection(Connection con, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) {
                if (connections.size() < 10) {
                    connections.addElement(con);
                } else {
                    con.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void freeConnection(Connection con, Statement stmt) {
        freeConnection(con, stmt, null);
    }
}

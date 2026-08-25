package api;

import api.handler.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * MgrApiServer - myJava Mgr 패턴 기반 REST API 서버 메인 클래스
 */
public class MgrApiServer {

    public static final int PORT = 8080;
    private static HttpServer server;

    public static synchronized void startServer() throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // myJava Mgr 핸들러 등록
        server.createContext("/api/users", new UserMgrHandler());
        server.createContext("/api/expenses", new ExpenseMgrHandler());
        server.createContext("/api/categories", new CategoryMgrHandler());
        server.createContext("/api/budgets", new BudgetMgrHandler());
        server.createContext("/api/points", new PointMgrHandler());
        server.createContext("/api/posts", new PostMgrHandler());
        server.createContext("/api/friends", new FriendMgrHandler());
        server.createContext("/api/challenges", new ChallengeMgrHandler());
        server.createContext("/api/store", new StoreMgrHandler());
        server.createContext("/api/settings", new SettingMgrHandler());
        // ProfileMgrHandler 
        server.createContext("/api/profile", new ProfileMgrHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static synchronized boolean isRunning() {
        return server != null;
    }

    public static void main(String[] args) {
        try {
            startServer();

            System.out.println("==========================================================");
            System.out.println(" [myJava Mgr REST API Server Started Successfully]");
            System.out.println(" Base URL: http://localhost:" + PORT);
            System.out.println(" DB Connection Pool: DBConnectionMgr Singleton Active");
            System.out.println("==========================================================");
        } catch (IOException e) {
            System.err.println("서버 구동 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

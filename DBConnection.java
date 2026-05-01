import java.sql.*;

public class DBConnection{
	private static final String URL  = "jdbc:mysql://localhost:3306/exam_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root@123";
    private static Connection conn = null;

    public static Connection get(){
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (Exception e) { System.out.println("DB Error: " + e.getMessage()); }
        return conn;
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}

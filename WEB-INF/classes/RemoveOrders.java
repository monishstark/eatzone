import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

@WebServlet("/RemoveOrders")
public class RemoveOrders extends HttpServlet {

    String url = "jdbc:postgresql://localhost:5432/eatzone";
    String user = "postgres";
    String password = "1234";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        System.out.println(userId);

        if (userId != null) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);

                String deleteSql = "DELETE FROM \"order\".orders WHERE uid = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setInt(1, Integer.parseInt(userId));
                    deleteStatement.executeUpdate();
                }

                response.getWriter().println("Orders removed successfully for userID: " + userId);
            } catch (Exception e) {
                e.printStackTrace();
                response.getWriter().println("Error removing orders for userID: " + userId);
            }
        } else {
            response.sendRedirect("index.html");
        }
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/OtpVerify")
public class OtpVerify extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";

        try {
            Class.forName("org.postgresql.Driver");

            StringBuilder requestData = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestData.append(line);
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});

            String email = jsonMap.get("email");
            String otp = jsonMap.get("otp");

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                System.out.println(email+" "+"email");
                String sql = "SELECT * FROM auth.users WHERE email = ?";
                try (PreparedStatement selectStatement = connection.prepareStatement(sql)) {
                    selectStatement.setString(1, email);
                    ResultSet resultSet = selectStatement.executeQuery();

                    if (resultSet.next()) {
                        String dbOtp = resultSet.getString("otp");
                        String db_id=resultSet.getString("id");
                        System.out.println(dbOtp+" "+db_id+" "+otp);

                        if (otp.equals(dbOtp)) {
                            HttpSession session = request.getSession();
                            session.setAttribute("userId", db_id);


                            String updateOtpToNullSql = "UPDATE auth.users SET otp = NULL WHERE email = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateOtpToNullSql)) {
                                updateStatement.setString(1, email);
                                updateStatement.executeUpdate();
                            }


                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().print("successful");
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().print("Authentication failed");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().print("User not found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to change password");
        }
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

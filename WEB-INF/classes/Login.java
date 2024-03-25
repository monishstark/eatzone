import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


@WebServlet("/Login")
public class   Login extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");


        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String dbpassword = "1234";


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


            Connection connection = DriverManager.getConnection(url, user, dbpassword);
            String sql = "SELECT * FROM auth.users WHERE email = '" + email + "'";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            if  (resultSet.next()) {
                sql="update auth.users set otp=? where email=?";
                Random random = new Random();
                int onetime = random.nextInt(9000) + 1000;
                try (PreparedStatement updateStatement = connection.prepareStatement(sql)) {
                    updateStatement.setString(1, Integer.toString(onetime));
                    updateStatement.setString(2, email);
                    updateStatement.executeUpdate();
                }
                String from = "gsatoru0373@gmail.com";

                String to = email;
                System.out.println(to);

                String username = "gsatoru0373@gmail.com";
                String password = "xkdh vdzp oqfw xrnd";

                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");

                Session session = Session.getInstance(properties, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                response.setContentType("text/html");
                PrintWriter out = response.getWriter();

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                    message.setSubject("OTP");
                    message.setText("Hello, this is a automated mail sent otp "+String.valueOf(onetime));
                    Transport.send(message);

                    out.println("<html><body>");
                    out.println("<h2>Email sent successfully</h2>");
                    out.println("</body></html>");
                } catch (MessagingException e) {
                    throw new ServletException("Could not send email.", e);
                }


            }
            else{
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("Authentication failed");
            }







            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

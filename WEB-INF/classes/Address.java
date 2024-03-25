
import java.io.*;
import java.util.*;
import java.sql.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebServlet("/Address")
public class Address extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("cart working");
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";



        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            response.getWriter().println("User ID from session: " + userId);

            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);
                StringBuilder requestData = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestData.append(line);
                    }
                }

                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});


                String address = jsonMap.get("address");




                String checkSql = "update auth.users set address=? where id=?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setString(1, address);
                    checkStatement.setInt(2, Integer.parseInt(userId));

                    checkStatement.executeUpdate();


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("index.html");
        }
    }




    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        System.out.println("ProductDetails working");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");


        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";


        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");


        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            String sql = "SELECT * from auth.users  where id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,Integer.parseInt(userId));


            ResultSet resultSet = statement.executeQuery();


            List<HashMap<String, String>> dataList = new ArrayList<>();
            HashMap<String, String> dataMap = new HashMap<>();
            if (resultSet.next()){
                dataMap.put("address",resultSet.getString("address"));
            }


            dataList.add(dataMap);


            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(dataList);


            try (PrintWriter writer = response.getWriter()) {
                writer.write(jsonData);
            }

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

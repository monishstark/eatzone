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

@WebServlet("/CartCount")
public class CartCount extends HttpServlet {
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
            String sql = "SELECT * FROM cart.cart WHERE uid=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,Integer.parseInt(userId));


            ResultSet resultSet = statement.executeQuery();

            int ans=0;

            while (resultSet.next()) {

                ans+=resultSet.getInt("quantity");



            }


            List<HashMap<String, String>> dataList = new ArrayList<>();
            HashMap<String, String> dataMap = new HashMap<>();
            dataMap.put("cartsize", String.valueOf(ans));
            dataList.add(dataMap);
            System.out.println(dataList);

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

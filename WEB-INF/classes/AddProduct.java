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

@WebServlet("/AddProduct")
public class AddProduct extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        System.out.println("AddProduct working");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";

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

            HttpSession session = request.getSession();
            String rId = (String) session.getAttribute("rId");

            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> productData = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});
            int id = Integer.parseInt(productData.get("id"));
            String name = productData.get("name");
            String description = productData.get("description");
            int price = Integer.parseInt(productData.get("price"));
            int discount = Integer.parseInt(productData.get("discount"));
            String type = productData.get("type");
            String img = productData.get("img");


            String sql = "INSERT INTO restaurants.dishes (id,rid, name, description, price, discount, type, img) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,id);
            statement.setInt(2, Integer.parseInt(rId));
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setInt(5, price);
            statement.setInt(6, discount);
            statement.setString(7, type);
            statement.setString(8, img);

            int rowsAffected = statement.executeUpdate();

            String result = (rowsAffected > 0) ? "Product added successfully" : "Failed to add product";

            try (PrintWriter writer = response.getWriter()) {
                writer.write(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        String id = (String) session.getAttribute("rId");
        id="1";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            String sql = "SELECT * FROM restaurants.dishes WHERE rid=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,Integer.parseInt(id));


            ResultSet resultSet = statement.executeQuery();

            List<HashMap<String, String>> dataList = new ArrayList<>();

            while (resultSet.next()) {
                HashMap<String, String> dataMap = new HashMap<>();
                dataMap.put("id", resultSet.getString("id"));
                dataMap.put("name", resultSet.getString("name"));
                dataMap.put("description", resultSet.getString("description"));
                dataMap.put("price", resultSet.getString("price"));
                dataMap.put("discount", resultSet.getString("discount"));
                dataMap.put("type", resultSet.getString("type"));
                dataMap.put("img", resultSet.getString("img"));

                dataList.add(dataMap);
            }


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

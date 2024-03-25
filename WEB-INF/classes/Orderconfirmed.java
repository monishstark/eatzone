
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

@WebServlet("/Orderconfirmed")
public class Orderconfirmed extends HttpServlet {

    String url = "jdbc:postgresql://localhost:5432/eatzone";
    String user = "postgres";
    String password = "1234";
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("cart working");
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");




        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            response.getWriter().println("User ID from session: " + userId);

            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);


                String checkSql = "SELECT * FROM cart.cart WHERE uid= ? ";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();
                    while (resultSet.next()){
                        String sql="INSERT INTO \"order\".orders (uid ,did, quantity) VALUES (?, ?, ?)";
                                try(PreparedStatement upStatement=connection.prepareStatement(sql)){
                                    upStatement.setInt(1,resultSet.getInt("uid"));
                                    upStatement.setInt(2,resultSet.getInt("did"));
                                    upStatement.setInt(3,resultSet.getInt("quantity"));
                                    upStatement.executeUpdate();

                                }}


                }

                String dsql="DELETE FROM cart.cart where uid= ? ";
                try (PreparedStatement deleteStatement = connection.prepareStatement(dsql)) {
                    deleteStatement.setInt(1, Integer.parseInt(userId));
                    deleteStatement.executeUpdate();


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
            String sql = "SELECT * FROM \"order\".orders WHERE uid=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,Integer.parseInt(userId));


            ResultSet resultSet = statement.executeQuery();

            int ans=0;
            List<HashMap<String, String>> dataList = new ArrayList<>();
            while (resultSet.next()) {

                ans+=resultSet.getInt("quantity");
                sql = "SELECT * FROM restaurants.dishes WHERE id=?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1,resultSet.getInt("did"));
                ResultSet rs=statement.executeQuery();
                while (rs.next()) {
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("id",resultSet.getString("did"));
                    dataMap.put("name",rs.getString("name"));
                    dataMap.put("price",rs.getString("price"));
                    dataMap.put("discount",rs.getString("discount"));
                    dataMap.put("quantity",resultSet.getString("quantity"));
                    dataList.add(dataMap);}

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


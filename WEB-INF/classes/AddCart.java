
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

@WebServlet("/AddCart")
public class AddCart extends HttpServlet {

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
                StringBuilder requestData = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestData.append(line);
                    }
                }

                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});


                String pid = jsonMap.get("id");
                String quantity = jsonMap.get("quantity");

                if (Integer.parseInt(quantity)==0){
                    String dsql="DELETE FROM cart.cart where uid= ?  and did=?";
                    try (PreparedStatement deleteStatement = connection.prepareStatement(dsql)) {
                        deleteStatement.setInt(1, Integer.parseInt(userId));
                        deleteStatement.setInt(2, Integer.parseInt(pid));
                        deleteStatement.executeUpdate();
                        return;
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                String checkSql = "SELECT * FROM cart.cart WHERE uid= ?  and did=?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    checkStatement.setInt(2, Integer.parseInt(pid));
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {

                        updateCart(connection, userId, resultSet,pid,quantity);
                    } else {

                        insertCart(connection, userId,pid,quantity);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("index.html");
        }
    }

    private void updateCart(Connection connection, String userId, ResultSet resultSet, String pid, String quantityStr) throws SQLException {




        int quantity = Integer.parseInt(quantityStr);



        String updateSql = "UPDATE cart.cart SET quantity = ? WHERE uid= ?  and did=?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            updateStatement.setInt(1, quantity);
            updateStatement.setInt(2, Integer.parseInt(userId));
            updateStatement.setInt(3, Integer.parseInt(pid));


            updateStatement.executeUpdate();
        }
    }


    private void insertCart(Connection connection, String userId,String pid,String  quantityStr) throws SQLException {


        int q = Integer.parseInt(quantityStr);



        String insertSql = "INSERT INTO cart.cart (uid ,did, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

            insertStatement.setInt(1, Integer.parseInt(userId));
            insertStatement.setInt(2, Integer.parseInt(pid));
            insertStatement.setInt(3, Integer.parseInt(quantityStr));

            insertStatement.executeUpdate();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");



        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        

        if (userId != null) {

            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);

                String checkSql = "SELECT * FROM cart.cart WHERE uid = ?";
                Set<Integer> rids=new HashSet<>();
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();

                    while(resultSet.next()) {
                        rids.add(resultSet.getInt("did"));

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                List<HashMap<String, String>> dataList = new ArrayList<>();

                for (int i:rids){
                    String sql = "SELECT * FROM restaurants.dishes WHERE id=?";

                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setInt(1,i);
                    ResultSet resultSet=statement.executeQuery();
                    HashMap<String, String> dataMap = new HashMap<>();
                    while (resultSet.next()) {
                        dataMap.put("id", resultSet.getString("id"));
                        dataMap.put("name", resultSet.getString("name"));
                        dataMap.put("description", resultSet.getString("description"));
                        dataMap.put("price", resultSet.getString("price"));
                        dataMap.put("discount", resultSet.getString("discount"));
                        dataMap.put("type", resultSet.getString("type"));
                        dataMap.put("img", resultSet.getString("img"));
                    }

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
            response.setStatus(HttpServletResponse.SC_OK);


        }
        else{
            response.sendRedirect("index.html");

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


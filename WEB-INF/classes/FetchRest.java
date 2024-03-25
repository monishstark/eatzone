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

@WebServlet("/FetchRest")
public class FetchRest extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        System.out.println("FetchRest");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");


        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";
        String type=request.getParameter("search");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            String sql = "SELECT * FROM restaurants.dishes where type=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1,type);


            ResultSet resultSet=statement.executeQuery();


            List<HashMap<String, String>> dataList = new ArrayList<>();
            while (resultSet.next()) {

                sql = "SELECT * FROM restaurants.restaurants_table where id=?";
                statement = connection.prepareStatement(sql);
                statement.setInt(1,resultSet.getInt("rid"));
                ResultSet rs=statement.executeQuery();
                HashMap<String, String> dataMap = new HashMap<>();
                dataMap.put("pid", resultSet.getString("id"));
                dataMap.put("pname", resultSet.getString("name"));
                dataMap.put("description", resultSet.getString("description"));
                dataMap.put("price", resultSet.getString("price"));
                dataMap.put("discount", resultSet.getString("discount"));
                dataMap.put("type", resultSet.getString("type"));
                dataMap.put("img", resultSet.getString("img"));
                while (rs.next()) {
                    dataMap.put("rid", rs.getString("id"));
                    dataMap.put("rname", rs.getString("name"));
                    dataMap.put("address", rs.getString("address"));
                    dataMap.put("rating", rs.getString("rating"));
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
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

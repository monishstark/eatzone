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

@WebServlet("/Rating")
public class Rating extends HttpServlet {

    String url = "jdbc:postgresql://localhost:5432/eatzone";
    String user = "postgres";
    String password = "1234";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("cart working");
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

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


            int pid = Integer.parseInt(jsonMap.get("id"));
            int rating = Integer.parseInt(jsonMap.get("rating"));


            String selectQuery = "SELECT *   FROM restaurants.dishes WHERE id = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, pid);
                ResultSet resultSet = selectStatement.executeQuery();
                int rid=1;

                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    int total = resultSet.getInt("total");
                    rid=resultSet.getInt("rid");


                    count++;
                    total += rating;


                    int newRating = total / count;


                    String updateQuery = "UPDATE restaurants.dishes SET count = ?, total = ?, rating = ? WHERE id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setInt(1, count);
                        updateStatement.setInt(2, total);
                        updateStatement.setInt(3, newRating);
                        updateStatement.setInt(4, pid);
                        updateStatement.executeUpdate();
                    }

                    response.getWriter().write("Rating added successfully!");
                } else {

                    response.getWriter().write("Dish not found with id: " + pid);
                }
                updateRating(rid, connection);
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    private void updateRating(int rid, Connection connection) throws SQLException {
        String selectRatingsQuery = "SELECT rating FROM restaurants.dishes WHERE rid = ?";
        try (PreparedStatement selectRatingsStatement = connection.prepareStatement(selectRatingsQuery)) {
            selectRatingsStatement.setInt(1, rid);
            ResultSet ratingsResultSet = selectRatingsStatement.executeQuery();

            int totalRating = 0;
            int totalCount = 0;

            while (ratingsResultSet.next()) {
                totalRating += ratingsResultSet.getInt("rating");
                totalCount++;
            }

            if (totalCount > 0) {
                int averageRating = totalRating / totalCount;

                String updateRestaurantRatingQuery = "UPDATE restaurants.restaurants_table SET rating = ? WHERE id = ?";
                try (PreparedStatement updateRestaurantRatingStatement = connection.prepareStatement(updateRestaurantRatingQuery)) {
                    updateRestaurantRatingStatement.setInt(1, averageRating);
                    updateRestaurantRatingStatement.setInt(2, rid);
                    updateRestaurantRatingStatement.executeUpdate();
                }
            }
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

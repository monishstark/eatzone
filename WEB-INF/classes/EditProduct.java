import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/EditProduct")
public class EditProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String url = "jdbc:postgresql://localhost:5432/eatzone";
        String user = "postgres";
        String password = "1234";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            HttpSession session = request.getSession();
            String rId = (String) session.getAttribute("rId");

            StringBuilder requestData = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestData.append(line);
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> productData = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {
            });
            int id = Integer.parseInt(productData.get("id"));

            int price = Integer.parseInt(productData.get("price"));
            int discount = Integer.parseInt(productData.get("discount"));


            // Perform the logic to update the product in the database
            String updateQuery = "UPDATE restaurants.dishes SET  price=?, discount=? WHERE id=? and rid=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

                preparedStatement.setInt(1, price);
                preparedStatement.setInt(2, discount);
                preparedStatement.setInt(3, id);
                preparedStatement.setInt(4, Integer.parseInt(rId));


                int rowsAffected = preparedStatement.executeUpdate();


                PrintWriter out = response.getWriter();
                if (rowsAffected > 0) {
                    out.write("Product edited successfully");
                } else {
                    out.write("Failed to edit product");
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
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



import com.razorpay.*;
import org.json.*;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/payment")
public class Payment extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        System.out.println("ProductDetails working");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        System.out.println("razerpay working");
        StringBuilder requestData = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestData.append(line);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});


        String total = jsonMap.get("total");

        int tprice= Integer.parseInt(total);
        System.out.println(tprice);
        tprice*=100;



        try {
            RazorpayClient razorpay = new RazorpayClient("rzp_test_F1rZEmlLIN2TiL", "YCzyrlQOGZlmacXpIzXdJJof");

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",tprice);
            orderRequest.put("currency","INR");
            orderRequest.put("receipt", "receipt#1");
            JSONObject notes = new JSONObject();
            notes.put("notes_key_1","Tea, Earl Grey, Hot");
            orderRequest.put("notes",notes);

            Order order = razorpay.orders.create(orderRequest);
            String orderid=order.get("id");
            System.out.println(orderid);
            List<HashMap<String, String>> dataList = new ArrayList<>();
            HashMap<String, String> dataMap = new HashMap<>();
            dataMap.put("orderid",orderid);
            dataList.add(dataMap);
            ObjectMapper om = new ObjectMapper();
            String jd = om.writeValueAsString(dataList);


            try (PrintWriter writer = response.getWriter()) {
                writer.write(jd);
            }

        } catch (RazorpayException e) {
            // Handle exceptions
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing payment");
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

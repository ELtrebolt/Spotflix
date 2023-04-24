import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/espotify");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String spotifyAuthUrl = SpotifyClient.getAuthorizationUrl();
        request.getSession().setAttribute("user", "test");
        request.getSession().setAttribute("setup", "custom");

        // response.sendRedirect(spotifyAuthUrl);
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "success");
        responseJsonObject.addProperty("spotifyAuthUrl", spotifyAuthUrl);
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        //        try (Connection conn = dataSource.getConnection()) {
//            Statement statement = conn.createStatement();
//            String query;
//
//            ResultSet rs = statement.executeQuery(query);
//            JsonArray jsonArray = new JsonArray();
//            while (rs.next()) {
//                String song_id =
//            }
//
//
//
//        } catch (SQLException e) {
//
//        }

        JsonObject responseJsonObject = new JsonObject();
        // Path1 = Custom Spotify Data
        if(username == null && password == null)
        {
            String spotifyAuthUrl = SpotifyClient.getAuthorizationUrl();
            request.getSession().setAttribute("user", "test");
            request.getSession().setAttribute("setup", "custom");
            // response.sendRedirect(spotifyAuthUrl);
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("spotifyAuthUrl", spotifyAuthUrl);
        }
        // Path2 = Sample Spotify Data
        else if (username.equals("anteater") && password.equals("123456")) {

            request.getSession().setAttribute("user", new User(username));
            request.getSession().setAttribute("setup", "sample");

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");

        } else {
            responseJsonObject.addProperty("status", "failed");

            if (!username.equals("anteater")) {
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else {
                responseJsonObject.addProperty("message", "incorrect password");
            }
        }
        response.getWriter().write(responseJsonObject.toString());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
    }
}
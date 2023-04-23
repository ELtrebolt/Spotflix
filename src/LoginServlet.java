import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String spotifyAuthUrl = SpotifyClient.getAuthorizationUrl();
        request.getSession().setAttribute("User", "test");
        request.getSession().setAttribute("Setup", 1);
        response.sendRedirect(spotifyAuthUrl);
    }
}
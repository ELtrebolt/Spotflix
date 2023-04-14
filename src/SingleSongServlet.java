import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleSongServlet, which maps to url "/api/single-song"
@WebServlet(name = "SingleSongServlet", urlPatterns = "/api/single-song")
public class SingleSongServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/espotify");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT s.*, a.Id AS ArtistId, a.Name AS Name, ts.ShortRank AS ShortRank,\n" +
                    "   GROUP_CONCAT(DISTINCT goa.Genre SEPARATOR ', ') AS Genres\n" +
                    "FROM songs AS s\n" +
                    "JOIN artists_in_songs AS ais ON ais.SongId = s.Id\n" +
                    "JOIN artists AS a ON a.Id = ais.ArtistId\n" +
                    "LEFT JOIN genres_of_artists AS goa ON goa.ArtistId = a.Id\n" +
                    "LEFT JOIN top_songs AS ts ON  ts.Id = s.Id\n" +
                    "WHERE s.Id = ?\n" +
                    "GROUP BY a.Id\n";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String songId = rs.getString("Id");
                String songTitle = rs.getString("Title");
                String songAlbum = rs.getString("Album");
                String songDateLiked = rs.getString("DateLiked");
                String shortRank = rs.getString("ShortRank");

                String artistId = rs.getString("ArtistId");
                String artistName = rs.getString("Name");
                String genres = rs.getString("Genres");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("song_id", songId);
                jsonObject.addProperty("song_title", songTitle);
                jsonObject.addProperty("song_album", songAlbum);
                jsonObject.addProperty("song_dateLiked", songDateLiked);
                jsonObject.addProperty("artist_id", artistId);
                jsonObject.addProperty("artist_name", artistName);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("short_rank", shortRank);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}

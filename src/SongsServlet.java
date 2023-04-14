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
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called SongsServlet, which maps to url "/api/songs"
@WebServlet(name = "SongsServlet", urlPatterns = "/api/songs")
public class SongsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/espotify");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT s.*,\n" +
                    "       GROUP_CONCAT(subquery.ArtistId ORDER BY subquery.ArtistId SEPARATOR ', ') as ArtistIds,\n" +
                    "       GROUP_CONCAT(subquery.ArtistName ORDER BY subquery.ArtistId SEPARATOR ', ') as ArtistNames,\n" +
                    "       (SELECT GROUP_CONCAT(DISTINCT Genre ORDER BY cnt DESC SEPARATOR ', ')\n" +
                    "    FROM (\n" +
                    "      SELECT goa.Genre, COUNT(*) AS cnt\n" +
                    "      FROM genres_of_artists AS goa\n" +
                    "      JOIN artists AS a ON goa.ArtistId = a.Id\n" +
                    "      JOIN artists_in_songs AS ais ON ais.ArtistId = a.Id\n" +
                    "      WHERE ais.SongId = subquery.Id\n" +
                    "      GROUP BY goa.Genre\n" +
                    "      ORDER BY cnt DESC\n" +
                    "      LIMIT 3\n" +
                    "      ) as ArtistGenres\n" +
                    "    ) AS TopGenres,\n" +
                    "       ts.ShortRank AS ShortRank\n" +
                    "FROM (\n" +
                    "    SELECT s.*, a.Id as ArtistId, a.Name as ArtistName,\n" +
                    "           ROW_NUMBER() OVER (PARTITION BY s.Id ORDER BY a.Id) AS row_num\n" +
                    "    FROM songs AS s\n" +
                    "    JOIN artists_in_songs AS ais ON ais.SongId = s.Id\n" +
                    "    JOIN artists AS a ON a.id = ais.ArtistId \n" +
                    "    ) as subquery\n" +
                    "JOIN songs AS s ON s.Id = subquery.Id\n" +
                    "JOIN top_songs AS ts ON ts.Id = subquery.Id\n" +
                    "WHERE row_num <= 3\n" +
                    "GROUP BY s.Id\n" +
                    "ORDER BY ShortRank IS NULL, ShortRank ASC;\n";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String song_id = rs.getString("Id");
                String song_title = rs.getString("Title");
                String song_album = rs.getString("Album");
                String song_dateLiked = rs.getString("DateLiked");
                String short_rank = rs.getString("ShortRank");

                String artist_names = rs.getString("ArtistNames");
                String top_genres = rs.getString("TopGenres");
                String artist_ids = rs.getString("ArtistIds");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("song_id", song_id);
                jsonObject.addProperty("song_title", song_title);
                jsonObject.addProperty("song_album", song_album);
                jsonObject.addProperty("song_dateLiked", song_dateLiked);
                jsonObject.addProperty("artist_names", artist_names);
                jsonObject.addProperty("artist_ids", artist_ids);
                jsonObject.addProperty("top_genres", top_genres);
                jsonObject.addProperty("short_rank", short_rank);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}

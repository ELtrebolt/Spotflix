import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "SetupServlet", urlPatterns = "/api/setup")
public class SetupServlet extends HttpServlet {

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

    private String runSqlFile(Connection conn, String path, String t) throws Exception {
        String sqlCodeRan = "";
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
            sqlCodeRan += sb.toString();
            String[] queries = sqlCodeRan.split(";");
            Statement statement = conn.createStatement();
            for(String query : queries) {
                if(!query.isEmpty())
                {
                    query = query.replace("FIELDS", " FIELDS");
                    query = query.replace("SET", "SET ");
                    sqlCodeRan += query + "\n\t   ";
                    if(t.equals("executeUpdate"))
                    {
                        statement.executeUpdate(query);
                    }
                    else if(t.equals("execute"))
                    {
                        statement.execute(query);
                    }
                }
            }
            statement.close();
            return sqlCodeRan;
        }
        catch(Exception e)
        {
            throw new Exception(e.getMessage() + "    " + sqlCodeRan);
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        // response.setContentType("text/plain");
        response.setContentType("text/event-stream");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Cache-Control", "no-cache");

        if (request.getSession().getAttribute("setup") == null) {
            response.sendRedirect("../login.html");
        } else if (request.getSession().getAttribute("setup").equals("sample")){
            try (Connection conn = dataSource.getConnection()) {
                // Create & Clear SQL Tables
                String deleteSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/create_table.sql";
                String res1 = runSqlFile(conn, deleteSql, "executeUpdate");

                // Load SQL Tables
                String loadSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/win_load_data_sample.sql";
                String res2 = runSqlFile(conn, loadSql, "execute");
                // Update Table Counts
                String updateSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/update_table_counts.sql";
                String res3 = runSqlFile(conn, updateSql, "execute");

                JsonObject responseJsonObject = new JsonObject();

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("CreateTables", res1);
                responseJsonObject.addProperty("LoadTables", res2);
                responseJsonObject.addProperty("UpdateTables", res3);
                out.write(responseJsonObject.toString());

                System.out.println("Sample Complete - redirect to Song List");
                response.sendRedirect("../song-list.html");
            }
            catch(Exception e) {
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "failed");
                responseJsonObject.addProperty("error", e.getMessage());
                out.write(responseJsonObject.toString());
            }

        } else if (request.getSession().getAttribute("setup").equals("custom")) {
            String code = request.getParameter("code");
            if(code == null)
            {
                System.out.println("Code is null");
                response.sendRedirect("../login.html");
            }
            else
            {
                try (Connection conn = dataSource.getConnection()) {
                    // Create & Clear SQL Tables
                    String deleteSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/create_table.sql";
                    String res1 = runSqlFile(conn, deleteSql, "executeUpdate");

                    SpotifyClient.setCode(code);
                    SpotifyClient.setAccessToken();
                    SpotifyClient.setPrinter(out);

                    SpotifyClient.getUsersSavedTracks_Sync();
                    SpotifyClient.getUsersTopSongs();

                    // Load SQL Tables
                    String loadSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/win_load_data_custom.sql";
                    String res2 = runSqlFile(conn, loadSql, "execute");
                    // Update Table Counts
                    String updateSql = "C:/Users/Ethan/Documents/cs122b/project2/sql-queries/update_table_counts.sql";
                    String res3 = runSqlFile(conn, updateSql, "execute");

                    JsonObject responseJsonObject = new JsonObject();

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("CreateTables", res1);
                    responseJsonObject.addProperty("LoadTables", res2);
                    responseJsonObject.addProperty("UpdateTables", res3);
                    out.write(responseJsonObject.toString());

                    System.out.println("Custom Complete - redirect to Song List");
                    response.sendRedirect("../song-list.html");
                }
                catch(Exception e){
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "failed");
                    responseJsonObject.addProperty("error", e.getMessage());
                    out.write(responseJsonObject.toString());
                }
                finally {
                    out.close();
                }
            }
        }



    }
}
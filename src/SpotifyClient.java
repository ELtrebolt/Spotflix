import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import org.apache.hc.core5.http.ParseException;
import java.io.IOException;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import com.neovisionaries.i18n.CountryCode;
import tech.tablesaw.io.csv.CsvWriteOptions;

import java.net.URI;

public class SpotifyClient {
    private static final String CLIENT_ID = "MY_ID";
    private static final String CLIENT_SECRET = "MY_SECRET";
    private static final String REDIRECT_URI = "http://localhost:8080/s23_122b_gbros_project1_war/setup.html";
    private static final String SCOPES = "user-library-read user-top-read";
    private static String code = "";
    private static PrintWriter out;

    public static SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .setRedirectUri(SpotifyHttpManager.makeUri(REDIRECT_URI))
            .build();

    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            //      .state("x4xkmn9pu3j6ukrs8n")
            .scope("user-library-read user-top-read")
            .build();

    // Auth Part 1 = Redirect to Spotify Auth Page
    public static String getAuthorizationUrl() {
        final URI uri = authorizationCodeUriRequest.execute();
        return uri.toString();
    }

    public static void setCode(String codeFromURL) {
        code = codeFromURL;
    }

    public static void setPrinter(PrintWriter writer) {
        out = writer;
    }

    // Auth Part 2 = Get Access & Refresh Token from Auth Code
    public static void setAccessToken() {
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        try {
            final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            out.write("event: message\n");
            out.write("data:" + e.getMessage() + "\n\n");
            out.flush();
        }
    }

    public static int getUsersTopTracks_Sync() {
        GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                //          .limit(10)
                //          .offset(0)
                //          .time_range("medium_term")
                .build();
        try {
            final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            return trackPaging.getTotal();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            out.write("event: message\n");
            out.write("data:" + e.getMessage() + "\n\n");
            out.flush();
        }
        return -1;
    }

    private static Table addTracksTable(Table tracksTable, SavedTrack[] items)
    {
        StringColumn idCol = StringColumn.create("Id");
        StringColumn nameCol = StringColumn.create("Title");
        StringColumn albumCol = StringColumn.create("Album");
        DateColumn dateCol = DateColumn.create("DateLiked");
        // IntColumn popCol = IntColumn.create("Popularity");

        StringColumn column = tracksTable.stringColumn("Id");
        for(int i = 0; i<items.length; i++)
        {
            Track track = items[i].getTrack();
            if(column.where(column.isEqualTo(track.getId())).isEmpty() )
            {
                if (idCol.where(idCol.isEqualTo(track.getId())).isEmpty() )
                {
                    idCol.append(track.getId());
                    nameCol.append(track.getName());
                    albumCol.append(track.getAlbum().getName());
                    Date date = items[i].getAddedAt();
                    LocalDate localDate = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
                    dateCol.append(localDate);
                    // popCol.append(track.getPopularity());
                }
            }
        }
        Table table = Table.create("SavedTracks", idCol, nameCol, albumCol, dateCol);
        // table.write().csv("SampleSavedTracks.csv");
        return tracksTable.append(table);
    }

    private static Table addArtistsTable(Table artistsTable, SavedTrack[] items)
    {
        StringColumn column = artistsTable.stringColumn("Id");
        StringColumn idCol = StringColumn.create("Id");
        StringColumn nameCol = StringColumn.create("Name");
        for(int i = 0; i<items.length; i++)
        {
            ArtistSimplified[] artists = items[i].getTrack().getArtists();
            for(int j = 0; j<artists.length; j++)
            {
                if(column.where(column.isEqualTo(artists[j].getId())).isEmpty() )
                {
                    if (idCol.where(idCol.isEqualTo(artists[j].getId())).isEmpty() )
                    {
                        idCol.append(artists[j].getId());
                        nameCol.append(artists[j].getName());
                    }
                }
            }
        }
        Table table = Table.create("Artists", idCol, nameCol);
        return artistsTable.append(table);
    }

    private static Table addArtistsInSongsTable(Table artistsInSongsTable, SavedTrack[] items)
    {
        StringColumn artistCol = StringColumn.create("ArtistId");
        StringColumn songCol = StringColumn.create("TrackId");
        for(int i = 0; i<items.length; i++)
        {
            Track track = items[i].getTrack();

            ArtistSimplified[] artists = track.getArtists();
            for(int j = 0; j<artists.length; j++)
            {
                artistCol.append(artists[j].getId());
                songCol.append(track.getId());
            }
        }
        Table table = Table.create("ArtistsInSongs", artistCol, songCol);
        return artistsInSongsTable.append(table);
    }

    private static Table addGenresOfArtistsTable(Table genresOfArtistsTable, SavedTrack[] items)
    {
        StringColumn genreCol = StringColumn.create("Genre");
        StringColumn artistCol = StringColumn.create("ArtistId");
        StringColumn uniqueArtists = StringColumn.create("uniqueArtists");

        // Get set of unique artists for these tracks
        for(int i = 0; i<items.length; i++)
        {
            ArtistSimplified[] artists = items[i].getTrack().getArtists();
            for(int j = 0; j<artists.length; j++)
            {
                if(uniqueArtists.where(uniqueArtists.isEqualTo(artists[j].getId())).isEmpty() )
                {
                    uniqueArtists.append(artists[j].getId());
                }
            }
        }

        // Add genres for each artist
        int index = 0;
        int size = uniqueArtists.size();
        while(index < size)
        {
            int end = index+50;
            if(end > size)
            {
                end = size;
            }
            String[] subset = uniqueArtists.inRange(index, end).asObjectArray();
            GetSeveralArtistsRequest getSeveralArtistsRequest = spotifyApi.getSeveralArtists(subset)
                    .build();

            try {
                final Artist[] artists = getSeveralArtistsRequest.execute();
                for(int i = 0; i<artists.length; i++)
                {
                    String[] genres = artists[i].getGenres();
                    // if(genres.length > 0)
                    // {
                    for(int j = 0; j<genres.length; j++)
                    {
                        genreCol.append(genres[j]);
                        artistCol.append(artists[i].getId());
                    }
                    // }
                    // else
                    // {
                    //   genreCol.append("N/A");
                    //   artistCol.append(artists[i].getId());
                    // }
                }
            }
            catch (IOException | SpotifyWebApiException | ParseException e) {
                out.write("event: message\n");
                out.write("data:" + e.getMessage() + "\n\n");
                out.flush();
            }
            index += 50;
            String update = String.format("ArtistSubset Index %s\n", index);
            out.write("event: message\n");
            out.write("data:" + update + "\n\n");
            out.flush();
        }

        Table table = Table.create("ArtistsInSongs", genreCol, artistCol);
        return genresOfArtistsTable.append(table);
    }

    public static void getUsersTopSongs() {
        StringColumn trackIdCol = StringColumn.create("TrackId");
        StringColumn trackIdCol1 = StringColumn.create("TrackId");
        StringColumn trackIdCol2 = StringColumn.create("TrackId");
        StringColumn trackIdCol3 = StringColumn.create("TrackId");
        IntColumn shortTermRankCol = IntColumn.create("ShortRank");
        IntColumn medTermRankCol = IntColumn.create("MedRank");
        IntColumn longTermRankCol = IntColumn.create("LongRank");

        String[] terms = {"short_term", "medium_term", "long_term"};
        StringColumn[] idCols = {trackIdCol1, trackIdCol2, trackIdCol3};
        IntColumn[] rankCols = {shortTermRankCol, medTermRankCol, longTermRankCol};
        Table topSongsTable = Table.create("TopSongs", trackIdCol);

        for(int j = 0; j<3; j++)
        {
            GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                    .limit(50)
                    .offset(0)
                    .time_range(terms[j])
                    .build();

            try {
                final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();
                Track[] items = trackPaging.getItems();
                for(int i = 0; i<items.length; i++)
                {
                    idCols[j].append(items[i].getId());
                    rankCols[j].append(i+1);
                }
                Table table = Table.create(terms[j], idCols[j], rankCols[j]);
                topSongsTable = topSongsTable.joinOn("TrackId")
                        .fullOuter(table, "TrackId");
            }
            catch (IOException | SpotifyWebApiException | ParseException e) {
                out.write("event: message\n");
                out.write("data:" + e.getMessage() + "\n\n");
                out.flush();
            }
        }
        for(int j = 0; j<rankCols.length; j++)
        {
            IntColumn i = topSongsTable.intColumn(rankCols[j].name());
            i = i.set(i.isMissing(), 0);
        }
        // export without header
        CsvWriteOptions.Builder builder =
                CsvWriteOptions.builder("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/TopSongs.csv")
                        .header(false);
        CsvWriteOptions options = builder.build();
        topSongsTable.write().usingOptions(options);
        // topSongsTable.write().csv("TopSongs.csv");
    }

    // tracksTable, artistsTable, artistsInSongsTable, genresOfArtistsTable, genresTable
    public static void getUsersSavedTracks_Sync() {
        int index = 0;
        StringColumn trackIdCol = StringColumn.create("Id");
        StringColumn trackNameCol = StringColumn.create("Title");
        StringColumn albumCol = StringColumn.create("Album");
        DateColumn dateCol = DateColumn.create("DateLiked");
        // IntColumn popCol = IntColumn.create("Popularity");
        Table tracksTable = Table.create("AllSavedTracks", trackIdCol, trackNameCol, albumCol, dateCol);

        StringColumn artistIdCol = StringColumn.create("Id");
        StringColumn artistNameCol = StringColumn.create("Name");
        Table artistsTable = Table.create("Artists", artistIdCol, artistNameCol);

        StringColumn artistIdCol2 = StringColumn.create("ArtistId");
        StringColumn trackIdCol2 = StringColumn.create("TrackId");
        Table artistsInSongsTable = Table.create("ArtistsInSongs", artistIdCol2, trackIdCol2);

        StringColumn genreCol = StringColumn.create("Genre");
        StringColumn artistIdCol3 = StringColumn.create("ArtistId");
        Table genresOfArtistsTable = Table.create("ArtistsInSongs", genreCol, artistIdCol3);

        // IntColumn genreIdCol = IntColumn.create("Id");
        // StringColumn genreCol2 = StringColumn.create("Name");
        // Table genresTable = Table.create("Genres", genreIdCol, genreCol2);

        while(true)
        {
            try {
                GetUsersSavedTracksRequest getUsersSavedTracksRequest = spotifyApi.getUsersSavedTracks()
                        .limit(50)
                        .offset(index)
                        .market(CountryCode.US)
                        .build();

                final Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();
                SavedTrack[] items = savedTrackPaging.getItems();
                tracksTable = addTracksTable(tracksTable, items);
                artistsTable = addArtistsTable(artistsTable, items);
                artistsInSongsTable = addArtistsInSongsTable(artistsInSongsTable, items);
                genresOfArtistsTable = addGenresOfArtistsTable(genresOfArtistsTable, items);

                if(savedTrackPaging.getNext() == null)
                {
                    break;
                }
            }
            catch (IOException | SpotifyWebApiException | ParseException e) {
                out.write("event: message\n");
                out.write("data:" + e.getMessage() + "\n\n");
                out.flush();
            }

            index += 50;
            String s = String.format("SavedTracks Index %s\n", index);
            out.write("event: message\n");
            out.write("data:" + s + "\n\n");
            out.flush();
        }

        StringColumn genreCol2 = StringColumn.create("Name", genresOfArtistsTable.stringColumn("Genre").unique().asObjectArray());
        int[] range = IntStream.rangeClosed(1, genreCol2.size()).toArray();
        IntColumn genreIdCol = IntColumn.create("Id", range);
        Table genresTable = Table.create("Genres", genreCol2, genreIdCol);

        tracksTable.write().csv("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/AllSavedTracks.csv");
        artistsTable.write().csv("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/Artists.csv");
        artistsInSongsTable.write().csv("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/ArtistsInSongs.csv");
        genresOfArtistsTable.write().csv("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/GenresOfArtists.csv");
        genresTable.write().csv("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/Genres.csv");

        saveFileAsUTF8("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/AllSavedTracks.csv");
        saveFileAsUTF8("C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/Artists.csv");
    }

    // Re-save as UTF 8 to account for accented characters
    private static void saveFileAsUTF8(String path) {
        try {
            Path filePath = Paths.get(path);
            List<String> lines = Files.readAllLines(filePath, Charset.forName("Windows-1252"));
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {

        }
    }

}
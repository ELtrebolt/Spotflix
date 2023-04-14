/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating song info from resultData");

    // populate the song info h3
    // find the empty h3 body by id "song_info"
    let artistInfoElement = jQuery("#artist_info");

    // append two html <p> created to the h3 body, which will refresh the page
    artistInfoElement.append("<p>Artist: " + resultData[0]["artist_name"] + "</p>" +
        "<p>Genres: " + resultData[0]["genres"] + "</p>");

    console.log("handleResult: populating artist table from resultData");

    // Populate the song table
    // Find the empty table body by id "artist_table_body"
    let songTableBodyElement = jQuery("#song_table_body");
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]);
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["short_rank"] + "</th>";
        rowHTML += "<th>" +
                    // Add a link to single-song.html with id passed with GET url parameter
                    '<a href="single-song.html?id=' + resultData[i]['song_id'] + '">'
                    + resultData[i]["song_title"] +     // display song_title for the link text
                    '</a>' +
                    "</th>";
        rowHTML += "<th>" + resultData[i]["song_album"] + "</th>";
        rowHTML += "<th>" + resultData[i]["song_dateLiked"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        songTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let artistId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-artist?id=" + artistId, // Setting request url, which is mapped by SongsServlet in Songs.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleSongServlet
});
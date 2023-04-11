/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSongResult(resultData) {
    console.log("handleSongResult: populating song table from resultData");

    // Populate the song table
    // Find the empty table body by id "song_table_body"
    let songTableBodyElement = jQuery("#song_table_body");

    // Iterate through resultData, no more than N entries
    for (let i = 0; i < Math.min(150, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-song.html with id passed with GET url parameter
            '<a href="single-song.html?id=' + resultData[i]['song_id'] + '">'
            + resultData[i]["song_title"] +     // display song_title for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["song_album"] + "</th>";
        rowHTML += "<th>" + resultData[i]["song_dateLiked"] + "</th>";
        rowHTML += "<th>" + resultData[i]["artist_names"] + "</th>";
        rowHTML += "<th>" + resultData[i]["top_genres"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        songTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleSongResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/songs", // Setting request url, which is mapped by SongsServlet in Songs.java
    success: (resultData) => handleSongResult(resultData) // Setting callback function to handle data returned successfully by the SongsServlet
});
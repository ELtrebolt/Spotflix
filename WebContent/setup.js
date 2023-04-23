// trigger GET method on page load
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["setup"] === "success") {
        window.location.replace("index.html");
        $("#setup_error_message").text("DOES THIS WORK");
    } else {
        $("#setup_error_message").text(resultDataJson["error"]);
    }
}

$.ajax("api/setup", {
    method: "GET",
    success: handleSessionData
});
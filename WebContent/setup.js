let newUrl = window.location.href.replace("setup.html?", "api/setup?")
window.location.replace(newUrl);

let source = new EventSource("api/setup");
source.onmessage = function(event) {
    $("#logs").append(event.data);
};

function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    if (resultDataJson["status"] === "success") {
        window.location.replace("songs-list.html");
    } else {
        $("#setup_error_message").text(resultDataJson["error"]);
    }
}

// Make the initial AJAX call to start the setup process
$.ajax("api/setup", {
    method: "GET",
    success: handleSessionData,
});

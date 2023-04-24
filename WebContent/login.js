// Path1 = Custom Spotify Data = trigger GET method for LoginServlet.java
// which then redirects to SpotifyAuthURL then triggers GET method for SetupServlet.java
let spotify_login = $("#spotify_login");
function redirectToSpotify(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["status"] === "success") {
        window.location.replace(resultDataJson["spotifyAuthUrl"]);
    }
}

function triggerGetForLoginJava(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    // $.ajax("api/login", {
    //     method: "GET",
    //     success: redirectToSpotify
    // });
    $.ajax("api/login", {
        method: "POST",
        data: JSON.stringify({username: null, password: null}),
        success: redirectToSpotify
    });
    // window.location.replace("api/login")
}
spotify_login.submit(triggerGetForLoginJava);

// Path2 = Sample Spotify Data = trigger POST method for LoginServlet.java
// On return trigger GET method for SetupServlet.java
let login_form = $("#login_form");
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["status"] === "success") {
        window.location.replace("api/setup");
    } else {
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}
login_form.submit(submitLoginForm);
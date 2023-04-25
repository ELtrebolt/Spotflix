// let newUrl = window.location.href.replace("setup.html?", "api/setup?")
// window.location.replace(newUrl);

// Make the initial AJAX call to start the setup process
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');
$.ajax("api/setup", {
    method: "GET",
    data: {code: code},
    success: completeSetup
});

function completeSetup() {
    window.location.replace("song-list.html");
}

let source = new EventSource(`api/setup?code=${code}`);
source.onmessage = (event) => {
    $("#logs").html($("#logs").html() + event.data + "<br>");
    // $("#logs").append(event.data);
    // $("#logs").append(`\n`);
};
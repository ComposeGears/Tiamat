function onLoadFinished() {
    document.dispatchEvent(new Event("app-loaded"));
}

document.addEventListener("app-loaded", function() {
     document.getElementById("loading-indicator").style.display = "none";
});
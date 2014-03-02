$( document ).ready(function() {
    alert("For now the site will try to refresh the \"current running driver list\" every 30 seconds");
    getRunningDrivers();
    setInterval(getRunningDrivers, 30000);
});
/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    console.log("reuquesting");
    $.ajax({
        type: "GET",
        url: "http://localhost:" + $portnum + "/get_running_drivers",
        dataType: "xml",
        async: true,
        contentType: "text/xml; charset=\"utf-8\"",
        success: function(xml) {
            $(xml).find('Module').each(function() {
                var $module = $(this);
                var name = $module.attr('name');
                document.getElementById("driver_names").innerHTML += "<li>name</li>";
            })
        },
        error: function(xhr, status, error) {
            if (xhr.status != 404) {
                alert(error);
            } 
            else {
                alert("404 xml not found");
            }
        }
    });
}   
    
function command() {
    $("#send_command").slideToggle("slow");
}

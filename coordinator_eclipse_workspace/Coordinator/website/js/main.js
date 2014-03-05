$( document ).ready(function() {
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
            var $driverList = "";
            $(xml).find('Module').each(function() {
                var $module = $(this);
                var $name = $module.attr('name');
                console.log("found driver " + $name);
                $driverList += "<li>" + $name + "</li>";
            })
            document.getElementById("driver_names").innerHTML = $driverList;
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
    
function showCommandBox() {
    $("#send_command").slideToggle("slow");
}

/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    console.log("reuquesting");
    document.getElementById("driver_refresh_button").innerHTML = "";
    document.getElementById("driver_names").innerHTML = "<i class=\"fa fa-spinner fa-spin fa-2x\"></i>";
//    document.getElementById("driver_names").style("color: red");
    $.ajax({
        type: "GET",
        url: "/get_running_drivers",
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
            document.getElementById("driver_refresh_button").innerHTML = "<div class=\"btn btn-default btn-sm\">refresh <i class=\"fa fa-refresh\"></i></div>";
            $("#driver_names").fadeIn("slow", function() {});
//            document.getElementById("driver_names").style("color: black");
        },
        error: function(xhr, status, error) {
            if (xhr.status != 404) {
//                alert("unknown error:\n" + error);
                document.getElementById("driver_refresh_button").innerHTML = "<div class=\"btn btn-default btn-sm\">refresh <i class=\"fa fa-refresh\"></i></div>";
                document.getElementById("driver_names").innerHTML = "<li ><i style=\"color: pink\" class=\"fa fa-warning fa-2x\"></i></li>";
            } 
            else {
//                alert("404 xml not found");
                document.getElementById("driver_refresh_button").innerHTML = "<div class=\"btn btn-default btn-sm\">refresh <i class=\"fa fa-refresh\"></i></div>";
                document.getElementById("driver_names").innerHTML = "<i style=\"color: pink\" class=\"fa fa-warning fa-2x\"></i>";
            }
        }
    });
    
}

$( "#driver_refresh_button" ).click(function() {
  $( "#driver_names" ).fadeOut( "slow", function() {
    // Animation complete.
  });
});
    
function showCommandBox() {
    $("#send_command").slideToggle("slow");
}

$(document).ready(function() {
    //alert("For now the site will try to refresh the \"current running driver list\" every 30 seconds");
    getRunningDrivers();
    setInterval(getRunningDrivers, 30000);
});

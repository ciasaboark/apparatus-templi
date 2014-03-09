/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    console.log("reuquesting");
    document.getElementById("driver_refresh_button").onclick = "";
    document.getElementById("driver_names").innerHTML = "<i class=\"fa fa-spinner fa-spin fa-2x\"></i>";
    $.ajax({
        type: "GET",
        url: "/get_running_drivers",
        dataType: "xml",
        async: true,
        contentType: "application/xml; charset=\"utf-8\"",
        success: function(xml) {
            var $driverList = "";
            $(xml).find('Module').each(function() {
                var $module = $(this);
                var $name = $module.attr('name');
                console.log("found driver " + $name);
                $driverList += "<li><a href='driver_widget?driver=" + $name + "'>" + $name + "</a></li>";
            })
            document.getElementById("driver_names").innerHTML = $driverList;
        },
        error: function(xhr, status, error) {
            if (xhr.status != 404) {
                document.getElementById("driver_names").innerHTML = "<i style=\"color: pink\" class=\"fa fa-warning fa-2x\"></i>";
            } 
            else {
                document.getElementById("driver_names").innerHTML = "<i style=\"color: pink\" class=\"fa fa-warning fa-2x\"></i>";
            }
        }
    });
    document.getElementById("driver_refresh_button").onclick = function onclick(event) {getRunningDrivers()};
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

function updateConfigFile() {
    var $newConfig = document.getElementById('f_config_file').value;
//    alert($newConfig);
    document.getElementById('btn_conf_file').innerHTML = $newConfig;
}

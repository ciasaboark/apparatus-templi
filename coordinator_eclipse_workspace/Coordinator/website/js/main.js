/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    console.log("requesting driver list");
    document.getElementById("driver_refresh_button").onclick = "";
//    document.getElementById("driver_names").style.textAlign = "center";
    document.getElementById("drivers_refresh_spinner").style.visibility = "visible";
    document.getElementById("driver_names").style.color = "#ccc";
    $.ajax({
        type: "GET",
        url: "/drivers.xml",
        dataType: "xml",
        async: true,
        contentType: "application/xml; charset=\"utf-8\"",
        success: function(xml) {
            var $driverList = "";
            $(xml).find('Module').each(function() {
                var $module = $(this);
                var $name = $module.attr('name');
                console.log("found driver " + $name);
                $driverList += "<li><a href='widget.xml?driver=" + $name + "'>" + $name + "</a></li>";
            })
            document.getElementById("driver_names").style.textAlign = "left";
            document.getElementById("driver_names").innerHTML = $driverList;
            document.getElementById("drivers_refresh_spinner").style.visibility = "hidden";
            document.getElementById("driver_refresh_button").onclick = function onclick(event) {getRunningDrivers()};
        },
        error: function(xhr, status, error) {
            if (xhr.status != 404) {
                
                document.getElementById("driver_names").innerHTML = "";
                document.getElementById("drivers_refresh_spinner").classList = "";
                document.getElementById("drivers_refresh_spinner").classList = "fa fa-warning fa-2x";
                document.getElementById("driver_refresh_button").onclick = function onclick(event) {getRunningDrivers()};
            } 
            else {
                document.getElementById("driver_names").innerHTML = "";
                document.getElementById("drivers_refresh_spinner").classList = "";
                document.getElementById("drivers_refresh_spinner").classList = "fa fa-warning fa-2x";
                document.getElementById("driver_refresh_button").onclick = function onclick(event) {getRunningDrivers()};
//              document.getElementById("driver_names").innerHTML = "<i style=\"color: pink\" class=\"fa fa-warning fa-2x\"></i>";
//                document.getElementById("drivers_refresh_spinner").style.visibility = "hidden";
            }
        }
    });
    
    
}

$("#driver_refresh_button").click(function() {
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
    updateLog();
    setInterval(getRunningDrivers, 30000);
    setInterval(updateLog, 10000);
});

function updateConfigFile() {
    var $newConfig = document.getElementById('f_config_file').value;
    
    if ($newConfig == "coordinator.conf" || $newConfig == "") {
        if ($newConfig == "") {
            $newConfig = "<empty>";
        }
        document.getElementById('btn_conf_file').innerHTML = $newConfig;
        console.log("new config file name is the default config file");
        document.getElementById('form_submit').onclick = "";
        document.getElementById('form_submit').classList.add("disabled");
    } else {
        document.getElementById('btn_conf_file').innerHTML = $newConfig;
        console.log("new config file name not default config");
        document.getElementById('form_submit').onclick = function onclick(event) {document.getElementById('prefs').submit()};
        document.getElementById('form_submit').classList.remove("disabled");
    }
}

function updateLog() {
    console.log("updating log");
    document.getElementById("log_refresh_spinner").style.visibility = "visible";
    document.getElementById("log_refresh_button").onclick = "";
    var $logDiv = document.getElementById("log");
    $logDiv.style.color = "#ccc";
    $.ajax({
        url: "/log.txt",
        dataType: "text",
        async: true,
        success: function(txt) {
//            console.log("success");
//            console.log(txt);
            $logDiv.innerHTML = "";
            $logDiv.innerHTML = txt;
            $logDiv.scrollTop = $logDiv.scrollHeight;
            $logDiv.style.color = "black";
            document.getElementById("log_refresh_spinner").style.visibility = "hidden";
            document.getElementById("log_refresh_button").onclick = function onclick(event) {updateLog()};
        },
        error: function(xhr, status, error) {
            document.getElementById("log_refresh_spinner").style.visibility = "hidden";
            $logDiv.innerHTML = "Error getting log";
            document.getElementById("log_refresh_button").onclick = function onclick(event) {updateLog()};
        }
    });
    
}
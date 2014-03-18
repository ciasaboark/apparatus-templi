/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    if (document.getElementById('driver_names') != null) {
        console.log("requesting dr  iver list");
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
    renderWidgets();
    setInterval(getRunningDrivers, 30000);
    setInterval(updateLog, 10000);
    setInterval(renderWidgets(), 30000);
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
    if (document.getElementById('log') != null) {
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
}

function renderWidgets() {
    //get a list of all drivers the iterate through the list rendering the widgets
    if (document.getElementById('widgets_box') != null) {
        console.log("beginRenderWidgets()");
        var $widgetsHtml = "";
        $.ajax({
            type: "GET",
            url: "/drivers.xml",
            dataType: "xml",
            async: false,
            contentType: "application/xml; charset=\"utf-8\"",
            success: function(xml) {
                var $driverList = "";
                $(xml).find('Module').each(function() {
                    var $module = $(this);
                    var $name = $module.attr('name');
                    console.log("found driver " + $name);
                    //for every driver get that drivers xml
                    var $widgetHtml = renderWidget($name);
                    $widgetsHtml += $widgetHtml;
                    console.log($widgetHtml);
                })
            },
            error: function(xhr, status, error) {
                if (xhr.status != 404) {
                    console.log("error getting driver list");
                } 
                else {
                    console.log("error getting driver list");
                }
            }
        });
        document.getElementById('widgets_box').innerHTML = $widgetsHtml;
    }
}

function renderWidget(driverName) {
    console.log("renderWidget(" + driverName + ")");
    var widgetHtml = "<div class='widget'>";
    $.ajax({
        type: "GET",
        url: "/widget.xml?driver=" + driverName,
        dataType: "xml",
        async: false,
        contentType: "application/xml; charset=\"utf-8\"",
        success: function (xml) {
            //var to hold html
            $(xml).find('module').each(function() {
                var $module = $(this);
                var $longName = $module.attr('name');
                var $driver = $module.attr('driver');
                widgetHtml += "<h4>" + $longName + "</h4>";
                $(this).children().each(function() {
                    var $elementType = this.nodeName;
                    console.log("current element " + $elementType);
                    if ($elementType == "sensor") {
                        widgetHtml += renderSensorElement($(this).attr('name'));
                    } else if ($elementType == "controller") {
                        //TODO pull needed controller attributes and pass
                        widgetHtml += renderControllerElement($(this).attr('name'));
                    } else if ($elementType == "button") {
                        widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'));
                    } else {
                        console.log("unknown element type: " + $elementType);
                    }
                });
            });
        },
        error: function(xhr, status, error) {
            if (xhr.status != 404) {
                console.log("error getting driver list");
            } 
            else {
                console.log("error getting driver list");
            }
        }
    });
    widgetHtml += "</div>";
    return widgetHtml;
}

function renderSensorElement(name) {
    console.log("renderSensor() unimplemented");
    var $markup = "<div class='widget_sensor'><span>" + name + "</span></div>";
    return $markup;
}

function renderControllerElement(name) {
    console.log("renderController() unimplemented");
    var $markup = "<div class='widget_controller'><span>" + name + "</span></div>";
    return $markup;
}
    
function renderButtonElement(driver, title, action, input) {
    console.log("renderButton() unimplemented");
    var $markup = "<div class='widget_button'><a href='/send_command?driver=" + driver + "&command=" + action + "'><span>" + title + "</span></a></div>";
    return $markup;
}
    

// $.get(tocURL, function(toc) {
//    function makeToc($xml) {
//        // variable to accumulate markup
//        var markup = "";
//        // worker function local to makeToc
//        function processXml() {
//            markup += "<li><a href='" + $(this).attr("url") + "'>" + $(this).attr("title") + "</a>";
//            if (this.nodeName == "BOOK") {
//                markup += "<ul>";
//                // recurse on book children
//                $(this).find("page").each(processXml);
//                markup += "</ul>";
//            }
//            markup += "</li>";
//        }
//        // call worker function on all children
//        $xml.children().each(processXml);
//        return markup;
//    }
//    var tocOutput = makeToc($(toc));
//    $("#list").html(tocOutput);
//});

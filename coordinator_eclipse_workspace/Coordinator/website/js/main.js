var refreshIntervals = {};
var firstRefresh = {};
var widgetCache = {};
var widgetsToRestore = [];
var inFullScreenMode = false;


function preload(arrayOfImages) {
    $(arrayOfImages).each(function(){
        $('<img/>')[0].src = this;
        // Alternatively you could use:
        // (new Image()).src = this;
    });
}

// Usage:
/*
preload([
    'img/imageName.jpg',
    'img/anotherOne.jpg',
    'img/blahblahblah.jpg'
]);
*/

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
            timeout: 10000,
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
                console.log(error);
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

$(window).load(function() {
    //alert("For now the site will try to refresh the \"current running driver list\" every 30 seconds");
    //preload the background image
    preload([
        '/resource?file=images/stardust.png'
    ]);
    getRunningDrivers();
    updateLog();
    formSubmitHandler();
    slideDownSettingsButtons();
    setInterval(getRunningDrivers, 30000);
    setInterval(updateLog, 5000);
    renderWidgets();
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
            timeout: 3000,
            success: function(txt) {
    //            console.log("success");
    //            console.log(txt);
                $logDiv.innerHTML = "";
                $logDiv.innerHTML = txt;
                $logDiv.scrollTop = $logDiv.scrollHeight;
                document.getElementById("log_refresh_spinner").style.visibility = "hidden";
                document.getElementById("log_refresh_button").onclick = function onclick(event) {updateLog();};
            },
            error: function(xhr, status, error) {
                document.getElementById("log_refresh_spinner").style.visibility = "hidden";
                $logDiv.innerHTML = "Error getting log";
                document.getElementById("log_refresh_button").onclick = function onclick(event) {updateLog();};
                console.log(error);
            }
        });
    }  
}

function renderWidgets() {
    console.log("full screen mode? " + inFullScreenMode);
    if (inFullScreenMode === false) {
        console.log("renderWidgets()");
        //get a list of all drivers the iterate through the list rendering the widgets
        if (document.getElementById('widgets_box') !== null) {
            //clear any previous intervals (including widget updates
            for (var $key in refreshIntervals) {
                console.log("refresh intervals: " + refreshIntervals[$key]);
                clearInterval(refreshIntervals[$key]);
                delete refreshIntervals[$key];
            }

            var $widgetsHtml = "";

            var $widgets_div_html = "";
            $.ajax({
                type: "GET",
                url: "/drivers.xml",
                dataType: "xml",
                async: true,
                timeout: 10000,
                contentType: "application/xml; charset=\"utf-8\"",
                success: function(xml) {
                    var $widgets_div = document.getElementById('widgets_box').innerHTML = "";
                    var $driverList = "";
                    var $numDrivers = $(xml).find('Module').length;
                    var $step = 100 / $numDrivers;
                    var $progress = 0;
                    $('#widgets_progress').attr('max', $numDrivers);
                    $('#widgets_progress').attr('value', $progress);
                    $(xml).find('Module').each(function() {
                        //create a unique div for each module, then so an async call to update the contents of each div
                        var $module = $(this);
                        var $name = $module.attr('name');
                        document.getElementById('widgets_box').innerHTML += "<span style='visibility:hidden' id='widget-" + $name + "'></span>";
                        updateWidget($name);
                        $progress += $step;
                        $('#widgets_progress').val($progress);
                    });
                    //if there were no modules then we should do a short interval, otherwise once a minute should be fine
                    if ($numDrivers === 0) {
                        console.log("setting short refresh interval");
                        document.getElementById('widgets_box').innerHTML = "<div  style='width: 500pt; height: 100pt; text-align: center; position:absolute; left: 50%; top:50%; padding:10px; margin-left: -250pt; margin-top: -50pt;' class='info-box'><h1>No Modules Loaded</h1><i class=\"fa fa-info-circle\"></i>&nbsp;&nbsp;You can specify which drivers to load from the <a href='settings.html'>settings</a> page</div>";
                        firstRefresh = {};
                        $intervalNum = setInterval(renderWidgets, 5000);
                        refreshIntervals.renderWidget = $intervalNum;
                    } else {
                        console.log("setting long refresh interval");
                        $intervalNum = setInterval(renderWidgets, 60000);
                        refreshIntervals.renderWidget = $intervalNum;
                    }
                },
                error: function(xhr, status, error) {
                    if (xhr.status != 404) {
                        console.log("error getting driver list");
                    } 
                    else {
                        console.log("error getting driver list");
                    }
                    console.log(error);
                    document.getElementById('widgets_box').innerHTML = "<div class=\"info-box\" style\"width: 80%\"><div style=\"text-align: center\"><h1>Error Loading Widgets</h1><i class=\"fa fa-warning fa-2x\"></i><div><small>Please check your internet connection</small></div></div></div>";
                    //set a new refresh interval
                    console.log("setting medium refresh interval");
                    $intervalNum = setInterval(renderWidgets, 10000);
                    refreshIntervals.renderWidget = $intervalNum;
                }
            });
        }
    } else {
        console.log("will not refresh widget list until out of full screen mode");
    }
}

function updateWidget(driverName) {
    if (!inFullScreenMode) {
        var $id = '#widget-' + driverName;
        $($id).find('.fa-refresh').addClass('fa-spin');
        window.setTimeout(
            function(){
                $($id).find('.fa-refresh').removeClass('fa-spin');
            },500
        );
        //clear any previous intervals
        if (driverName in refreshIntervals) {
    //        console.log("clearing previous interval for " + driverName);
            var $intervalNum = refreshIntervals[driverName];
            clearInterval($intervalNum);
        } else {
    //        console.log("no previous interval set for " + driverName);
        }

    //    document.getElementById('widget-' + driverName).innerHTML = "<div class='widget info-box'><i style='position:absolute; left: 50%; top:50%;' class=\"fa fa-spinner fa-spin fa-2x\"></i></div>";
        var widgetHtml = "<div class='widget info-box' >";
        $.ajax({
            type: "GET",
            url: "/widget.xml?driver=" + driverName,
            dataType: "xml",
            async: true,
            timeout: 6000,
            contentType: "application/xml; charset=\"utf-8\"",
            success: function (xml) {
                var $prevXml = widgetCache[driverName];
                var $curXml = (new XMLSerializer()).serializeToString(xml);

                widgetCache[driverName] = $curXml;
                //var to hold html
                $(xml).find('module').each(function() {
                    var $module = $(this);
                    var $longName = $module.attr('name');
                    var $driver = $module.attr('driver');
                    var $refreshInterval = $module.attr('refresh');
                    console.log($driver + " refresh: " + $refreshInterval);
                    if ($refreshInterval === undefined) {
                        $refreshInterval = 60;
                    } else if ($refreshInterval < 3) {
                        $refreshInterval = 3;
                    } else if ($refreshInterval > 60) {
                        $refreshInterval = 60;
                    }
                    var $refreshOnClick = "updateWidget('" + $driver + "')";
                    var $expandWidgetOnClick = "expandWidget('" + $driver + "')";

                    widgetHtml += "<div class='title'><span class=\"refresh-btn\"><a onclick=\"" + $refreshOnClick + "\" ><i class=\"fa fa-refresh\"></i></a></span>" + $longName + "<span class=\"expand-btn\"><a onclick=\"" + $expandWidgetOnClick + "\" ><i class=\"fa fa-expand\"></i></a></span></div>";
                    widgetHtml += "<div class='content'>";
                    $(this).children().each(function() {
                        var $elementType = this.nodeName;
    //                    console.log("current element " + $elementType);
                        if ($elementType == "sensor") {
                            var $value = $(this).find('value').text();
                            widgetHtml += renderSensorElement($(this).attr('name'), $value);
                        } else if ($elementType == "controller") {
                            var $status = $(this).find('status').text();
                            widgetHtml += renderControllerElement($(this).attr('name'), $status);
                        } else if ($elementType == "button") {
                            widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'));
                        } else if ($elementType == "textarea") {
                            widgetHtml += renderTextAreaElement($(this).text());
                        } else if ($elementType == "pre") {
                            widgetHtml += renderPreElement($.trim( $(this).text()));
                        } else {
                            console.error("unknown element type: " + $elementType);
                        }
                    });
                    widgetHtml += "</div>"; //close content div
                    widgetHtml += "</div>"; //close widget div
                    if (document.getElementById('widget-' + driverName) !== null) {
                        $($id).css("visibility","visible");
    //                    console.log("updating id widget-" + driverName);

    //                    console.log("first refresh? " + firstRefresh[$id]);
                        //if this was the first time the widget was refresh then do a fancy dropdown animation
                        //+ otherwise flash the background
                        $($id).html(widgetHtml);
                        if (typeof firstRefresh[$id] == 'undefined') {
    //                        console.log($id + " first refresh");
                            $($id).removeClass();
                            window.setTimeout(
                                function(){
                                    $($id).addClass("animated fadeInDownBig");
    //                                document.getElementById('widget-' + driverName).innerHTML = widgetHtml;
                                },10
                            );
                            firstRefresh[$id] = "false";
                        } else if ($prevXml !== $curXml) {

    //                        console.log($id + " NOT first refresh");
                            $($id).removeClass();
    //                        document.getElementById('widget-' + driverName).innerHTML = widgetHtml;

                            $($id).find('.widget').addClass('bounce');
                            $($id).find('.widget').addClass('flash-border');
                            $($id).find('.title').addClass('flash-title');
                            window.setTimeout(
                                function(){
                                    $($id).find('.widget').removeClass("bounce");
                                    $($id).find('.widget').removeClass("flash-border");
                                    $($id).find('.title').removeClass('flash-title');
                                },2000
                            );

    //                        $($id).fadeOut(1).fadeIn(20);
                        }
    //                    document.getElementById('widget-' + driverName).innerHTML = widgetHtml;
                    } else {
                        console.error("unable to find id widget-" + driverName);
                    }

                    //set a new refresh interval
                    $intervalNum = setInterval(function() { updateWidget(driverName); }, $refreshInterval * 1000);
    //                console.log("setting interval to update " + driverName + " to " + $refreshInterval + " seconds.");
                    refreshIntervals[driverName] = $intervalNum;
                });
            },
            error: function(xhr, status, error) {
                console.log("unable to get xml for widget-" + driverName);
                console.log(error);
                //if the driver has no xml or does not exist then we do not want to insert
                //+ a div
                if (document.getElementById('widget-' + driverName) !== null) {
                    console.log("removing id widget-" + driverName);
                    $($id).removeClass();
                    $($id).addClass("animated fadeOutUpBig");

                    //scale out the widget horizontally 
                    window.setTimeout(
                        function() {
                            $($id).toggle({ effect: "scale", direction: "horizontal", duration: "6000"});
                        }, 600
                    );

                    //remove the entire widget, this should be timed to complete after the horizontal scale is completed
                    //TODO is there a callback method available?
                    window.setTimeout(
                        function(){
                            $($id).remove();
                            if ($("#widgets_box").is(':empty')) {
                                //if this was the last widget then we need to update the widget box
                                console.log("last widget removed, updating available widgets");
                                renderWidgets();
                                firstRefresh = {};
                            }
                        },1200
                    );


                } else {
                    console.log("unable to get id for widget-" + driverName);
                }
            }
        });
    } else {
        console.log("will not update widgets while in full screen mode");
    }
}

function renderSensorElement(name, value) {
//    console.log("renderSensor() unfinished");
    var $markup = "<div class='sensor'><span class='name'>" + name + "</span><span class='value'>" + value + "</span></div>";
    return $markup;
}

function renderControllerElement(name, status) {
//    console.log("renderController() unfinished");
    var $markup = "<div class='controller'><span class='name'>" + name + "</span><span class='status'>" + status + "</span></div>";
    return $markup;
}
    
function renderButtonElement(driver, title, action, input) {
//    console.log("renderButton() unfinished");
    var $markup = "<div class='button'><a ";
    var $buttonID =  "widget-input-" + driver + "-" + title;
    $markup += "onclick=\"widgetButtonOnClick('" + driver + "','" + $buttonID + "','" + action + "')\"><span class=' btn btn-default'>" + title + "</span></a>";
    if (input !== null && input != "none") {
        $markup += "<input type='";
        if (input == "text") {
            $markup += "text";
        } else if (input == "numeric") {
            $markup += "number";
        }
        $markup += "' ";
        $markup += "id='" + $buttonID + "'";
        $markup += "></input>";
    }
    $markup += "</div>";
    return $markup;
}

function renderTextAreaElement(content) {
    return "<p>" + content + "</p>";
}

function renderPreElement(content) {
    return content;
}

function slideDownSettingsButtons() {
    var $buttons = document.getElementById("settings-buttons");
    if ( $buttons !== null ){
////        $($buttons).slideUp(2000);
//        $($buttons).slideDown(1000);
//        $( $buttons ).fadeIn(1000);
        document.getElementById("settings-buttons").style.visibility = "visible";
        $('#settings-buttons').addClass('animated fadeInDownBig');
    }
}

function widgetButtonOnClick(driver, button, action) {
    var $id = '#widget-' + driver;
    $($id).find('.fa-refresh').addClass('fa-spin');
    console.log("button clicked");
    var $buttonInput = $("#" + button).val();
    var $actionCommand;
    if ($buttonInput !== null || $buttonInput !== "" || $buttonInput !== undefined) {
        $actionCommand = action.replace("$input", $buttonInput);
        console.log($actionCommand);
    } else {
        $actionCommand = action.replace("$input", "");
        console.log($actionCommand);
    }
    
    var $url = "/send_command";
    var $data = "?driver=" + driver + "&command=" + $actionCommand;
    $.ajax({
        type: "GET",
        url: $url + $data,
        success: function() {
            console.log("button command data '" + $actionCommand + "' sent correctly");
            window.setTimeout(
                function(){
                    updateWidget(driver);
                },600
            );
        },
        error: function() {
            console.log("command '" + $actionCommand + "' could not be sent");
        }
    });
    
}

function formSubmitHandler() {
    var $form = $("#prefs");
    if (document.getElementById("prefs") !== null) {
        $form.submit(function(e) {
            console.log("submit handler");
            console.log(e);
            e.preventDefault();
            e.stopPropagation();
        });
    } else {
        console.log("no prefs form found");
    }
}

function expandWidget(driver) {
    inFullScreenMode = true;
    //clear any widgets ids to restore
    widgetsToRestore = [];
    console.log("expandWidget() " + driver);
    $('#widgets_box span[id^="widget-"]').each(function() {
        console.log(this.id);
        //hide all other widgets
        if (this.id != "widget-" + driver) {
            console.log(this);
            var $id = this.id;
            console.log("found widget to hide: " + $id);
            widgetsToRestore.push($id);
            var $widget = $($id).find('.widget');
            $(this).removeClass();
            $(this).addClass('animated');
            $(this).addClass('fadeOutUpBig');
            console.log($id);
            window.setTimeout(
                function() {
                    console.log("removing widgets");
                    var thisWidget = document.getElementById($id);
                    console.log("parent element: " + thisWidget.parentElement);
                    thisWidget.parentElement.removeChild(thisWidget);
                }, 500
            );
        }
    });
    
    //expand this widget
    window.setTimeout(
        function() {
            console.log("expanding widget");
            $("#widget-" + driver).removeClass();
            $("#widget-" + driver).find('.widget').removeClass('widget');
            $("#widget-" + driver).find('.info-box').removeClass('info-box');
            $("#widget-" + driver).addClass('fullscreenWidget');
            $("#widget-" + driver).find(".content").html("");
            $("#widget-" + driver).find('.expand-btn').find('i').removeClass('fa-expand');
            $("#widget-" + driver).find('.expand-btn').find('i').addClass('fa-compress');
            $("#widget-" + driver).find('span').animate({
                width:"100%",
                height:"100%",
            },1400);
            window.setTimeout(
                function() {
                    $("#widget-" + driver).find(".content").html("<i class=\"fa fa-spinner fa-spin fa-2x busy\"></i>");
                    //***********************************************************
                    $("#widget-" + driver).find('.expand-btn').find('a').removeAttr("onClick");
                    $("#widget-" + driver).find('.expand-btn').click(function() {collapseFullScreenWidget();});
                    
                    $.ajax({
                        type: "GET",
                        url: "/full.xml?driver=" + driver,
                        dataType: "xml",
                        async: true,
                        timeout: 15000,
                        contentType: "application/xml; charset=\"utf-8\"",
                        success: function (xml) {
                            //var to hold html
                            $(xml).find('module').each(function() {
                                var $module = $(this);
//                                var $longName = $module.attr('name');
                                var $driver = $module.attr('driver');
                                var $refreshInterval = $module.attr('refresh');
                                console.log($driver + " refresh: " + $refreshInterval);
                                if ($refreshInterval === undefined) {
                                    $refreshInterval = 60;
                                } else if ($refreshInterval < 3) {
                                    $refreshInterval = 3;
                                } else if ($refreshInterval > 60) {
                                    $refreshInterval = 60;
                                }
                                console.log($("#widget-" + driver).find('.expand-btn').find('a').attr("onClick"));
                                

                                widgetHtml = "";
                                $(this).children().each(function() {
                                    var $elementType = this.nodeName;
                //                    console.log("current element " + $elementType);
                                    if ($elementType == "sensor") {
                                        var $value = $(this).find('value').text();
                                        widgetHtml += renderSensorElement($(this).attr('name'), $value);
                                    } else if ($elementType == "controller") {
                                        var $status = $(this).find('status').text();
                                        widgetHtml += renderControllerElement($(this).attr('name'), $status);
                                    } else if ($elementType == "button") {
                                        widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'));
                                    } else if ($elementType == "textarea") {
                                        widgetHtml += renderTextAreaElement($(this).text());
                                    } else if ($elementType == "pre") {
                                        widgetHtml += renderPreElement($.trim( $(this).text()));
                                    } else {
                                        console.error("unknown element type: " + $elementType);
                                    }
                                });
                                $("#widget-" + driver).find(".content").html(widgetHtml);
                            });
                        },
                        error: function(xhr, status, error) {
                            console.log("unable to get full xml for widget-" + driver);
                            console.log(error);
                            $("#widget-" + driver).find(".content").html("<div class=\"info-box\" style\"width: 80%\"><div style=\"text-align: center\"><h1>Error Loading Widget</h1><i class=\"fa fa-warning fa-2x\"></i><div><small>Please check your internet connection</small></div></div></div>");
                        }
                    });
                    
                    //***********************************************************
                    
                }, 1000
            );
        }, 1200
    );
}

function collapseFullScreenWidget() {
    console.log("collapseFullscreenWidget()");
//    widgetCache = {};
    inFullScreenMode = false;
    $("#widgets_box").html("");
    renderWidgets();
}
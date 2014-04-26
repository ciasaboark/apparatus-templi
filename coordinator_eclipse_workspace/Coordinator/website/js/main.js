/*jslint browser:true */
/*global $ */
var refreshIntervals = {};
var firstRefresh = {};
var widgetCache = {};
var widgetsToRestore = [];
var inFullScreenMode = false;


$(window).load(function() {
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

$(document).ready(function () {
	buildTooltips();
});

function preload(arrayOfImages) {
    $(arrayOfImages).each(function () {
        $('<img/>')[0].src = this;
        // Alternatively you could use:
        // (new Image()).src = this;
    });
}

/*
 * Ajax request to update the running driver list
 */
function getRunningDrivers() {
    if (document.getElementById('driver_names') !== null) {
        console.log("requesting driver list");
        document.getElementById("driver_refresh_button").onclick = "";
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
                });
                document.getElementById("driver_names").style.textAlign = "left";
                document.getElementById("driver_names").innerHTML = $driverList;
                document.getElementById("drivers_refresh_spinner").style.visibility = "hidden";
                document.getElementById("driver_refresh_button").onclick = function onclick() {getRunningDrivers();};
            },
            error: function(xhr, status, error) {
                if (xhr.status != 404) {
                    
                    document.getElementById("driver_names").innerHTML = "";
                    document.getElementById("drivers_refresh_spinner").classList = "";
                    document.getElementById("drivers_refresh_spinner").classList = "fa fa-warning fa-2x";
                    document.getElementById("driver_refresh_button").onclick = function onclick() {getRunningDrivers();};
                } 
                else {
                    document.getElementById("driver_names").innerHTML = "";
                    document.getElementById("drivers_refresh_spinner").classList = "";
                    document.getElementById("drivers_refresh_spinner").classList = "fa fa-warning fa-2x";
                    document.getElementById("driver_refresh_button").onclick = function onclick() {getRunningDrivers();};
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
    
/*jshint unused: true */
/*exported showCommandBox */
function showCommandBox() {
    $("#send_command").slideToggle("slow");
}


/*jshint unused: true */
/*exported updateConfigFile */
function updateConfigFile() {
    var $newConfig = document.getElementById('f_config_file').value;
    
    if ($newConfig == "coordinator.conf" || $newConfig === "") {
        if ($newConfig === "") {
            $newConfig = "<empty>";
        }
        document.getElementById('btn_conf_file').innerHTML = $newConfig;
        console.log("new config file name is the default config file");
        document.getElementById('form_submit').onclick = "";
        document.getElementById('form_submit').classList.add("disabled");
    } else {
        document.getElementById('btn_conf_file').innerHTML = $newConfig;
        console.log("new config file name not default config");
        document.getElementById('form_submit').onclick = function onclick() {document.getElementById('prefs').submit();};
        document.getElementById('form_submit').classList.remove("disabled");
    }
}

function updateLog() {
    if (document.getElementById('log') !== null) {
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
                $logDiv.innerHTML = "";
                $logDiv.innerHTML = txt;
                $logDiv.scrollTop = $logDiv.scrollHeight;
                document.getElementById("log_refresh_spinner").style.visibility = "hidden";
                document.getElementById("log_refresh_button").onclick = function onclick() {updateLog();};
            },
            error: function(xhr, status, error) {
                document.getElementById("log_refresh_spinner").style.visibility = "hidden";
                $logDiv.innerHTML = "Error getting log";
                document.getElementById("log_refresh_button").onclick = function onclick() {updateLog();};
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
            //clear any previous intervals (including widget updates)
            for (var $key in refreshIntervals) {
                console.log("refresh intervals: " + refreshIntervals[$key]);
                clearInterval(refreshIntervals[$key]);
                delete refreshIntervals[$key];
            }
			
			document.getElementById('widgets_box').innerHTML = "";

            $.ajax({
                type: "GET",
                url: "/drivers.xml",
                dataType: "xml",
                async: true,
                timeout: 10000,
                contentType: "application/xml; charset=\"utf-8\"",
                success: function(xml) {
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
                        //try to render all the widgets async,
                        //+ if the server is single threaded then this will not work
                        window.setTimeout(function() {
                            console.log("building widget " + $name);
                            updateWidget($name);
                        },1);
                        $progress += $step;
                        $('#widgets_progress').val($progress);
                    });
                    //if there were no modules then we should do a short interval, otherwise once a minute should be fine
                    if ($numDrivers === 0) {
                        console.log("setting short refresh interval");
                        document.getElementById('widgets_box').innerHTML = "<div  style='width: 500pt; height: 100pt; text-align: center; position:absolute; left: 50%; top:50%; padding:10px; margin-left: -250pt; margin-top: -50pt;' class='info-box'><h1>No Modules Loaded</h1><i class=\"fa fa-info-circle\"></i>&nbsp;&nbsp;You can specify which drivers to load from the <a href='settings.html'>settings</a> page</div>";
                        firstRefresh = {};
                    } else {
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
        $($id).find('.title').find('.fa-refresh').addClass('fa-spin');
        window.setTimeout(
            function(){
                $($id).find('.fa-refresh').removeClass('fa-spin');
            },500
        );
        
        var widgetHtml = "<div class='widget info-box' >";
        $.ajax({
            type: "GET",
            url: "/widget.xml?driver=" + driverName,
            dataType: "xml",
            async: true,
            timeout: 30000,
            contentType: "application/xml; charset=\"utf-8\"",
            success: function (xml) {
                //clear any previous intervals
                if (driverName in refreshIntervals) {
                    var $intervalNum = refreshIntervals[driverName];
                    clearInterval($intervalNum);
                } else {
                }
                var $prevXml = widgetCache[driverName];
                var $curXml = (new XMLSerializer()).serializeToString(xml);

                widgetCache[driverName] = $curXml;
                //var to hold html
                $(xml).find('module').each(function() {
                    var $module = $(this);
                    var $longName = $module.attr('name');
                    var $driver = $module.attr('driver');
                    var $refreshInterval = $module.attr('refresh');
                    console.log($driver + " " + $refreshInterval);
                    if ($refreshInterval === undefined) {
                        $refreshInterval = 60;
                    } else if ($refreshInterval < 3) {      //refresh at most once every 3 seconds
                        $refreshInterval = 3;
                    } else if ($refreshInterval >60 * 30) { //refresh at least once every 30 minutes
                        $refreshInterval = 60 * 30;
                    }
                    var $refreshOnClick = "updateWidget('" + $driver + "')";
                    var $expandWidgetOnClick = "expandWidget('" + $driver + "')";

                    widgetHtml += "<div class='title'><span class=\"refresh-btn\"><a onclick=\"" + $refreshOnClick + "\" ><i class=\"fa fa-refresh\"></i></a></span>" + $longName + "<span class=\"expand-btn\"><a onclick=\"" + $expandWidgetOnClick + "\" ><i class=\"fa fa-expand\"></i></a></span></div>";
                    widgetHtml += "<div class='content'>";
                    $(this).children().each(function() {
                        var $elementType = this.nodeName;
                        if ($elementType == "sensor") {
                            var $value = $(this).find('value').text();
                            widgetHtml += renderSensorElement($(this).attr('name'), $value, $(this).attr('description'));
                        } else if ($elementType == "controller") {
                            var $status = $(this).find('status').text();
                            widgetHtml += renderControllerElement($(this).attr('name'), $status, $(this).attr('description'));
                        } else if ($elementType == "button") {
                            widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'), $(this).attr('inputVal'), $(this).attr('icon'), $(this).attr('description'));
                        } else if ($elementType == "textarea") {
                            widgetHtml += renderTextAreaElement($(this).text(), $(this).attr('description'));
                        } else if ($elementType == "pre") {
                            widgetHtml += renderPreElement($.trim( $(this).text()), $(this).attr('description'));
                        } else {
                            console.error("unknown element type: " + $elementType);
                        }
                    });
                    widgetHtml += "</div>"; //close content div
                    widgetHtml += "</div>"; //close widget div
                    if (document.getElementById('widget-' + driverName) !== null) {
                        $($id).css("visibility","visible");
                        //if this was the first time the widget was refresh then do a fancy dropdown animation
                        //+ otherwise flash the background
                        
                        if (typeof firstRefresh[$id] == 'undefined') {
							$($id).html(widgetHtml);
                            $($id).removeClass();
                            window.setTimeout(
                                function(){
                                    $($id).addClass("animated fadeInDownBig");
                                },10
                            );
                            firstRefresh[$id] = "false";
                        } else if ($prevXml !== $curXml) {
                            $($id).removeClass();
							$($id).html(widgetHtml);
                            $($id).find('.widget').find('.content').addClass('bounce-custom');
                            $($id).find('.widget').addClass('flash-border');
                            $($id).find('.widget .title').addClass('flash-title');
							
                            window.setTimeout(
                                function(){
									
                                    $($id).find('.widget').find('.content').removeClass("bounce-custom");
                                    $($id).find('.widget').removeClass("flash-border");
                                    $($id).find('.widget .title').removeClass('flash-title');
                                },1200
                            );
                        }
                    } else {
                        console.error("unable to find id widget-" + driverName);
                    }
					
					//rebuild tooltips
					buildTooltips();

                    //set a new refresh interval
                    $intervalNum = setInterval(function() { updateWidget(driverName); }, $refreshInterval * 1000);
                    refreshIntervals[driverName] = $intervalNum;
                });
            },
            error: function(xhr, status, error) {
                console.log(error);
                //if the driver has no xml or does not exist then we do not want to insert
                //+ a div
                if (document.getElementById('widget-' + driverName) !== null) {
                    $($id).removeClass();
                    if (!$($id).find('.title').hasClass('title-err')) {
                        $($id).find('.title').addClass('flash-title-err');
                        window.setTimeout(function() {
                            $($id).find('.title').addClass('title-err').removeClass('flash-title-err');
                        }, 1000);
                    }
                    if ($($id).find('.widget').find('.content').hasClass('grayscale')) {
                        console.log("widget already disabled");
                    } else {
                        $($id).find('.widget').find('.content').addClass("blurout");
                        window.setTimeout(function() {
                            $($id).find('.widget').find('.content').addClass('grayscale').removeClass('blurout');
                        }, 1000);
                    }
                } else {
                }
//                //set a short refresh interval
//                $intervalNum = setInterval(function() { updateWidget(driverName); }, 3000);
//                refreshIntervals[driverName] = $intervalNum;
            }
        });
    } else {
    }
}

function renderSensorElement(name, value, desc) {
	if (desc === undefined) {
		desc = "";
	}
    var $markup = "<div class='sensor'><span class='name'>" + name + "</span><span class='value' title='" + desc + "' >" + value + "</span></div>";
    return $markup;
}

function renderControllerElement(name, status, desc) {
	if (desc === undefined) {
		desc = "";
	}
    var $markup = "<div class='controller'><span class='name' title='" + desc + "' >" + name + "</span><span class='status'>" + status + "</span></div>";
    return $markup;
}
    
function renderButtonElement(driver, title, action, input, inputval, icon, desc) {
//	if (desc === undefined) {
//		desc = "";
//	}
    var title_id = title.split(' ').join('_');
    console.log("renderButtonElement()" + "driver '" + driver + "' title '" +  title + "' action '" +  action + "' input '" + input + "' inputval '" +  inputval + "' icon '" +  icon + "' desc '" + desc + "'");
    
    if (inputval === null || inputval === undefined) {
        inputval = "";
    }
    if (icon === null || icon === undefined) {
        icon = "";
    }
    
    var $markup = "<div class='button'><a ";
    var $buttonID =  "widget-input-" + driver + "-" + title_id;
    $markup += "onclick=\"widgetButtonOnClick('" + driver + "','" + title_id + "','" + action + "')\"><span class='btn btn-default'" + ((input === 'none') ? ("title='" + desc + "'") : ("")) + ">";
    if (icon !== "") {
        $markup += "<i class='icon " + icon + "'>&nbsp;&nbsp;</i>";
    }
    $markup += title + "</span></a>";
    if (input !== null && input !== "none") {
        $markup += "<input type='";
        if (input === "text") {
            $markup += "text' title='" + desc + "' ";
        } else if (input == "numeric") {
            $markup += "number' title='" + desc + "' ";
        }
//        $markup += "' ";
        $markup += "id='" + $buttonID + "' ";
        console.log("button value '" + inputval + "' ");
        $markup += "value='" + inputval + "' ";
        $markup += "></input>";
    }
    //every button has a area set aside to display status info
    //this area should be used to indicate whether the action was received
    //correctly
    $markup += "<span class='button-status' id=\"button-status-" + driver + "-" + title_id + "\"></span>";
    $markup += "</div>";
    return $markup;
}

function renderTextAreaElement(content, desc) {
	if (desc === undefined) {
		desc = "";
	}
    return "<p title='" + desc + "' >" + content + "</p>";
}

function renderPreElement(content, desc) {
	if (desc === undefined) {
		desc = "";
	}
    return "<span title='" + desc + "' >" + content + "</span>";
}

function slideDownSettingsButtons() {
    var $buttons = document.getElementById("settings-buttons");
    if ( $buttons !== null ){
        document.getElementById("settings-buttons").style.visibility = "visible";
        $('#settings-buttons').addClass('animated fadeInDownBig');
    }
}

/*jshint unused: true */
/*exported widgetButtonOnClick */
function widgetButtonOnClick(driver, button_id, action) {
    console.log("widgetButtonOnClick() " + driver + " " + button_id + " " + action);
//    var $id = '#widget-' + driver;
    var inputAreaId = '#widget-input-' + driver + "-" + button_id;
    console.log("input area id: " + inputAreaId);
    var buttonStatusArea = 'button-status-' + driver + "-" + button_id;
    console.log("button-status id: " + buttonStatusArea);
    
    //show a spinner beside the button
    document.getElementById(buttonStatusArea).innerHTML = "<i class='fa fa-spinner fa-spin'></i>";
    
    //$($id).find('.title').find('.fa-refresh').addClass('fa-spin');
    
    var $buttonInput = $(inputAreaId).val();
    console.log("input area val " + $buttonInput);
    
    var $actionCommand;
    if ($buttonInput !== null || $buttonInput !== "" || $buttonInput !== undefined) {
        $actionCommand = action.replace("$input", $buttonInput);
    } else {
        $actionCommand = action.replace("$input", "");
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
            //show that commmand was received
            document.getElementById(buttonStatusArea).innerHTML = "<i class='fa fa-check-circle' style='color:#57ff57'></i>";
        },
        error: function() {
            console.log("command '" + $actionCommand + "' could not be sent");
            document.getElementById(buttonStatusArea).innerHTML = "<i class='fa fa-exclamation-circle' style='color:#f84c4c'></i>";
        }
    });
    
}

function formSubmitHandler() {
    var $form = $("#prefs");
    if (document.getElementById("prefs") !== null) {
        $form.submit(function(e) {
            e.preventDefault();
            e.stopPropagation();
        });
    } else {
    }
}

/*jshint unused: true */
/*exported expandWidget */
function expandWidget(driver) {
    inFullScreenMode = true;
    //clear any widgets ids to restore
    widgetsToRestore = [];
    console.log("expandWidget() " + driver);
    $('#widgets_box span[id^="widget-"]').each(function() {
        //hide all other widgets
        if (this.id !== "widget-" + driver) {
            var $id = this.id;
            widgetsToRestore.push($id);
//            var $widget = $($id).find('.widget');
            $(this).removeClass().addClass('animated fadeOutUpBig');
            window.setTimeout(
                function() {
                    $(this).removeClass('animated fadeOutUpBig');
                    var thisWidget = document.getElementById($id);
                    thisWidget.parentElement.removeChild(thisWidget);
                }, 1000
            );
        } else {
            $(this).removeClass().addClass('animated bounceOut');
            window.setTimeout(
                function() {
                    $(this).hide();
                    $(this).removeClass('animated bounceOut');
//                    var thisWidget = document.getElementById($id);
//                    thisWidget.parentElement.removeChild(thisWidget);
                }, 1000
            );
        }
    });
//
//    window.setTimeout(function() {
//        $("#widget-" + driver).addClass('animated fadeOut');
//        window.setTimeout(function() {
//            $("#widget-" + driver).removeClass('animated fadeOut');
//        },1000);
//    }, 1000);
    
    //expand this widget
    window.setTimeout(
        function() {
            $("#widget-" + driver).removeClass();
            $("#widget-" + driver).find('.widget').removeClass('widget');
            $("#widget-" + driver).find('.info-box').addClass('fullscreenWidget');
            $("#widget-" + driver).find('.info-box').removeClass('info-box');
            $("#widget-" + driver).find('.title').hide();
            $("#widget-" + driver).find('.fullscreenWidget').addClass('animated bounceIn');
            $("#widget-" + driver).find('.content').removeClass('grayscale');
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
                    $("#widget-" + driver).find('.expand-btn').find("a").attr('onClick', 'collapseFullScreenWidget(\"' + driver + '\")');
                    $("#widget-" + driver).find('.refresh-btn').find("a").attr('onClick', 'refreshFullScreenWidget(\"' + driver + '\")');
                    $("#widget-" + driver).find('.title').addClass('animated fadeInDown');
					$("#widget-" + driver).find('.content').addClass('animated fadeInDownBig');					
                    $("#widget-" + driver).find('.title').show();
                    
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

                                var widgetHtml = "";
                                $(this).children().each(function() {
                                    var $elementType = this.nodeName;
                                    if ($elementType == "sensor") {
                                        var $value = $(this).find('value').text();
                                        widgetHtml += renderSensorElement($(this).attr('name'), $value, $(this).attr('description'));
                                    } else if ($elementType == "controller") {
                                        var $status = $(this).find('status').text();
                                        widgetHtml += renderControllerElement($(this).attr('name'), $status, $(this).attr('description'));
                                    } else if ($elementType == "button") {
                                        widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'), $(this).attr('inputVal'), $(this).attr('icon'), $(this).attr('description'));
                                    } else if ($elementType == "textarea") {
                                        widgetHtml += renderTextAreaElement($(this).text(), $(this).attr('description'));
                                    } else if ($elementType == "pre") {
                                        widgetHtml += renderPreElement($.trim( $(this).text()), $(this).attr('description'));
                                    } else {
                                        console.error("unknown element type: " + $elementType);
                                    }
                                });
                                $("#widget-" + driver).find(".content").hide();
                                $("#widget-" + driver).find(".content").html(widgetHtml);
                                $("#widget-" + driver).find(".content").addClass('animated fadeIn');
								$("#widget-" + driver).find(".content").translate('latin');
                                $("#widget-" + driver).find(".content").show();
                            });
							
							//rebuild tooltips
							buildTooltips();
                        },
						statusCode: {
							//the server is up, but the widget did not provide any full screen widget xml
							404: function() {
								$("#widget-" + driver).find(".content").html("<div class=\"info-box\" style\"width: 80%\"><div style=\"text-align: center\"><h1>Error Loading Widget</h1><i class=\"fa fa-warning fa-2x\"></i><div><small>This driver does not provide a full screen widget.</small></div></div></div>");
							}
						},
                        error: function(xhr, status, error) {
							//either the server is not up or we received an unknown error code
                            console.log(error);
							$("#widget-" + driver).find(".content").html("<div class=\"info-box\" style\"width: 80%\"><div style=\"text-align: center\"><h1>Error Loading Widget</h1><i class=\"fa fa-warning fa-2x\"></i><div><small>Please check your internet connection</small></div></div></div>");
                        }
                    });                    
                }, 1000
            );
        }, 1500
    );
}

/*jshint unused: true */
/*exported collapseFullScreenWidget */
function collapseFullScreenWidget(driver) {
    console.log("collapseFullscreenWidget()" + driver);
    var $id = $("#widget-" + driver);
    $($id).find('.fullscreenWidget').css("border-radius", "16px");
    $($id).find('.fullscreenWidget').removeClass().addClass('fullscreenWidget animated bounceOut');
    window.setTimeout(function() {
        $($id).find('.fullscreenWidget').removeClass('animated bounceOut');
        $($id).find('.fullscreenWidget').hide();
        
        $("#widgets_box").empty();
        $("#widgets_box").hide();
        inFullScreenMode = false;
		widgetCache = {};
        renderWidgets();
        window.setTimeout(function() {
            $("#widgets_box").addClass('animated fadeIn');
            $("#widgets_box").show();
            window.setTimeout(function() {
                $("#widgets_box").removeClass("animated fadeIn");
            }, 1000);
        }, 1000);
    }, 1000);
    
    
}

//convert element title attribute to tooptips 
function buildTooltips() {
	console.log("replacing tooltips with javascript tooltips");
	$('[title]').qtip({
		position: {
			viewport: $(window)
		},
		style: {
			classes: "qtip-rounded qtip-light tooltip-custom"	
		}
	});
	$('input[title]').qtip({
        position: {
			viewport: $(window)
		},
		style: {
			classes: "qtip-rounded qtip-light tooltip-custom"	
		},
        show: {
            event: 'focus'
        },
        hide: {
			event: 'blur'
        }
    });
	
}

/*jshint unused: true */
/*exported refreshFullScreenWidget */
function refreshFullScreenWidget(driver) {
	if (inFullScreenMode) {
		$("#widget-" + driver).find(".title").find(".fa-refresh").addClass("fa-spin");
		$("#widget-" + driver).find(".content").html("<i class=\"fa fa-spinner fa-spin fa-2x busy\"></i>");
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
					var $driver = $module.attr('driver');
					var $longName = $module.attr('name');
					var $refreshInterval = $module.attr('refresh');
					
					console.log($driver + " refresh: " + $refreshInterval);
					if ($refreshInterval === undefined) {
						$refreshInterval = 60;
					} else if ($refreshInterval < 3) {
						$refreshInterval = 3;
					} else if ($refreshInterval > 60) {
						$refreshInterval = 60;
					}

					var widgetHtml = "";
					$(this).children().each(function() {
						var $elementType = this.nodeName;
						if ($elementType == "sensor") {
							var $value = $(this).find('value').text();
							widgetHtml += renderSensorElement($(this).attr('name'), $value, $(this).attr('description'));
						} else if ($elementType == "controller") {
							var $status = $(this).find('status').text();
							widgetHtml += renderControllerElement($(this).attr('name'), $status, $(this).attr('description'));
						} else if ($elementType == "button") {
							widgetHtml += renderButtonElement($driver, $(this).attr('title'), $(this).attr('action'),$(this).attr('input'), $(this).attr('inputVal'), $(this).attr('icon'), $(this).attr('description'));
						} else if ($elementType == "textarea") {
							widgetHtml += renderTextAreaElement($(this).text(), $(this).attr('description'));
						} else if ($elementType == "pre") {
							widgetHtml += renderPreElement($.trim( $(this).text()), $(this).attr('description'));
						} else {
							console.error("unknown element type: " + $elementType);
						}
					});
					$("#widget-" + driver).find(".content").hide();
					//apply the new title and body content
					console.log("new title: " + $longName);
					console.log("new body: " + widgetHtml);
					var $titlebar = "<span class='refresh-btn'><a onclick='" + refreshFullScreenWidget( "$driver" ) + "'></a></span>";
					$titlebar += $longName;
					$titlebar += "<span class='expand-btn'><a onclick='" + collapseFullScreenWidget( "$driver" ) + "'></a></span>";
					
					$("#widget-" + driver).find(".title").find("span").html($titlebar);
					$("#widget-" + driver).find(".content").html(widgetHtml);
					
					$("#widget-" + driver).find(".content").addClass('animated fadeIn');
					$("#widget-" + driver).find(".content").show();
				});

				//rebuild tooltips
				buildTooltips();
				$("#widget-" + driver).find(".title").find(".fa-refresh").removeClass("fa-spin");
				
			},
			error: function(xhr, status, error) {
				console.log(error);
				$("#widget-" + driver).find(".content").html("<div class=\"info-box\" style\"width: 80%\"><div style=\"text-align: center\"><h1>Error Loading Widget</h1><i class=\"fa fa-warning fa-2x\"></i><div><small>Please check your internet connection</small></div></div></div>");
				$("#widget-" + driver).find(".title").find(".fa-refresh").removeClass("fa-spin");
			}
		}); 
	}
}

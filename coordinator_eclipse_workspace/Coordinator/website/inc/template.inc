<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    
<link rel="stylesheet" type="text/css" href="resource?file=css/index.css">
<link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/themes/smoothness/jquery-ui.css" />
<link href='http://fonts.googleapis.com/css?family=Varela+Round' rel='stylesheet' type='text/css'>
<link href='http://fonts.googleapis.com/css?family=Noto+Serif' rel='stylesheet' type='text/css'>
<link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="resources?file=css/foundation-icons.css">
    
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script src="resource?file=js/main.js"></script>
<script src="js/default.js"></script>

<title>Apparatus Templi</title>
</head>

<body>
	<div id="header">
        
        <div id="header_bar">
            <div id="header_title">
                <span id="header_logo">
                    <img src="resource?file=images/small_logo.png" alt="small logo">
                </span>
                <span id="header_title_text">Apparatus Templi</span>
            </div>
            <!-- <div id="header_spacer"></div> -->
            <div id="header_links">
                <span>
                    <a id="button_home"class="btn btn-default btn-sm" href="index.html">
                        <i class="fa fa-code"></i>&nbsp;&nbsp;Widgets
                    </a>
                </span>
                
                <span>
                    <a id="button_about" class="btn btn-default btn-sm" href="about.html">
                        <i class="fa fa-info"></i>&nbsp;&nbsp;About
                    </a>
                </span>
                <span>
                    <a id="button_settings" class="btn btn-default btn-sm" href="settings.html">
                        <i class="fa fa-cog"></i>&nbsp;&nbsp;Settings
                    </a>
                </span>
            </div>
        </div>
        
        <div id="send_command">
            Driver Name:<input class="input" type="text"/><br><br>
            Command:<input class="input" type="text"/>
        </div>
        
<!--
    	<div id="header_content">
            <div id="logo">
                <img src="resource?file=images/Apparatus%20Templi-logo.png" alt="Apparatus Templi Logo">
            </div>
        </div>
-->
    </div>
    
    <div id="main_content">
        MAIN_CONTENT
        <div id="main_content_padding"></div>
    </div>
    <div id="RunningDrivers">
        <div id="driver_list">
            <div>Loaded Divers</div>
            <ul id="driver_names">
                <li><i class="fa-li fa fa-spinner fa-spin fa-2x"></i></li>
            </ul>
            <div id="driver_refresh_button" href="#" onclick="getRunningDrivers()">
                <div class="btn btn-default btn-sm">refresh <i class="fa fa-refresh"></i></div>
            </div>
        </div>
    </div>
    <div id="footer">
        <span>
            <span id="footer_arduino" >
                <a href="http://www.arduino.cc">
                    <img src="resource?file=images/arduino_icon.png" height="32px" alt="Device List">
                </a>
            </span>
            <span id="footer_github">
                <a href="https://github.com/ciasaboark/apparatus-templi">
                    <i class="fa fa-github-alt fa-2x"></i>
                </a>
            </span>
        </span>
    </div>
</body>
</html>
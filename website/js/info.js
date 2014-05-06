/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*jslint browser:true */
/*global $ */

$(window).load(function() {
	getRunningDrivers();
	getSysStatus();
	updateLog();
	setInterval(getRunningDrivers, 30000);
	setInterval(getSysStatus, 3000);
	setInterval(updateLog, 5000);
});


$("#driver_refresh_button").click(function() {
    $( "#driver_names" ).fadeOut( "slow", function() {
        // Animation complete.
    });
});

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


function getSysStatus() {
	if (document.getElementById('sys_status') !== null) {
		console.log("requesting system status");
		
		//get the service uptime
		$.ajax({
			type: "GET",
            url: "/sys_status?status=uptime",
            dataType: "text",
            async: true,
            timeout: 10000,
            contentType: "text/plain; charset=\"utf-8\"",
            success: function(text) {
				var uptime = "<p>Uptime: " + text + " seconds</p>";
				console.log(uptime);
				document.getElementById('sys_status_uptime').innerHTML = uptime;
			},
			error: function(xhr, status, error) {
				console.log("unable to read uptime: " + error);
				var uptime = "<p>Uptime: unknown</p>";
				console.log(uptime);
				document.getElementById('sys_status_uptime').innerHTML = uptime;
			}	
		});
		
		//get free disk space
		$.ajax({
			type: "GET",
            url: "/sys_status?status=freedisk",
            dataType: "text",
            async: true,
            timeout: 10000,
            contentType: "text/plain; charset=\"utf-8\"",
            success: function(text) {
				var disk = "<p>Free space: " + text + " MB</p>";
				console.log(disk);
				document.getElementById('sys_status_disk').innerHTML = disk;
			},
			error: function(xhr, status, error) {
				console.log("unable to read free disk space: " + error);
				var disk = "<p>Free space: unknown MB</p>";
				console.log(disk);
				document.getElementById('sys_status_disk').innerHTML = disk;
			}	
		});
		
		//get load avgerage
		$.ajax({
			type: "GET",
            url: "/sys_status?status=loadavg",
            dataType: "text",
            async: true,
            timeout: 10000,
            contentType: "text/plain; charset=\"utf-8\"",
            success: function(text) {
				var load = "<p>Load average: " + parseFloat(text * 100).toFixed(2) + " %</p>";
				console.log(load);
				document.getElementById('sys_status_load').innerHTML = load;
			},
			error: function(xhr, status, error) {
				console.log("unable to read load avg: " + error);
				var load = "<p>Load average: unknown</p>";
				console.log(load);
				document.getElementById('sys_status_load').innerHTML = load;
			}	
		});
		
		//get remote modules list
		$.ajax({
			type: "GET",
            url: "/sys_status?status=modules",
            dataType: "text",
            async: true,
            timeout: 10000,
            contentType: "text/plain; charset=\"utf-8\"",
            success: function(text) {
				if (text === "") {
					text = "<p>No remote modules</p>";
				}
				console.log(text);
				document.getElementById('known_modules').innerHTML = text;
			},
			error: function(xhr, status, error) {
				console.log("unable to read module list: " + error);
				var text = "<p>No remote modules</p>";
				console.log(text);
				document.getElementById('known_modules').innerHTML = text;
			}	
		});
		
	}
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
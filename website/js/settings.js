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
	formSubmitHandler();
    slideDownSettingsButtons();
});

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

function slideDownSettingsButtons() {
    var $buttons = document.getElementById("settings-buttons");
    if ( $buttons !== null ){
        document.getElementById("settings-buttons").style.visibility = "visible";
        $('#settings-buttons').addClass('animated fadeInDownBig');
    }
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
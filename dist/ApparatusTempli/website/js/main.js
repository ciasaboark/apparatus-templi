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

$(document).ready(function () {
	buildTooltips();
});

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

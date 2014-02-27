function loadXMLDoc() {
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
		xmlhttp.open("GET","http://localhost:8000/get_running_drivers", true );
	}
	else {// code for IE6, IE5
  		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");	
		xmlhttp.open("GET","http://localhost:8000/get_running_drivers", true );
	}
	xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			document.getElementById("runningDrivers").innerHTML=xmlhttp.responseText;
			alert(xmlhttp.responseText);
	    }
		else {
			alert("error");
		}
  	}
	xmlhttp.send();
}

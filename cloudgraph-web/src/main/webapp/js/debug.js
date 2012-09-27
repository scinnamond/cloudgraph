   	var reportStatus = new Array();
   	
	function report ( msg ) {
    	reportStatus.push ( msg );
	}
	function showReport ( err ) {
    	alert ( reportStatus.join ( "\n" ) );
	}
	window.onerror = function ( err, url, line ) {
    	report ( err + " [" + url + " - line " + line + "]" );
	    showReport();
	}

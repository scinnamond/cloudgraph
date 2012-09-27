
function showCalendar(str_target, str_datetime) {
	var arr_months = ["January", "February", "March", "April", "May", "June",
		"July", "August", "September", "October", "November", "December"];
	var week_days = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
	var n_weekstart = 1; // day week starts from (normally 0 or 1)

	var dt_datetime = (str_datetime == null || str_datetime =="" ?  new Date() : str2dt(str_datetime));
	var today = new Date();
	var dt_prev_month = new Date(dt_datetime);
	if (today.getMonth() == dt_datetime.getMonth()-1)
		dt_prev_month.setMonth(dt_datetime.getMonth()-1,today.getDate());
	else
		dt_prev_month.setMonth(dt_datetime.getMonth()-1,1);
	var dt_next_month = new Date(dt_datetime);
	
	if ((today.getMonth() == dt_datetime.getMonth()+1) || (dt_datetime.getMonth() == 11 && today.getMonth() == 0))
		dt_next_month.setMonth(dt_datetime.getMonth()+1,today.getDate());
	else
		dt_next_month.setMonth(dt_datetime.getMonth()+1,1);
	var dt_firstday = new Date(dt_datetime);
	dt_firstday.setDate(1);
	dt_firstday.setDate(1-(7+dt_firstday.getDay()-n_weekstart)%7);
	var dt_lastday = new Date(dt_next_month);
	dt_lastday.setDate(0);
	   
    var escaped_str_target = str_target.replace(/\['/, "[\\\'");
    escaped_str_target = escaped_str_target.replace(/'\]/, "\\\']");   
    
	// html generation (feel free to tune it for your particular application)
	// print calendar header
	var str_buffer = new String (
		"<html>\n"+
		"<head>\n"+
		"	<title>Calendar</title>\n"+
		"</head>\n"+
		"<body bgcolor=\"White\">\n"+
		"<table class=\"clsOTable\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n"+
		"<tr><td bgcolor=\"#4682B4\">\n"+
		"<table cellspacing=\"1\" cellpadding=\"3\" border=\"0\" width=\"100%\">\n"+
		"<tr>\n	<td bgcolor=\"#4682B4\"><a href=\"javascript:window.opener.showCalendar('"+
		escaped_str_target+"', '"+ dt2dtstr(dt_prev_month)+"');\">"+
		"<img src=\"/counseling/images/prev.gif\" width=\"16\" height=\"16\" border=\"0\""+
		" alt=\"previous month\"></a></td>\n"+
		"	<td bgcolor=\"#4682B4\" colspan=\"5\">"+
		"<font color=\"white\" face=\"tahoma, verdana\" size=\"2\">"
		+arr_months[dt_datetime.getMonth()]+" "+dt_datetime.getFullYear()+"</font></td>\n"+
		"	<td bgcolor=\"#4682B4\" align=\"right\"><a href=\"javascript:window.opener.showCalendar('"
		+escaped_str_target+"', '"+dt2dtstr(dt_next_month)+"');\">"+
		"<img src=\"/counseling/images/next.gif\" width=\"16\" height=\"16\" border=\"0\""+
		" alt=\"next month\"></a></td>\n</tr>\n"
	);

	var dt_current_day = new Date(dt_firstday);
	// print weekdays titles
	str_buffer += "<tr>\n";
	for (var n=0; n<7; n++)
		str_buffer += "	<td bgcolor=\"#87CEFA\">"+
		"<font color=\"white\" face=\"tahoma, verdana\" size=\"2\">"+
		week_days[(n_weekstart+n)%7]+"</font></td>\n";
	// print calendar table
	str_buffer += "</tr>\n";
	while (dt_current_day.getMonth() == dt_datetime.getMonth() ||
		dt_current_day.getMonth() == dt_firstday.getMonth()) {
		// print row heder
		str_buffer += "<tr>\n";
		for (var n_current_wday=0; n_current_wday<7; n_current_wday++) {
				if (dt_current_day.getDate() == dt_datetime.getDate() &&
					dt_current_day.getMonth() == dt_datetime.getMonth())
					// print current date
					str_buffer += "	<td bgcolor=\"#FFB6C1\" align=\"right\">";
				else if (dt_current_day.getDay() == 0 || dt_current_day.getDay() == 6)
					// weekend days
					str_buffer += "	<td bgcolor=\"#DBEAF5\" align=\"right\">";
				else
					// print working days of current month
					str_buffer += "	<td bgcolor=\"white\" align=\"right\">";

				if (dt_current_day.getMonth() == dt_datetime.getMonth())
					// print days of current month
					str_buffer += "<a href=\"javascript:window.opener."+str_target+
					".value='"+dt2dtstr(dt_current_day)+"'; window.opener."+str_target+".focus(); window.close();\">"+
					"<font color=\"black\" face=\"tahoma, verdana\" size=\"2\">";
				else
					// print days of other months
					str_buffer += "<a href=\"javascript:window.opener."+str_target+
					".value='"+dt2dtstr(dt_current_day)+"'; window.opener."+str_target+".focus(); window.close();\">"+
					"<font color=\"gray\" face=\"tahoma, verdana\" size=\"2\">";
				str_buffer += dt_current_day.getDate()+"</font></a></td>\n";
				dt_current_day.setDate(dt_current_day.getDate()+1);
		}
		// print row footer
		str_buffer += "</tr>\n";
	}
	// print calendar footer
	str_buffer +=
		"<form name=\"cal\">\n<tr><td colspan=\"7\" bgcolor=\"#87CEFA\">"+
		"<font color=\"White\" face=\"tahoma, verdana\" size=\"2\">"+
		"<input type=\"hidden\" name=\"time\" value=\""+dt2tmstr(dt_datetime)+
		"\" size=\"8\" maxlength=\"8\"></font></td></tr>\n</form>\n" +
		"</table>\n" +
		"</tr>\n</td>\n</table>\n" +
		"</body>\n" +
		"</html>\n";

 
	var vWinCal = window.open(null, "Calendar",
		"width=200,height=250,status=no,resizable=yes,top=200,left=200"); 		
	vWinCal.opener = self;
	var calc_doc = vWinCal.document;
	calc_doc.write (str_buffer);
	calc_doc.close();
}
// datetime parsing and formatting routimes. modify them if you wish other datetime format
function str2dt (str_datetime) {
	var re_date = /^(\d+)\-(\D+)\-(\d+)$/;
	if (!re_date.exec(str_datetime))
		return alert("Invalid Datetime format: "+ str_datetime);
	var vmonth = RegExp.$2;
	vmonth = vmonth.toUpperCase();
	switch(vmonth){
		case "JAN":
			index = 1;
			break;
		case "FEB":
			index = 2;
			break;
		case "MAR":
			index = 3;
			break;
		case "APR":
			index = 4;
			break;
		case "MAY":
			index = 5;
			break;
		case "JUN":
			index = 6;
			break;
		case "JUL":
			index = 7;
			break;
		case "AUG":
			index = 8;
			break;
		case "SEP":
			index = 9;
			break;
		case "OCT":
			index = 10;
			break;
		case "NOV":
			index = 11;
			break;
		case "DEC":
			index = 12;
			break;

	}


	return (new Date (RegExp.$3, index-1, RegExp.$1));
}
function dt2dtstr (dt_datetime) {
	var arr_months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
		"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

	var vday = dt_datetime.getDate();
	var vmonth = dt_datetime.getMonth();

	if(vday < 10)
		vday = '0' + vday;

	return (new String (
			vday+"-"+arr_months[vmonth]+"-"+dt_datetime.getFullYear()));
}
function dt2tmstr (dt_datetime) {
	return (new String (
			dt_datetime.getHours()+":"+dt_datetime.getMinutes()+":"+dt_datetime.getSeconds()));
}

function dt2tmstr (dt_datetime) {
	return (new String (
			dt_datetime.getHours()+":"+dt_datetime.getMinutes()+":"+dt_datetime.getSeconds()));
}

//**********************************************************************************************************************//
//		Date Validator functions
//
//**********************************************************************************************************************//
// function to restrict any non-numeric key pressing

function nn_Key(){
	if(((event.keyCode<48) || (event.keyCode>57)) && (event.keyCode != 45))
	event.keyCode = 0;
}

// function to check leap year
function isLeapYear(yr){
	if(yr % 2 == 0)
		return true;
		return false;

}

function max_day(mn,yr){
	var mDay;
	if((mn == 3) || (mn == 5) || (mn == 8) || (mn == 10)){
		mDay = 30;
	}else if(mn == 2){
		mDay = isLeapYear(yr) ? 29: 28;
	}else{
		mDay = 31;
	}
	return mDay;
}


function isValidDate(dd,mm,yyyy){

	var chk = 0;
	var maxDay = 0;

	maxDay = max_day(mm,yyyy);


	if((dd <= 0) || (dd > maxDay)){
		chk = 1;
//	}else if((mm <= 0) || (mm > 12)){
	}else if((mm < 0) || (mm > 11)){
		chk = 1;
	}else if(yyyy <= 0){
		chk = 1;
	}

	if(chk == 1)
		return false;			// invalid date
	else
		return true;	 		// valid date
}



/**************************************************************************************

formattedDate returns Day or Month or Year depending on the code passed

**************************************************************************************/


function formattedDate(dateStr,code){

	// you can either have date as 10-Nov-2002 or 2002-Nov-10

	var re_date = /^(\d+)\-(\D+)\-(\d+)$/;

	if (!re_date.exec(dateStr))
		return alert("Invalid Date format");

	var vmonth = RegExp.$2;
	vmonth = vmonth.toUpperCase();

	var dayYearStr = RegExp.$1;			// find out if RegExp1 is day or Year ?


	if(dayYearStr.length == 4){
		vYear = RegExp.$1;
		vDay = RegExp.$3;

	}else{
		vYear = RegExp.$3;
		vDay = RegExp.$1;

	}

	switch(vmonth){
		case "JAN":
			index = 1;
			break;
		case "FEB":
			index = 2;
			break;
		case "MAR":
			index = 3;
			break;
		case "APR":
			index = 4;
			break;
		case "MAY":
			index = 5;
			break;
		case "JUN":
			index = 6;
			break;
		case "JUL":
			index = 7;
			break;
		case "AUG":
			index = 8;
			break;
		case "SEP":
			index = 9;
			break;
		case "OCT":
			index = 10;
			break;
		case "NOV":
			index = 11;
			break;
		case "DEC":
			index = 12;
			break;

	}


	switch(code){
		case "D":
			return vDay;
		case "M":
			return (index-1);
		case "Y":
			return vYear;
	}




}


function isValidDateFormat(dateStr){

	var re_date = /^(\d+)\-(\D+)\-(\d+)$/;

	if (!re_date.exec(dateStr))
		return false;

	var Str1 = RegExp.$1;			// find out if RegExp1 is day or Year ?
	var Str2 = RegExp.$3;			// find out if RegExp3 is day or Year ?

	var Str3 = RegExp.$2;			// find out if RegExp2 is month in mmm format

	if((Str1.length == 4) && (Str2.length == 2) && (Str3.length == 3))
		return true;
	else if ((Str1.length == 2) && (Str2.length == 4) && (Str3.length == 3))
		return true;
	else
		return false;
}

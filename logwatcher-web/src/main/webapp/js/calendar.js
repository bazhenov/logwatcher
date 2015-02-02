function buildCal(baseDate, urlCallback){
	var mn=['January','February','March','April','May','June','July','August','September','October','November','December'];
	var cD = "days";

	var dim=[31,0,31,30,31,30,31,31,30,31,30,31];

	baseDate.day = baseDate.getDay() + 1;

	var todaydate = new Date();
	var y = baseDate.getFullYear();
	var m = baseDate.getMonth()+1;
	var scanfortoday = (y==todaydate.getFullYear() && m==todaydate.getMonth()+1)? todaydate.getDate() : 0;

	dim[1]=(((baseDate.getFullYear()%100!=0)&&(baseDate.getFullYear()%4==0))||(baseDate.getFullYear()%400==0))?29:28;
	var t = '<table cols="7" cellpadding="0" border="0" cellspacing="0"><tr align="center">';
	t+='<td colspan="7" align="center" class="month"><a class="prevMonthLink" href="#">&larr;</a> '  
		+ mn[m-1]+' - ' + y + ' <a class="nextMonthLink" href="#">&rarr;</a>' + '</td></tr><tr align="center">';
	for( s=0; s<7; s++) {
		t += '<th>'+"SMTWTFS".substr(s,1)+'</th>';
	}
	t+='</tr><tr align="center">';
	for ( i=1; i<=42; i++) {
		if ( ( i - baseDate.day >= 0 ) && ( i - baseDate.day < dim[m-1]) ) {
			var day = i - baseDate.day + 1;
			var iDate = new Date(baseDate);
			iDate.setDate(day);
			var url = urlCallback(iDate);

			x = ( (baseDate.getMonth() < todaydate.getMonth()) || (day <= scanfortoday) )
				? "<a href='"+url+"'>" + day + "</a>"
				: day;
			if ( baseDate.getMonth() >= todaydate.getMonth() && day > scanfortoday ) {
				x = '<span class="inactive">'+x+'</span>';
			}else if ( day == scanfortoday ) {
				x = '<span class="today">'+x+'</span>';
			}

		}else{
			x = '&nbsp;';
		}
		t += '<td>' + x + '</td>';
		if( ((i)%7==0)&&(i<36) )
			t+='</tr><tr align="center">';
	}
	return t+='</tr></table>';
}
package org.bazhenov.logging.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.farpost.timepoint.Date;
import static com.farpost.timepoint.Date.today;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.LinkedHashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

@Controller
public class FeedController {

	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	@RequestMapping(value = "/feed")
	public String handleFeed(ModelMap map, HttpServletRequest request) throws ParseException {

		Date today = today();
		Date date;
		String dateStr = request.getParameter("date");
		if ( dateStr == null ) {
			date = today;
		} else {
			date = new Date(format.parse(dateStr).getTime());
		}

		Map<String, Date> dates = new LinkedHashMap<String, Date>();
		dates.put("сегодня", today);
		dates.put("вчера", today.minusDay(1));
		dates.put("позавчера", today.minusDay(2));
		map.addAttribute("dates", dates);

		map.addAttribute("date", date);
		map.addAttribute("severity", getSeverity(request));

		return "index";
	}

	private String getSeverity(HttpServletRequest request) {
		String get = request.getParameter("severity");
		if ( get != null ) {
			return get;
		}

		Cookie[] cookies = request.getCookies();
		for ( Cookie cookie : cookies ) {
			if ( "severity".equals(cookie.getName()) ) {
				return cookie.getValue();
			}
		}

		return "error";
	}
}

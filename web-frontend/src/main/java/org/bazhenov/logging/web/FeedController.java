package org.bazhenov.logging.web;

import com.farpost.timepoint.Date;
import static com.farpost.timepoint.Date.today;
import org.bazhenov.logging.AggregatedLogEntry;
import org.bazhenov.logging.Severity;
import org.bazhenov.logging.storage.InvalidCriteriaException;
import static org.bazhenov.logging.storage.LogEntries.entries;
import org.bazhenov.logging.storage.LogStorage;
import org.bazhenov.logging.storage.LogStorageException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FeedController {

	private final ThreadLocal<DateFormat> format = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	private LogStorage storage;

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping(value = "/")
	public View handleRoot() {
		return new RedirectView("/feed", true);
	}

	@RequestMapping(value = "/feed")
	public String handleFeed(ModelMap map, HttpServletRequest request) throws ParseException,
		LogStorageException, InvalidCriteriaException {

		Date today = today();
		Date date;
		String dateStr = request.getParameter("date");
		if ( dateStr == null ) {
			date = today;
		} else {
			date = new Date(format.get().parse(dateStr).getTime());
		}

		Map<String, Date> dates = new LinkedHashMap<String, Date>();
		dates.put("сегодня", today);
		dates.put("вчера", today.minusDay(1));
		dates.put("позавчера", today.minusDay(2));
		if ( date.lessThan(today.minusDay(2)) ) {
			dates.put(date.toString(), date);
		}
		map.addAttribute("dates", dates);

		map.addAttribute("date", date);
		map.addAttribute("prevDate", date.minusDay(1));
		if ( date.lessThan(today()) ) {
			map.addAttribute("nextDate", date.plusDay(1));
		}
		String severity = getSeverity(request);
		map.addAttribute("severity", severity);
		List<AggregatedLogEntry> entries = entries().
			date(date).
			severity(Severity.forName(severity)).
			find(storage);

		int times = 0;
		for ( AggregatedLogEntry entry : entries ) {
			times += entry.getCount();
		}
		map.addAttribute("entries", entries);
		map.addAttribute("times", times);

		return "feed";
	}

	private String getSeverity(HttpServletRequest request) {
		String get = request.getParameter("severity");
		if ( get != null ) {
			return get;
		}

		Cookie[] cookies = request.getCookies();
		if ( cookies != null ) {
			for ( Cookie cookie : cookies ) {
				if ( "severity".equals(cookie.getName()) ) {
					return cookie.getValue();
				}
			}
		}

		return "error";
	}
}

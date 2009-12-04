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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.*;
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

	private final ThreadLocal<DateFormat> fullFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("d MMMM");
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
	public String handleFeed(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws ParseException,
		LogStorageException, InvalidCriteriaException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		Date today = today();
		Date date;
		String dateStr = request.getParameter("date");
		if ( dateStr == null ) {
			date = today;
		} else {
			date = new Date(format.get().parse(dateStr).getTime());
		}

		Map<String, java.util.Date> dates = new LinkedHashMap<String, java.util.Date>();
		dates.put("today", today.asDate());
		dates.put("yesterday", today.minusDay(1).asDate());
		dates.put("2 days ago", today.minusDay(2).asDate());
		if ( date.lessThan(today.minusDay(2)) ) {
			dates.put(fullFormat.get().format(date.asDate()), date.asDate());
		}
		map.addAttribute("dates", dates);

		map.addAttribute("date", date.asDate());
		map.addAttribute("prevDate", date.minusDay(1).asDate());
		if ( date.lessThan(today()) ) {
			map.addAttribute("nextDate", date.plusDay(1).asDate());
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

	@RequestMapping("/feed/rss")
	public String handleRss(ModelMap map, @RequestParam("severity") String severity) throws
		LogStorageException, InvalidCriteriaException {

		List<AggregatedLogEntry> entries = entries().
			date(today()).
			severity(Severity.forName(severity)).
			find(storage);

		map.addAttribute("entries", entries);

		return "feed-rss";
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

package org.bazhenov.logging.web;

import com.farpost.timepoint.Date;
import static com.farpost.timepoint.Date.today;
import org.bazhenov.logging.*;
import static org.bazhenov.logging.storage.LogEntries.entries;
import org.bazhenov.logging.storage.*;
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
	private QueryTranslator translator = new AnnotationDrivenQueryTranslator(new TranslationRulesImpl());

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	@RequestMapping("/")
	public View handleRoot() {
		return new RedirectView("/feed", true);
	}

	@RequestMapping("/entry/remove")
	public View removeEntry(@RequestParam("checksum") String checksum) throws LogStorageException {
		storage.removeEntries(checksum);
		return new BufferView("Ok");
	}

	@RequestMapping("/feed")
	public String handleFeed(@RequestParam(value = "query", required = false) String query, ModelMap map,
	                         HttpServletRequest request, HttpServletResponse response)
		throws ParseException, LogStorageException, InvalidCriteriaException, InvalidQueryException {

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
		List<AggregatedLogEntry> entries;
		if ( query != null && query.trim().length() > 0 ) {
			List<LogEntryMatcher> matchers = translator.translate(query.trim());
			if ( !contains(matchers, DateMatcher.class) ) {
				matchers.add(new DateMatcher(today()));
			}
			entries = entries().
				withCriteria(matchers).
				find(storage);
		}else{
			String severity = getSeverity(request);
			map.addAttribute("severity", severity);
			entries = entries().
				date(date).
				severity(Severity.forName(severity)).
				find(storage);
		}
		map.addAttribute("query", query);

		int times = 0;
		for ( AggregatedLogEntry entry : entries ) {
			times += entry.getCount();
		}
		map.addAttribute("entries", entries);
		map.addAttribute("times", times);

		return "feed";
	}

	private boolean contains(List<LogEntryMatcher> matchers, Class<? extends LogEntryMatcher> type) {
		for ( LogEntryMatcher matcher : matchers ) {
			if ( matcher.getClass().equals(type) ) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping("/feed/rss")
	public String handleRss(ModelMap map, @RequestParam(value = "severity", required = false) String s) throws
		LogStorageException, InvalidCriteriaException {

		Severity severity = s == null
			? Severity.error
			: Severity.forName(s);
		List<AggregatedLogEntry> entries = entries().
			date(today()).
			severity(severity).
			find(storage);

		map.addAttribute("entries", entries);
		map.addAttribute("date", new java.util.Date());

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

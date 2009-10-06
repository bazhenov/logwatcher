import com.farpost.timepoint.Date
import static com.farpost.timepoint.Date.*
import com.farpost.timepoint.DateTime
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.storage.LogStorage
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.bazhenov.logging.frontend.RussianDateFormat
import static org.bazhenov.logging.storage.LogEntries.entries
import javax.servlet.http.Cookie
import org.bazhenov.logging.Severity

class IndexController {

	LogStorage logStorage
	DateFormat format = new SimpleDateFormat("yyyy-MM-dd")

	def index = {
		String dateStr = params.date as String
		Date date
		if ( dateStr ) {
			date = new Date(format.parse(dateStr))
		} else {
			date = today()
		}

		Cookie cookie = request.cookies.find { it.name == 'severity' } as Cookie
		Severity cookieSeverity = Severity.forName(cookie?.value);
		Severity requestSeverity = Severity.forName(params.severity);
		Severity severity = null;

		if ( requestSeverity ) {
			severity = requestSeverity
			if ( requestSeverity != cookieSeverity ) {
				response.addCookie(new Cookie("severity", severity as String))
			}
		}else if ( cookieSeverity ) {
			severity = cookieSeverity
		}else{
			severity = Severity.error
			response.addCookie(new Cookie("severity", severity as String))
		}

		def app = params.application
		def storageEntries = entries().date(date).severity(severity).find(logStorage)
		def filteredEntries = storageEntries.findAll { filterCriteria(app, it) }
		def allApps = allApplications(storageEntries)
		def entries	= filteredEntries.collect { new Entry(it) }

		[
			entries: entries,
			today: Date.today(),
			date: date,
			dateAsString: new RussianDateFormat().format(date.asDate()),
			linkDates: ['сегодня': today(), 'вчера': today().minusDay(1), 'позавчера': today().minusDay(2)],
			application: app,
			allApps: allApps,
			severity: severity
		]
	}

	def removeEntry = {
		String checksum = params.checksum as String
		Date date = params.date ? new Date(format.parse(params.date))	: today()
		logStorage.removeEntries(checksum, date)
		render status: 200
	}

	private allApplications(entries){
		entries.sampleEntry.applicationId.unique()
	}

	private filterCriteria(appFilter, entry){
		appFilter ? entry.sampleEntry.applicationId == appFilter : true
	}
}

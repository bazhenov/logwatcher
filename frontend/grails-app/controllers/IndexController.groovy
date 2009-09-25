import com.farpost.timepoint.Date
import static com.farpost.timepoint.Date.*
import com.farpost.timepoint.DateTime
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.storage.LogStorage
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.bazhenov.logging.frontend.FrontendDateFormat

class IndexController {

	LogStorage logStorage
	DateFormat format = new SimpleDateFormat("yyyy-MM-dd")

	def index = {
		String dateStr = params.date as String
		Date date;
		if ( dateStr ) {
			date = new Date(format.parse(dateStr))
		} else {
			date = today()
		}

		def app = params.application
		def storageEntries = logStorage.getEntries(date)
		def filteredEntries = storageEntries.findAll { filterCriteria(app, it) }
		def allApps = allApplications(storageEntries)
		def entries	= filteredEntries.collect { new Entry(it) }

		[
			entries: entries,
			today: Date.today(),
			date: date,
			dateAsString: new FrontendDateFormat().format(date.asDate()),
			linkDates: ['сегодня': today(), 'вчера': today().minusDay(1), 'позавчера': today().minusDay(2)],
			application: app,
			allApps: allApps
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

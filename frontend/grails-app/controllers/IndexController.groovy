import com.farpost.timepoint.Date
import com.farpost.timepoint.DateTime
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.storage.LogStorage
import java.text.DateFormat
import java.text.SimpleDateFormat

class IndexController {

	LogStorage logStorage
	DateFormat format = new SimpleDateFormat("yyyy-MM-dd")

	def index = {
		String dateStr = params.date as String
		Date date;
		if ( dateStr ) {
			date = new Date(format.parse(dateStr))
		} else {
			date = DateTime.today()
		}

		def app = params.getApplicationId
		def storageEntries = logStorage.getEntries(date)
		def filteredEntries = storageEntries.findAll { filterCriteria(app, it) }
		def allApps = allApplications(storageEntries)
		def entries	= filteredEntries.collect { new Entry(it) }

		[entries: entries, today: Date.today(), date: date,
				application: app, allApps: allApps]
	}

	private allApplications(entries){
		entries.sampleEntry.getApplicationId.unique()
	}

	private filterCriteria(appFilter, entry){
		appFilter ? entry.sampleEntry.getApplicationId == appFilter : true
	}
}

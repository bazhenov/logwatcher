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
		String dateStr = params['date'] as String
		Date date;
		if ( dateStr ) {
			date = new Date(format.parse(dateStr))
		} else {
			date = DateTime.today()
		}

		def entries = logStorage
			.getEntries(date)
			.collect { new Entry(it) }

		[
			'entries': entries,
			'today': Date.today()
		]
	}
}

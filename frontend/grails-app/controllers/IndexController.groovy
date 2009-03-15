import com.farpost.timepoint.DateTime
import org.bazhenov.logging.frontend.Entry
import org.bazhenov.logging.storage.LogStorage

class IndexController {

	LogStorage logStorage

	def index = {
		def entries = logStorage
			.getEntries(DateTime.today())
			.collect { new Entry(it) }

		[ 'entries': entries ]
	}
}

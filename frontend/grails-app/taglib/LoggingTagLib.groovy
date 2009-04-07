import org.bazhenov.logging.frontend.Entry
import java.text.SimpleDateFormat

class LoggingTagLib {

	public final int MAX_LENGTH = 100;
	def dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", new Locale("En"))

	def entry = {attrs, body ->
		String title
		String content
		int count
		String lastTime
		String message;

		def entry = attrs['ref'] as Entry
		if ( entry ) {
			title = entry.title
			content = entry.text
			count = entry.count
			lastTime = dateFormat.format(entry.lastTime.asDate())
			message = entry.getText()
		} else {
			title = attrs['title']
			content = attrs['content']
			count = attrs['count']
			message = body()
		}

		if ( title.length() > MAX_LENGTH ) {
			title = title.substring(0, MAX_LENGTH) + "..."
		}

		def isEmpty = !content || content.length() <= 0;
		def severity = attrs['severity'] ?: 'info'
		def className = "logEntry ${severity}"
		if ( isEmpty ) {
			className += " empty";
		}
		def listener = isEmpty ? '' : "onclick='return toggle(this.parentNode); return false;'"


		out << "<div class='${className}'>"
		out << "<div class='entryHeader' ${listener}>"
		out << "<div style='clear: both'></div>"
		out << "<div class='message'>${title.encodeAsHTML()}</div>"
		if ( count > 1 ) {
			out << "<div class='count'>${count} times</div>"
		}
		if ( lastTime ) {
			out << "<div class='date'>Last: ${lastTime}</div>"
		}
		out << "<div style='clear: both'></div>"
		out << "</div>"
		out << "<div class='entryContent'>${content.encodeAsHTML()}</div>"
		out << "</div>"
	}
}


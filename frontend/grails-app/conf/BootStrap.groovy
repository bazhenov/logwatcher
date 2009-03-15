import org.bazhenov.logging.storage.LogStorage
import org.bazhenov.logging.LogEntry
import com.farpost.timepoint.DateTime
import org.bazhenov.logging.Severity
import org.springframework.web.context.support.WebApplicationContextUtils
import org.bazhenov.logging.Cause

class BootStrap {

	def init = {servletContext ->
		def appCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
		def storage = appCtx.getBean("logStorage") as LogStorage

		Cause cause = new Cause("str1", "str2", "str3")
		storage.writeEntry(new LogEntry(DateTime.now(), "Hi", "Hi", Severity.info, "oops", cause))
		storage.writeEntry(new LogEntry(DateTime.now(), "Hi", "Hi", Severity.info, "oops", cause))
		storage.writeEntry(new LogEntry(DateTime.now(), "Hi", "Hi", Severity.info, "oops", cause))
	}
	def destroy = {
	}
}
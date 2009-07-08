import org.bazhenov.logging.storage.LogStorage
import org.bazhenov.logging.LogEntry
import com.farpost.timepoint.DateTime
import org.bazhenov.logging.Severity
import org.springframework.web.context.support.WebApplicationContextUtils
import org.bazhenov.logging.Cause
import org.codehaus.groovy.grails.commons.ApplicationAttributes 

class BootStrap {
	def init = {servletContext ->
//		def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
//		LogStorage s = ctx.logStorage
//		s.writeEntry new LogEntry(DateTime.now(), "group", "advert message", Severity.error, "sum", "advert");
//		s.writeEntry new LogEntry(DateTime.now(), "group", "billling message", Severity.error, "sum2", "billing");
//		s.writeEntry new LogEntry(DateTime.now(), "group", "search message", Severity.error, "sum3", "search");
//		s.writeEntry new LogEntry(DateTime.now(), "group", "search message2", Severity.error, "sum4", "search");
	}

	def destroy = {
	}
}
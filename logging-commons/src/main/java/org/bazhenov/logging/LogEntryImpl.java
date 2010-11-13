package org.bazhenov.logging;

import com.farpost.timepoint.DateTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "logEntry")
public class LogEntryImpl implements LogEntry {

	@XmlJavaTypeAdapter(value = JaxbDateAdapter.class)
	@XmlAttribute
	private final DateTime date;

	@XmlElement
	private final String message;

	@XmlElement(name = "severity")
	private final SeverityContainer severity;

	private volatile String checksum;

	@XmlJavaTypeAdapter(value = JaxbAttributesMapAdapter.class)
	private final Map<String, String> attributes;

	@XmlElement
	private final Cause cause;

	@XmlElement(name = "application")
	private ApplicationContainer applicationContainer;

	@XmlElement(name = "group")
	private GroupContainer groupContainer;

	/**
	 * Этот конструктор не предназначен для прямого ипользования. Нужен для корректной работы JAXB.
	 */
	@Deprecated
	public LogEntryImpl() {
		this(null, null, null, null, null, null, null, null);
	}

	public LogEntryImpl(DateTime date, String group, String message, Severity severity, String checksum,
	                String applicationId, Map<String, String> attributes) {
		this(date, group, message, severity, checksum, applicationId, attributes, null);
	}

	public LogEntryImpl(DateTime date, String group, String message, Severity severity, String checksum,
	                String applicationId, Map<String, String> attributes, Cause cause) {
		this.date = date;
		this.groupContainer = new GroupContainer(group);
		this.message = message;
		this.severity = new SeverityContainer(severity);
		this.checksum = checksum;
		this.applicationContainer = new ApplicationContainer(applicationId);
		this.cause = cause;
		this.attributes = attributes == null
			? new HashMap<String, String>()
			: new HashMap<String, String>(attributes);
	}

	@Override
	public DateTime getDate() {
		return date;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Severity getSeverity() {
		return severity.getSeverity();
	}

	public SeverityContainer getSeverityContainer() {
		return severity;
	}

	@Override
	public String getCategory() {
		return groupContainer.getGroup();
	}

	public GroupContainer getGroupContainer() {
		return groupContainer;
	}

	@Override
	public Cause getCause() {
		return cause;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	@XmlAttribute
	public String getChecksum() {
		return checksum;
	}

	@Override
	public String getApplicationId() {
		return applicationContainer.getApplicationId();
	}

	public ApplicationContainer getApplicationContainer() {
		return applicationContainer;
	}

	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LogEntryImpl logEntry = (LogEntryImpl) o;

		if (applicationContainer != null ? !applicationContainer.equals(logEntry.applicationContainer) : logEntry.applicationContainer != null)
			return false;
		if (attributes != null ? !attributes.equals(logEntry.attributes) : logEntry.attributes != null) return false;
		if (cause != null ? !cause.equals(logEntry.cause) : logEntry.cause != null) return false;
		if (checksum != null ? !checksum.equals(logEntry.checksum) : logEntry.checksum != null) return false;
		if (date != null ? !date.equals(logEntry.date) : logEntry.date != null) return false;
		if (groupContainer != null ? !groupContainer.equals(logEntry.groupContainer) : logEntry.groupContainer != null)
			return false;
		if (message != null ? !message.equals(logEntry.message) : logEntry.message != null) return false;
		if (severity != null ? !severity.equals(logEntry.severity) : logEntry.severity != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = date != null ? date.hashCode() : 0;
		result = 31 * result + (message != null ? message.hashCode() : 0);
		result = 31 * result + (severity != null ? severity.hashCode() : 0);
		result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
		result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
		result = 31 * result + (cause != null ? cause.hashCode() : 0);
		result = 31 * result + (applicationContainer != null ? applicationContainer.hashCode() : 0);
		result = 31 * result + (groupContainer != null ? groupContainer.hashCode() : 0);
		return result;
	}

	@XmlType
	public final static class SeverityContainer {

		@XmlAttribute(name = "name")
		private final Severity severity;

		public SeverityContainer() {
			this(null);
		}

		public SeverityContainer(Severity severity) {
			this.severity = severity;
		}

		public Severity getSeverity() {
			return severity;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SeverityContainer that = (SeverityContainer) o;

			return severity == that.severity;

		}
	}

	@XmlType
	public static class ApplicationContainer {

		@XmlAttribute(name = "id")
		private final String applicationId;

		public ApplicationContainer() {
			this(null);
		}

		public ApplicationContainer(String applicationId) {
			this.applicationId = applicationId;
		}

		public String getApplicationId() {
			return applicationId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ApplicationContainer that = (ApplicationContainer) o;

			return !(applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null);

		}
	}

	@XmlType
	public static class GroupContainer {

		@XmlAttribute(name = "name")
		private final String group;

		public GroupContainer() {
			this(null);
		}

		public GroupContainer(String group) {
			this.group = group;
		}

		public String getGroup() {
			return group;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			GroupContainer that = (GroupContainer) o;

			return !(group != null ? !group.equals(that.group) : that.group != null);

		}
	}
}

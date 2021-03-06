package com.farpost.logwatcher;

import com.farpost.logwatcher.marshalling.JaxbAttributesMapAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "logEntry")
public class LogEntryImpl implements LogEntry {

	@XmlAttribute
	private final Date date;

	@XmlElement(name = "application")
	private ApplicationContainer applicationContainer;

	@XmlElement
	private final String message;

	@XmlElement(name = "severity")
	private final SeverityContainer severity;

	private volatile String checksum;

	@XmlJavaTypeAdapter(value = JaxbAttributesMapAdapter.class)
	private final Map<String, String> attributes;

	@XmlElement(name = "group")
	private GroupContainer groupContainer;

	@XmlElement
	private final Cause cause;

	/**
	 * Этот конструктор не предназначен для прямого ипользования. Нужен для корректной работы JAXB.
	 */
	@SuppressWarnings("unused")
	private LogEntryImpl() {
		this.date = null;
		this.groupContainer = null;
		this.message = null;
		this.severity = null;
		this.checksum = null;
		this.applicationContainer = null;
		this.cause = null;
		this.attributes = new HashMap<>();
	}

	public LogEntryImpl(Date date, String group, String message, Severity severity, String checksum,
											String applicationId, Map<String, String> attributes) {
		this(date, group, message, severity, checksum, applicationId, attributes, null);
	}

	public LogEntryImpl(Date date, String group, String message, Severity severity, String checksum,
											String applicationId, Map<String, String> attributes, Cause cause) {
		checkNotNull(date);
		checkNotNull(group);
		checkNotNull(severity);
		checkNotNull(applicationId);

		this.date = date;
		this.groupContainer = new GroupContainer(group);
		this.message = message == null ? "null" : message;
		this.severity = new SeverityContainer(severity);
		this.checksum = checksum;
		this.applicationContainer = new ApplicationContainer(applicationId);
		this.cause = cause;
		this.attributes = attributes == null
			? new HashMap<>()
			: new HashMap<>(attributes);
	}

	private static void checkNotNull(Object object) {
		if (object == null) throw new NullPointerException();
	}

	@Override
	public Date getDate() {
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

	@SuppressWarnings("UnusedDeclaration")
	public SeverityContainer getSeverityContainer() {
		return severity;
	}

	@SuppressWarnings("UnusedDeclaration")
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

	@SuppressWarnings("UnusedDeclaration")
	public ApplicationContainer getApplicationContainer() {
		return applicationContainer;
	}

	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public String getGroup() {
		return groupContainer.getGroup();
	}

	@SuppressWarnings("RedundantIfStatement")
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

		@SuppressWarnings("UnusedDeclaration")
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

		@Override
		public int hashCode() {
			return severity != null ? severity.hashCode() : 0;
		}
	}

	@XmlType
	public static class ApplicationContainer {

		@XmlAttribute(name = "id")
		private final String applicationId;

		@SuppressWarnings("UnusedDeclaration")
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

		@Override
		public int hashCode() {
			return applicationId != null ? applicationId.hashCode() : 0;
		}
	}

	@XmlType
	public static class GroupContainer {

		@XmlAttribute(name = "name")
		private final String group;

		@SuppressWarnings("UnusedDeclaration")
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

		@Override
		public int hashCode() {
			return group != null ? group.hashCode() : 0;
		}
	}
}

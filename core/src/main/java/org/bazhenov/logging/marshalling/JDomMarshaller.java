package org.bazhenov.logging.marshalling;

import com.farpost.timepoint.DateTime;
import org.bazhenov.logging.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.text.*;

public class JDomMarshaller implements Marshaller {

	private final SAXBuilder builder;
	private final Namespace namespace;
	private final XMLOutputter out;
	private final DateFormat format;

	public JDomMarshaller() {
		builder = new SAXBuilder();
		namespace = Namespace.getNamespace("http://logging.farpost.com/schema");
		out = new XMLOutputter(Format.getPrettyFormat());
		format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	}

	public String marshall(LogEntry entry) throws MarshallerException {
		Element root = element("logEntry");
		root.setAttribute("checksum", entry.getChecksum());
		String date = dateToString(entry);
		root.setAttribute("date", date);
		root.addContent(element("message", entry.getMessage()));
		root.addContent(element("application").setAttribute("id", entry.getApplicationId()));
		root.addContent(element("group").setAttribute("name", entry.getGroup()));
		root.addContent(element("severity").setAttribute("name", entry.getSeverity().toString()));

		Cause cause = entry.getCause();
		if ( cause != null ) {
			marshalCause(cause, root);
		}

		Document doc = new Document(root);

		return out.outputString(doc);
	}

	private String dateToString(LogEntry entry) {
		String date = format.format(entry.getDate().asDate());
		StringBuilder builder = new StringBuilder(date);
		builder.insert(22, ':');
		return builder.toString();
	}

	private void marshalCause(Cause cause, Element context) {
		Element node = element("cause");
		node.addContent(element("message", cause.getMessage()));
		node.addContent(element("stackTrace", cause.getStackTrace()));
		node.setAttribute("type", cause.getType());
		context.addContent(node);

		Cause nestedCause = cause.getCause();
		if ( nestedCause != null ) {
			marshalCause(nestedCause, node);
		}
	}

	private Element element(String name, String value) {
		return new Element(name, namespace).setText(value);
	}

	public LogEntry unmarshall(String data) throws MarshallerException {
		try {
			Document doc = builder.build(new StringReader(data));
			Element root = doc.getRootElement();
			String message = root.getChildText("message", namespace);
			String application = root.getChild("application", namespace).getAttributeValue("id");
			if(application == null){
				application = "default";
			}
			String checksum = root.getAttributeValue("checksum");
			DateTime dateTime = dateTime(root.getAttributeValue("date"));
			String group = root.getChild("group", namespace).getAttributeValue("name");
			String severityName = root.getChild("severity", namespace).getAttributeValue("name");
			Severity severity = Severity.valueOf(severityName);

			Element causeNode = root.getChild("cause", namespace);
			Cause cause = null;
			if ( causeNode != null ) {
				cause = parseCause(causeNode);
			}
			return new LogEntry(dateTime, group, message, severity, checksum, cause, application);
		} catch ( JDOMException e ) {
			throw new MarshallerException(e);
		} catch ( IOException e ) {
			throw new MarshallerException(e);
		} catch ( ParseException e ) {
			throw new RuntimeException(e);
		}
	}

	private Cause parseCause(Element node) {
		String type = node.getAttributeValue("type");
		String message = node.getChildText("message", namespace);
		String stackTrace = node.getChildText("stackTrace", namespace);
		Element nestedCauseElement = node.getChild("cause", namespace);
		Cause nestedCause = null;
		if ( nestedCauseElement != null ) {
			nestedCause = parseCause(nestedCauseElement);
		}
		return new Cause(type, message, stackTrace, nestedCause);
	}

	private DateTime dateTime(String value) throws ParseException {
		StringBuilder builder = new StringBuilder(value);
		builder.deleteCharAt(22);
		return new DateTime(format.parse(builder.toString()));
	}

	private Element element(String name) {
		return new Element(name, namespace);
	}
}

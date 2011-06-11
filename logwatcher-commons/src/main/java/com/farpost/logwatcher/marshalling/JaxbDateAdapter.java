package com.farpost.logwatcher.marshalling;

import com.farpost.timepoint.DateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

/**
 * JAXB Адаптер сериализующий тип Map<String, String> в XML дерево аттрибутов
 */
public class JaxbDateAdapter extends XmlAdapter<Date, DateTime> {

	@Override
	public DateTime unmarshal(Date date) throws Exception {
		return new DateTime(date);
	}

	@Override
	public Date marshal(DateTime date) throws Exception {
		return date.asDate();
	}
}

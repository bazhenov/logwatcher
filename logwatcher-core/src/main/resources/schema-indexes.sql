CREATE INDEX application_date_severity ON aggregated_entry(application_id, date, severity);

CREATE INDEX date ON entry(date);
CREATE INDEX checksum ON entry(checksum);

CREATE INDEX application_date_severity ON aggregated_entry(application_id, date, severity);

CREATE INDEX date ON entry(date);
CREATE INDEX time ON entry(time);
CREATE INDEX checksum ON entry(checksum);
CREATE INDEX date_checksum ON entry(date, checksum);
CREATE INDEX severity ON entry(severity);
CREATE INDEX application_id ON entry(application_id);

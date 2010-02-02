CREATE TABLE IF NOT EXISTS aggregated_entry (
	date DATE NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	last_time TIMESTAMP NOT NULL,
	category VARCHAR(255) NULL,
	severity INT NOT NULL,
	application_id VARCHAR(255) NOT NULL,
	count INT NOT NULL,
	content TEXT NOT NULL,
	PRIMARY KEY(date, checksum)
);
CREATE INDEX date_severity ON aggregated_entry(date, severity);

CREATE TABLE IF NOT EXISTS entry (
	id INT NOT NULL AUTO_INCREMENT,
	time TIMESTAMP NOT NULL,
	date DATE NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	category VARCHAR(255) NULL,
	severity INT NOT NULL,
	application_id VARCHAR(255) NOT NULL,
	content TEXT NOT NULL,
	PRIMARY KEY(id)
);

CREATE INDEX date ON entry(date);
CREATE INDEX time ON entry(time);
CREATE INDEX checksum ON entry(checksum);
CREATE INDEX date_checksum ON entry(date, checksum);
CREATE INDEX severity ON entry(severity);
CREATE INDEX application_id ON entry(application_id);

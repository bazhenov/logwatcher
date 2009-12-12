CREATE TABLE IF NOT EXISTS log_entry (
	id INT NOT NULL AUTO_INCREMENT,
	date DATE NOT NULL,
	checksum VARCHAR NOT NULL,
	category VARCHAR NULL,
	severity INT NOT NULL,
	application_id VARCHAR NOT NULL,
	text varchar NOT NULL,
	attributes varchar NOT NULL,
	count INT NOT NULL DEFAULT 1,
	last_date TIMESTAMP NOT NULL,
	PRIMARY KEY(id),
	UNIQUE KEY(date, checksum)
);

CREATE TABLE IF NOT EXISTS entry (
	id INT NOT NULL AUTO_INCREMENT,
	time TIMESTAMP NOT NULL,
	date DATE NOT NULL,
	checksum VARCHAR NOT NULL,
	category VARCHAR NULL,
	severity INT NOT NULL,
	application_id VARCHAR NOT NULL,
	content varchar NOT NULL,
	PRIMARY KEY(id)
);

CREATE INDEX date ON entry(date);
CREATE INDEX time ON entry(time);
CREATE INDEX checksum ON entry(checksum);
CREATE INDEX severity ON entry(severity);
CREATE INDEX application_id ON entry(application_id);

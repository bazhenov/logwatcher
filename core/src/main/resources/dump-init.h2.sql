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

CREATE INDEX IF NOT EXISTS date ON entry(date);
CREATE INDEX IF NOT EXISTS time ON entry(time);
CREATE INDEX IF NOT EXISTS checksum ON entry(checksum);
CREATE INDEX IF NOT EXISTS severity ON entry(severity);
CREATE INDEX IF NOT EXISTS application_id ON entry(application_id);

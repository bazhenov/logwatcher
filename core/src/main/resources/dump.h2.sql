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

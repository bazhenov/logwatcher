CREATE TABLE IF NOT EXISTS entry (
	id INT NOT NULL,
	value BLOB NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	date DATE NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS cluster_day_stat (
	application VARCHAR(255) NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	date DATE NOT NULL,
	count INTEGER NOT NULL,
	severity VARCHAR(255) NOT NULL,
	PRIMARY KEY(application, checksum, date)
);

CREATE TABLE IF NOT EXISTS cluster_general_stat (
	application VARCHAR(255) NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	last_seen DATETIME NOT NULL,
	first_seen DATETIME NOT NULL,
	minute_vector BLOB NULL,
	PRIMARY KEY(application, checksum)
);

CREATE TABLE IF NOT EXISTS cluster (
	application VARCHAR(255) NOT NULL,
	checksum VARCHAR(255) NOT NULL,
	cause_type VARCHAR(255) NULL,
	`group` VARCHAR(255) NULL,
	description TEXT NULL,
	severity VARCHAR(16) NOT NULL,
	issue_key VARCHAR(16) NULL,
	title TEXT NOT NULL,
	PRIMARY KEY(application, checksum)
);
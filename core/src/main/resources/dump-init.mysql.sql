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
CREATE INDEX severity ON entry(severity);
CREATE INDEX application_id ON entry(application_id);
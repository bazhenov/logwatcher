DROP TABLE IF EXISTS log_entry;

CREATE TABLE log_entry (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`date` DATE NOT NULL,
	`checksum` VARCHAR(128) NOT NULL,
	`category` VARCHAR(128) NULL,
	`severity` INT NOT NULL,
	`application_id` VARCHAR(128) NOT NULL,
	`text` TEXT NOT NULL,
	`count` INT(11) NOT NULL DEFAULT 1,
	`last_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	UNIQUE KEY(`date`, `checksum`)
) ENGINE=InnoDB;
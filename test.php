<?php

$socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP);
$packet = '<?xml version="1.0" encoding="utf-8"?>
<logEntry xmlns="http://logging.farpost.com/schema"
          checksum="3dfre"
          date="2009-03-22T12:33:12+10:00">
	<message>Log message</message>
	<group name="group" />
	<severity name="warning" />

	<cause type="RuntimeException">
		<message>Error occured</message>
		<stackTrace>Stacktrace</stackTrace>
		<cause type="InternalException">
			<message>Another message</message>
			<stackTrace>Another stacktrace</stackTrace>
		</cause>
	</cause>
</logEntry>';

echo socket_sendto($socket,  $packet, strlen($packet), 0, "127.0.0.1", 6578);
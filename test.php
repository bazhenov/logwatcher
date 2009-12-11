<?php

$socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP);
$packet = '<?xml version="1.0" encoding="utf8"?>                                                       
<logEntry xmlns="http://logging.farpost.com/schema/v1.1" date="2009-12-11T17:03:26+10:00" checksum="e85a67d17ede2673abda44c99c093a77"><message>Rolling back transaction with (1 resources)</message><application id="frontend"/><attributes><attribute name="machine">aux2.srv.loc</attribute><attribute name="group">slrTransaction</attribute></attributes><group name=""/><severity name="warning"/></logEntry>';
echo socket_sendto($socket,  $packet, strlen($packet), 0, "127.0.0.1", 6578);

package com.farpost.logging;

import org.apache.log4j.Logger;

public class AppenderTest {

	public static void main(String[] args) {
		Logger logger = Logger.getLogger(AppenderTest.class);
		logger.warn("Hello from main", new RuntimeException("Ihhaaa"));
	}
}

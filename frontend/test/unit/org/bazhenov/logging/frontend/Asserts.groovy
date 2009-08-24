package org.bazhenov.logging.frontend

public class Asserts {

	public static void assertContains(String needle, String stack) {
		assert stack?.indexOf(needle) >= 1, "Needle '${needle}' not found in stack"
	}
}
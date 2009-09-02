package org.bazhenov.logging.frontend

/**
 * Утилитарный класс предоставляющий матчеры для поиска подстроки в строке
 */
public class Asserts {

	public static void assertContains(def needle, String stack) {
		if ( needle instanceof List ) {
			for ( String n : needle ) {
				assertContains n, stack
			}
		}else{
			assert stack?.indexOf(needle) >= 0, "Needle '${needle}' not found in stack"
		}
	}

	public static void assertNotContains(def needle, String stack) {
		if ( needle instanceof List ) {
			for ( String n : needle ) {
				assertNotContains n, stack
			}
		}else{
			assert stack?.indexOf(needle) == -1, "Needle '${needle}' found in stack"
		}
	}
}

package org.bazhenov.logging.frontend

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
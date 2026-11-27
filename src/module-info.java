/**
 * 
 */
/**
 * 
 */
module LMSProject {
	requires transitive java.desktop;
	requires transitive java.sql;
	requires com.formdev.flatlaf;

	exports lms.object;
	exports lms.person;
	exports lms.screen;
	exports lms.screen.borrower;
	exports lms.screen.clerk;
	exports lms.screen.util;
}

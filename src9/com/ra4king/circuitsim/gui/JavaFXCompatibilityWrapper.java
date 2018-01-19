package com.ra4king.circuitsim.gui;

import javafx.application.Platform;
import javafx.scene.control.TableView;

/**
 * The Java 9 version of a class which provides compatiblity for both
 * JavaFX 8 and 9.
 *
 * A multi-platform jar will effectively replace
 * src/../JavaFXCompatibilityWrapper.java with this class on Java 9.
 */
class JavaFXCompatibilityWrapper {
	/**
	 * Start the JavaFX runtime and run runnable on the JavaFX
	 * Application thread.
	 *
	 * On Java 9, PlatformImpl is no longer available, so we have to use
	 * the new Platform.startup() function instead.
	 */
	static void platformStartup(Runnable runnable) {
	Platform.startup(runnable);
	}

	/**
	 * Stop users from rearranging columns on a TableView.
	 *
	 * On Java 8, this requires poking around with some internal APIs.
	 * On Java 9, as far as I can tell, you cannot.
	 */
	static <T> void tableDisableColumnReordering(TableView<T> tableView) {
		// Can't do this on Java 9, far as I can tell
	}
}

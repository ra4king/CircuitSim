package com.ra4king.circuitsim.gui;

import javafx.application.Platform;
import javafx.scene.control.TableView;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

/**
 * Provide compatiblity for both JavaFX 8 and 9. Some breaking API
 * changes from 8->9 make this required.
 *
 * A multi-platform jar will provide a Java 9 version of this class.
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
		PlatformImpl.startup(runnable);
	}

	/**
	 * Stop users from rearranging columns on a TableView.
	 *
	 * On Java 8, this requires poking around with some internal APIs.
	 * On Java 9, as far as I can tell, you cannot.
	 */
	static <T> void tableDisableColumnReordering(TableView<T> tableView) {
	tableView.widthProperty().addListener((source, oldWidth, newWidth) -> {
		TableHeaderRow header = (TableHeaderRow)tableView.lookup("TableHeaderRow");
		header.reorderingProperty().addListener(
			(observable, oldValue, newValue) -> header.setReordering(false));
		});
	}
}

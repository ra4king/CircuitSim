package com.ra4king.circuitsim.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

/**
 * @author Roi Atalla
 */
public class DebugUtil {
	private CircuitSim simulatorWindow;
	
	DebugUtil(CircuitSim simulatorWindow) {
		this.simulatorWindow = simulatorWindow;
	}
	
	public void logException(Throwable throwable) {
		logException("", throwable);
	}
	
	public void logException(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace();
		
		if(simulatorWindow.isWindowOpen()) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			throwable.printStackTrace(new PrintStream(stream));
			
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(simulatorWindow.getStage());
			alert.initModality(Modality.NONE);
			alert.setTitle("Internal error");
			alert.setHeaderText("Internal error: " + message);
			alert.getDialogPane().setContent(new TextArea(stream.toString()));
			alert.show();
			alert.setY(100);
			alert.setWidth(600);
			alert.setHeight(600);
		}
	}
}

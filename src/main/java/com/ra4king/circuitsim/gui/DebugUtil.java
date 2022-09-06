package com.ra4king.circuitsim.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

/**
 * @author Roi Atalla
 */
public class DebugUtil {
	private final CircuitSim simulatorWindow;
	private volatile boolean showingError;
	
	DebugUtil(CircuitSim simulatorWindow) {
		this.simulatorWindow = simulatorWindow;
	}
	
	public void logException(Throwable throwable) {
		logException("", throwable);
	}
	
	public void logException(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace();
		
		if (simulatorWindow.isWindowOpen()) {
			Platform.runLater(() -> {
				synchronized (DebugUtil.this) {
					if (showingError) {
						return;
					}
					showingError = true;
				}
				
				try {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					throwable.printStackTrace(new PrintStream(stream));
					String errorMessage = stream.toString();
					
					Alert alert = new Alert(AlertType.ERROR);
					alert.initOwner(simulatorWindow.getStage());
					alert.initModality(Modality.WINDOW_MODAL);
					alert.setTitle("Internal error");
					alert.setHeaderText("Internal error: " + message);
					TextArea textArea = new TextArea(errorMessage);
					textArea.setMinWidth(600);
					textArea.setMinHeight(400);
					alert.getDialogPane().setContent(textArea);
					
					alert.getButtonTypes().clear();
					alert.getButtonTypes().add(new ButtonType("Save and Exit", ButtonData.APPLY));
					alert.getButtonTypes().add(new ButtonType("Send Error Report", ButtonData.YES));
					alert.getButtonTypes().add(new ButtonType("Cancel", ButtonData.CANCEL_CLOSE));
					Optional<ButtonType> buttonType = alert.showAndWait();
					
					if (buttonType.isPresent()) {
						if (buttonType.get().getButtonData() == ButtonData.YES) {
							sendErrorReport(message + "\n" + errorMessage);
						} else if (buttonType.get().getButtonData() == ButtonData.APPLY) {
							try {
								simulatorWindow.saveCircuits();
							} catch (Exception exc) {
								exc.printStackTrace();
							}
							
							simulatorWindow.closeWindow();
						}
					}
				} finally {
					showingError = false;
				}
			});
		}
	}
	
	private static final String[] SYSTEM_PROPERTIES = {
		"java.version",
		"java.vendor",
		"java.vm.specification.version",
		"java.vm.specification.vendor",
		"java.vm.specification.name",
		"java.vm.version",
		"java.vm.vendor",
		"java.vm.name",
		"java.specification.version",
		"java.specification.vendor",
		"java.specification.name",
		"os.name",
		"os.arch",
		"os.version",
		};
	
	private void sendErrorReport(String message) {
		StringBuilder messageBuilder = new StringBuilder();
		for (String property : SYSTEM_PROPERTIES) {
			messageBuilder.append(property).append("=").append(System.getProperty(property)).append("\n");
		}
		
		messageBuilder.append("CircuitSim version=" + CircuitSim.VERSION).append("\n\n");
		
		String msg = messageBuilder.append(message.replace("\t", "    ").replace("\r", "")).toString();
		
		new Thread(() -> {
			try {
				HttpURLConnection
					httpConnection =
					(HttpURLConnection)new URL("https://www.roiatalla.com/circuitsimerror").openConnection();
				httpConnection.setRequestMethod("POST");
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				PrintWriter printWriter = new PrintWriter(httpConnection.getOutputStream());
				printWriter.write(msg);
				printWriter.flush();
				
				httpConnection.getInputStream().read();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}).start();
	}
}

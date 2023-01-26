package com.ra4king.circuitsim.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

public final class CircuitSimVersion implements Comparable<CircuitSimVersion> {
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(b?)");
	
	public static final CircuitSimVersion VERSION = new CircuitSimVersion("1.9.1");
	
	private final String version;
	private final int major;
	private final int minor;
	private final int bugfix;
	private final boolean beta;
	
	public CircuitSimVersion(String version) {
		this.version = version;
		
		Matcher matcher = VERSION_PATTERN.matcher(version);
		
		if (!matcher.find()) {
			throw new IllegalArgumentException("Invalid version string: " + version);
		}
		
		major = Integer.parseInt(matcher.group(1));
		minor = Integer.parseInt(matcher.group(2));
		bugfix = Integer.parseInt(matcher.group(3));
		beta = !matcher.group(4).isEmpty();
	}
	
	public String getVersion() {
		return version;
	}
	
	@Override
	public int compareTo(CircuitSimVersion o) {
		if (this.major > o.major) {
			return 1;
		} else if (this.major == o.major) {
			if (this.minor > o.minor) {
				return 1;
			} else if (this.minor == o.minor) {
				if (this.bugfix > o.bugfix) {
					return 1;
				} else if (this.bugfix == o.bugfix) {
					if (this.beta == o.beta) {
						return 0;
					} else if (o.beta) {
						return 1;
					}
				}
			}
		}
		
		return -1;
	}
	
	private static final AtomicBoolean checkingForUpdate = new AtomicBoolean(false);
	
	public static void checkForUpdate(CircuitSim circuitSim, boolean showOk) {
		if (!checkingForUpdate.compareAndSet(false, true)) {
			return;
		}
		
		Thread versionCheckThread = new Thread(() -> {
			try {
				URL url = new URL("https://www.roiatalla.com/public/CircuitSim/version.txt");
				
				String remoteVersion;
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
					remoteVersion = reader.readLine();
				} catch (IOException exc) {
					System.err.println("Error checking server for version.");
					exc.printStackTrace();
					
					if (showOk) {
						circuitSim.runFxSync(() -> {
							Alert alert = new Alert(AlertType.ERROR);
							alert.initOwner(circuitSim.getStage());
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.setTitle("Error");
							alert.setHeaderText("Error checking server");
							alert.setContentText("Error occurred when checking server for new update.\n" + exc);
							alert.show();
						});
					}
					
					return;
				}
				
				if (VERSION.compareTo(new CircuitSimVersion(remoteVersion)) < 0) {
					circuitSim.runFxSync(() -> {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.initOwner(circuitSim.getStage());
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("New Version Available");
						alert.setHeaderText("New version available: CircuitSim v" + remoteVersion);
						alert.setContentText("Click Update to open a browser to the download location.");
						alert.getButtonTypes().set(0, new ButtonType("Update", ButtonData.OK_DONE));
						alert.getButtonTypes().add(ButtonType.CANCEL);
						Optional<ButtonType> result = alert.showAndWait();
						if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
							circuitSim.getHostServices().showDocument("https://www.roiatalla.com/public/CircuitSim/");
						}
					});
				} else if (showOk) {
					circuitSim.runFxSync(() -> {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.initOwner(circuitSim.getStage());
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("No new updates");
						alert.setHeaderText("No new updates");
						alert.setContentText("You have the latest version and are good to go! :)");
						alert.show();
					});
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				checkingForUpdate.set(false);
			}
		});
		versionCheckThread.setDaemon(true);
		versionCheckThread.setName("Version Check Thread");
		versionCheckThread.start();
	}
}

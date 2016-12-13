package com.ra4king.circuitsimulator.gui;

import java.util.HashMap;

import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.components.Clock;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class CircuitSimulator extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	private Simulator simulator;
	
	private ComponentManager componentManager;
	
	private ToggleGroup buttonsToggleGroup;
	private ComboBox<Integer> bitSizeSelect, secondaryOptionSelect;
	
	private TabPane canvasTabPane;
	private HashMap<Tab, CircuitManager> circuitManagers;
	
	private String componentMode;
	
	@Override
	public void init() {
		simulator = new Simulator();
		circuitManagers = new HashMap<>();
		Clock.addChangeListener(value -> {
			getCurrentCircuit().runSim();
			getCurrentCircuit().repaint();
		});
	}
	
	private CircuitManager getCurrentCircuit() {
		return circuitManagers.get(canvasTabPane.getSelectionModel().getSelectedItem());
	}
	
	private void modifiedSelection() {
		CircuitManager current = getCurrentCircuit();
		if(current != null) {
			current.modifiedSelection(componentManager.getComponentCreator(componentMode, bitSizeSelect.getValue(), secondaryOptionSelect.getValue()));
			current.repaint();
		}
	}
	
	private ToggleButton setupButton(ToggleGroup group, int size, String componentName) {
		ToggleButton button = new ToggleButton(componentName);
		group.getToggles().add(button);
		button.setMinWidth(size);
		button.setMinHeight(2 * size / 3);
		button.addEventHandler(ActionEvent.ACTION, (e) -> {
			if(componentMode != componentName) {
				componentMode = componentName;
			} else {
				componentMode = null;
			}
			modifiedSelection();
		});
		return button;
	}
	
	@Override
	public void start(Stage stage) {
		final int buttonSize = 75;
		
		GridPane buttons = new GridPane();
		buttons.setAlignment(Pos.BASELINE_CENTER);
		
		buttonsToggleGroup = new ToggleGroup();
		componentManager = new ComponentManager();
		
		bitSizeSelect = new ComboBox<>();
		bitSizeSelect.setMinWidth(buttonSize);
		for(int i = 1; i <= 32; i++) {
			bitSizeSelect.getItems().add(i);
		}
		bitSizeSelect.setValue(1);
		bitSizeSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> modifiedSelection());
		
		secondaryOptionSelect = new ComboBox<>();
		secondaryOptionSelect.setMinWidth(buttonSize);
		for(int i = 1; i <= 8; i++) {
			secondaryOptionSelect.getItems().add(i);
		}
		secondaryOptionSelect.setValue(1);
		secondaryOptionSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> modifiedSelection());
		
		canvasTabPane = new TabPane();
		canvasTabPane.setMinWidth(800);
		canvasTabPane.setMinHeight(600);
		
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue != null) {
				CircuitManager oldManager = circuitManagers.get(oldValue);
				CircuitManager newManager = circuitManagers.get(newValue);
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
			}
			
			modifiedSelection();
		});
		
		HBox hBox = new HBox(buttons, canvasTabPane);
		Scene scene = new Scene(hBox);
		
		for(int i = 0; i < 3; i++) {
			Canvas canvas = new Canvas(800, 600) {
				{
					widthProperty().addListener(evt -> {
						CircuitManager manager = getCurrentCircuit();
						if(manager != null) {
							manager.repaint();
						}
					});
					heightProperty().addListener(evt -> {
						CircuitManager manager = getCurrentCircuit();
						if(manager != null) {
							manager.repaint();
						}
					});
				}
				
				@Override
				public boolean isResizable() {
					return true;
				}
				
				@Override
				public double prefWidth(double height) {
					return getWidth();
				}
				
				@Override
				public double prefHeight(double width) {
					return getHeight();
				}
			};
			
			canvas.widthProperty().bind(scene.widthProperty());
			canvas.heightProperty().bind(scene.heightProperty());
			
			canvas.addEventHandler(MouseEvent.ANY, e -> canvas.requestFocus());
			canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
			canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
			canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClicked);
			canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
			canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
			canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
			canvas.addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
			canvas.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
			canvas.addEventHandler(KeyEvent.KEY_TYPED, this::keyTyped);
			canvas.addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
			
			DraggableTab canvasTab = new DraggableTab("Circuit " + i, canvas);
			canvasTab.setDetachable(false);
			canvasTab.setClosable(false);
			
			CircuitManager circuitManager = new CircuitManager(canvas, simulator);
			circuitManagers.put(canvasTab, circuitManager);
			componentManager.addCircuit("Circuit " + i, circuitManager);
			
			canvasTabPane.getTabs().addAll(canvasTab);
		}
		
		int count = 0;
		for(String component : componentManager.getComponentNames()) {
			buttons.addRow(count++ / 2, setupButton(buttonsToggleGroup, buttonSize, component));
		}
		
		Label bitSizeLabel = new Label("Bit size:");
		Label secondaryLabel = new Label("Secondary:");
		GridPane.setHalignment(bitSizeLabel, HPos.CENTER);
		GridPane.setHalignment(secondaryLabel, HPos.CENTER);
		buttons.addRow(count++, bitSizeLabel, secondaryLabel);
		buttons.addRow(count, bitSizeSelect, secondaryOptionSelect);
		
		buttons.setMinWidth(150);
		buttons.setMinHeight(600);
		
		stage.setScene(scene);
		stage.setTitle("Circuit Simulator");
		stage.sizeToScene();
		stage.show();
		stage.centerOnScreen();
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case DIGIT0:
			case DIGIT1:
			case DIGIT2:
			case DIGIT3:
			case DIGIT4:
			case DIGIT5:
			case DIGIT6:
			case DIGIT7:
			case DIGIT8:
			case DIGIT9:
				int value = e.getText().charAt(0) - '0';
				if(value > 0) {
					Platform.runLater(() -> bitSizeSelect.setValue(value));
				}
				break;
			case NUMPAD0:
			case NUMPAD1:
			case NUMPAD2:
			case NUMPAD3:
			case NUMPAD4:
			case NUMPAD5:
			case NUMPAD6:
			case NUMPAD7:
			case NUMPAD8:
			case NUMPAD9:
				value = e.getText().charAt(0) - '0';
				Platform.runLater(() -> secondaryOptionSelect.setValue(value));
				break;
			case SPACE:
				if(Clock.isRunning()) {
					Clock.stopClock();
				} else {
					Clock.startClock(secondaryOptionSelect.getValue());
				}
				break;
			case ESCAPE:
				if(buttonsToggleGroup.getSelectedToggle() != null) {
					buttonsToggleGroup.getSelectedToggle().setSelected(false);
				}
				componentMode = null;
				modifiedSelection();
				break;
		}
		
		getCurrentCircuit().keyPressed(e);
	}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
	
	public void mouseClicked(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {
		getCurrentCircuit().mousePressed(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		getCurrentCircuit().mouseReleased(e);
	}
	
	public void mouseMoved(MouseEvent e) {
		getCurrentCircuit().mouseMoved(e);
	}
	
	public void mouseDragged(MouseEvent e) {
		getCurrentCircuit().mouseDragged(e);
	}
	
	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}
}

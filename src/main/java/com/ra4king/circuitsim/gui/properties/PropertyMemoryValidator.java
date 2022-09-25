package com.ra4king.circuitsim.gui.properties;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ra4king.circuitsim.gui.Properties.PropertyValidator;
import com.ra4king.circuitsim.gui.properties.PropertyMemoryValidator.MemoryLine;
import com.ra4king.circuitsim.simulator.SimulationException;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class PropertyMemoryValidator implements PropertyValidator<List<MemoryLine>> {
	private final int addressBits, dataBits;
	
	public PropertyMemoryValidator(int addressBits, int dataBits) {
		this.addressBits = addressBits;
		this.dataBits = dataBits;
	}
	
	public String parseValue(int value) {
		if (dataBits < 32) {
			value &= (1 << dataBits) - 1;
		}
		return String.format("%0" + (1 + (dataBits - 1) / 4) + "x", value);
	}
	
	public int parseValue(String value) {
		try {
			return Integer.parseUnsignedInt(value, 16);
		} catch (NumberFormatException exc) {
			throw new SimulationException("Cannot parse invalid hex value: " + value);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PropertyMemoryValidator) {
			PropertyMemoryValidator validator = (PropertyMemoryValidator)other;
			return validator.addressBits == this.addressBits && validator.dataBits == this.dataBits;
		}
		
		return false;
	}
	
	public List<MemoryLine> parse(int[] values, BiConsumer<Integer, Integer> memoryListener) {
		List<MemoryLine> lines = new ArrayList<>();
		
		int address = 0;
		MemoryLine currLine = null;
		for (int value : values) {
			if (currLine == null) {
				currLine = new MemoryLine(address);
			}
			
			SimpleStringProperty prop = new SimpleStringProperty(parseValue(value));
			
			if (memoryListener != null) {
				MemoryLine currMemoryLine = currLine;
				int currSize = currMemoryLine.values.size();
				prop.addListener((observable, oldValue, newValue) -> memoryListener.accept(currMemoryLine.address + currSize,
				                                                                           parseValue(newValue)));
			}
			
			currLine.values.add(prop);
			
			if (currLine.values.size() == 16) {
				lines.add(currLine);
				currLine = null;
				address += 16;
			}
		}
		
		while (address < (1 << addressBits)) {
			if (currLine == null) {
				currLine = new MemoryLine(address);
			}
			
			currLine.values.add(new SimpleStringProperty("0"));
			
			if (currLine.values.size() == 16) {
				lines.add(currLine);
				currLine = null;
				address += 16;
			}
		}
		
		if (currLine != null) {
			lines.add(currLine);
		}
		
		return lines;
	}
	
	@Override
	public List<MemoryLine> parse(String contents) {
		return parse(parsePartial(contents), null);
	}
	
	private int[] parsePartial(String contents) {
		int[] values = new int[1 << addressBits];
		
		Scanner scanner = new Scanner(contents);
		int length;
		for (length = 0; length < values.length && scanner.hasNext(); length++) {
			String piece = scanner.next();
			if (piece.matches("^\\d+-[\\da-fA-F]+$")) {
				String[] split = piece.split("-");
				int count = Integer.parseInt(split[0]);
				int val = parseValue(split[1]);
				for (int j = 0; j < count && length < values.length; j++, length++) {
					values[length] = val;
				}
				length--; // to account for extra increment
			} else {
				values[length] = parseValue(piece);
			}
		}
		return Arrays.copyOf(values, length);
	}
	
	@Override
	public String toString(List<MemoryLine> lines) {
		String values = lines.stream().map(MemoryLine::toString).collect(Collectors.joining(" "));
		
		// expensive I know, but whatever...
		String[] split = values.split(" ");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < split.length; ) {
			int count = 1;
			
			while ((i + count) < split.length && split[i].equals(split[i + count])) {
				count++;
			}
			
			if (count == 1) {
				builder.append(split[i]);
			} else {
				builder.append(count).append('-').append(split[i]);
			}
			
			i += count;
			
			if (i < split.length) {
				builder.append(' ');
			}
		}
		
		return builder.length() < values.length() ? builder.toString() : values;
	}
	
	@Override
	public Node createGui(Stage stage, List<MemoryLine> value, Consumer<List<MemoryLine>> onAction) {
		Button button = new Button("Click to edit");
		button.setOnAction(event -> {
			List<MemoryLine> lines = value == null ? parse(new int[0], null) : value;
			createAndShowMemoryWindow(stage, lines);
			onAction.accept(lines);
		});
		return button;
	}
	
	private void copyMemoryValues(List<MemoryLine> dest, List<MemoryLine> src) {
		for (int i = 0; i < src.size(); i++) {
			MemoryLine srcLine = src.get(i);
			MemoryLine tableLine = dest.get(i);
			
			for (int j = 0; j < srcLine.values.size() && j < tableLine.values.size(); j++) {
				tableLine.values.get(j).set(srcLine.values.get(j).get());
			}
		}
	}
	
	public void createAndShowMemoryWindow(Stage stage, List<MemoryLine> lines) {
		Stage memoryStage = new Stage();
		memoryStage.initOwner(stage);
		memoryStage.setTitle("Modify memory");
		
		TableView<MemoryLine> tableView = new TableView<>();
		tableView.getSelectionModel().setCellSelectionEnabled(true);
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tableView.setEditable(true);
		
		TableColumn<MemoryLine, String> address = new TableColumn<>("Address");
		address.setStyle("-fx-alignment: CENTER-RIGHT; -fx-background-color: lightgray;");
		address.setSortable(false);
		address.setEditable(false);
		address.setCellValueFactory(param -> new SimpleStringProperty(String.format("%0" + (1 + (addressBits - 1) / 4) + "x",
		                                                                            param.getValue().address)));
		tableView.getColumns().add(address);
		
		int columns = Math.min(1 << addressBits, 16);
		for (int i = 0; i < columns; i++) {
			int j = i;
			
			TableColumn<MemoryLine, String> column = new TableColumn<>(String.format("%x", i));
			column.setStyle("-fx-alignment: CENTER;");
			column.setSortable(false);
			column.setEditable(true);
			column.setCellValueFactory(param -> param.getValue().get(j));
			column.setCellFactory(c -> new TableCell<>() {
				private TextField textField;
				private String oldText;
				
				@Override
				public void startEdit() {
					oldText = getText();
					super.startEdit();
					setText(null);
					
					textField = new TextField(oldText);
					textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
						if (event.getCode() == KeyCode.ESCAPE) {
							textField.setText(oldText);
						}
						if (event.getCode() == KeyCode.ENTER) {
							cancelEdit();
						}
					});
					textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
						if (!newValue) {
							cancelEdit();
						}
					});
					
					setGraphic(textField);
					textField.selectAll();
					textField.requestFocus();
				}
				
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					
					if (item == null) {
						if (textField != null) {
							updateText(textField.getText());
						} else {
							setText(null);
						}
					} else {
						updateText(item);
						if (textField != null) {
							textField.setText(item);
						}
					}
					
					setGraphic(null);
					tableView.requestFocus();
				}
				
				@Override
				public void cancelEdit() {
					super.cancelEdit();
					if (textField != null) {
						updateText(textField.getText());
						textField = null;
						setGraphic(null);
						tableView.requestFocus();
					}
				}
				
				private void updateText(String newText) {
					String newValue;
					try {
						newValue = parseValue(parseValue(newText));
					} catch (SimulationException exc) {
						newValue = oldText;
					}
					
					setText(newValue);
					
					if (getTableRow() != null) {
						lines.get(getTableRow().getIndex()).values.get(j).set(newValue);
					}
				}
			});
			
			tableView.getColumns().add(column);
		}
		
		tableView.setItems(FXCollections.observableList(lines));
		
		Button loadButton = new Button("Load from file");
		loadButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose save file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
			File selectedFile = fileChooser.showOpenDialog(memoryStage);
			if (selectedFile != null) {
				try {
					String contents = new String(Files.readAllBytes(selectedFile.toPath()));
					copyMemoryValues(lines, parse(contents));
				} catch (Exception exc) {
					exc.printStackTrace();
					new Alert(AlertType.ERROR, "Could not open file: " + exc.getMessage()).showAndWait();
				}
			}
		});
		Button saveButton = new Button("Save to file");
		saveButton.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose save file");
			fileChooser.setInitialFileName("Memory.dat");
			File selectedFile = fileChooser.showSaveDialog(memoryStage);
			if (selectedFile != null) {
				List<String> strings = lines.stream().map(MemoryLine::toString).collect(Collectors.toList());
				try {
					Files.write(selectedFile.toPath(), strings);
				} catch (Exception exc) {
					exc.printStackTrace();
					new Alert(AlertType.ERROR, "Could not open file: " + exc.getMessage()).showAndWait();
				}
			}
		});
		Button clearButton = new Button("Clear contents");
		clearButton.setOnAction(event -> lines.forEach(line -> line.values.forEach(value -> value.set("0"))));
		
		memoryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
			if (keyEvent.isShortcutDown()) {
				if (keyEvent.getCode() == KeyCode.C) {
					ClipboardContent content = new ClipboardContent();
					
					StringBuilder ramContent = new StringBuilder();
					for (TablePosition<?, ?> selectedCell : tableView.getSelectionModel().getSelectedCells()) {
						if (selectedCell.getColumn() > 0) {
							ramContent
								.append(lines.get(selectedCell.getRow()).values.get(selectedCell.getColumn() - 1).get())
								.append(" ");
						}
					}
					
					content.putString(ramContent.toString());
					Clipboard.getSystemClipboard().setContent(content);
				} else if (keyEvent.getCode() == KeyCode.V) {
					String clipboard = Clipboard.getSystemClipboard().getString();
					if (clipboard != null) {
						try {
							ObservableList<TablePosition>
								selectedCells =
								tableView.getSelectionModel().getSelectedCells();
							
							int[] values = parsePartial(clipboard);
							
							if (selectedCells.size() <= 1) {
								TablePosition<?, ?>
									selectedCell =
									selectedCells.isEmpty() ? null : selectedCells.get(0);
								int row = selectedCell == null ? 0 : selectedCell.getRow();
								int col = selectedCell == null ? 0 : selectedCell.getColumn() - 1;
								
								if (col >= 0) {
									for (int value : values) {
										lines.get(row).get(col).set(parseValue(value));
										
										if (++col == lines.get(0).values.size()) {
											col = 0;
											row++;
											
											if (row == lines.size()) {
												break;
											}
										}
									}
								}
							} else {
								for (int i = 0; i < selectedCells.size() && i < values.length; i++) {
									TablePosition<?, ?> selectedCell = selectedCells.get(i);
									if (selectedCell.getColumn() > 0) {
										lines.get(selectedCell.getRow()).values
											.get(selectedCell.getColumn() - 1)
											.set(parseValue(values[i]));
									}
								}
							}
						} catch (Exception exc) {
							exc.printStackTrace();
							new Alert(AlertType.ERROR, "Invalid clipboard data: " + exc.getMessage()).showAndWait();
						}
					}
				}
			} else if (keyEvent.getCode() == KeyCode.DELETE || keyEvent.getCode() == KeyCode.BACK_SPACE) {
				for (TablePosition<?, ?> selectedCell : tableView.getSelectionModel().getSelectedCells()) {
					if (selectedCell.getColumn() > 0) {
						lines.get(selectedCell.getRow()).values.get(selectedCell.getColumn() - 1).set(parseValue(0));
					}
				}
			} else if (tableView.getEditingCell() == null &&
			           tableView.getSelectionModel().getSelectedCells().size() == 1 &&
			           (keyEvent.getCode().isLetterKey() || keyEvent.getCode().isDigitKey())) {
				@SuppressWarnings("unchecked")
				TablePosition<MemoryLine, String> selectedCell = tableView.getFocusModel().getFocusedCell();
				tableView.edit(selectedCell.getRow(), selectedCell.getTableColumn());
			}
		});
		
		VBox.setVgrow(tableView, Priority.ALWAYS);
		Platform.runLater(tableView::refresh);
		
		memoryStage.setScene(new Scene(new VBox(new HBox(loadButton, saveButton, clearButton), tableView)));
		memoryStage.sizeToScene();
		memoryStage.showAndWait();
	}
	
	public static class MemoryLine {
		public final int address;
		public final List<StringProperty> values;
		
		public MemoryLine(int address) {
			this.address = address;
			values = new ArrayList<>(16);
		}
		
		public StringProperty get(int index) {
			if (index < values.size()) {
				return values.get(index);
			}
			
			return new SimpleStringProperty("");
		}
		
		@Override
		public String toString() {
			return values.stream().map(StringProperty::get).collect(Collectors.joining(" "));
		}
	}
}

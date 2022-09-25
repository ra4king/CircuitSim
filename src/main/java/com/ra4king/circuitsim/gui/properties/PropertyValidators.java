package com.ra4king.circuitsim.gui.properties;

import java.util.Arrays;
import java.util.function.Consumer;

import com.ra4king.circuitsim.gui.Properties.PropertyValidator;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Common property validators.
 *
 * @author Roi Atalla
 */
public final class PropertyValidators {
	public static final PropertyValidator<IntegerString> INTEGER_VALIDATOR = IntegerString::new;
	public static final PropertyValidator<String> ANY_STRING_VALIDATOR = value -> value;
	public static final PropertyValidator<Boolean> YESNO_VALIDATOR = new PropertyListValidator<>(new Boolean[] {
		true, false
	}, bool -> bool ? "Yes" : "No");
	
	public static final PropertyListValidator<Boolean>
		LOCATION_VALIDATOR =
		new PropertyListValidator<>(Arrays.asList(true, false), bool -> bool ? "Left" + "/Top" : "Right" + "/Down");
	
	public static final PropertyValidator<Color> COLOR_VALIDATOR = new PropertyValidator<>() {
		@Override
		public Color parse(String value) {
			return Color.valueOf(value);
		}
		
		@Override
		public Node createGui(Stage stage, Color value, Consumer<Color> onAction) {
			ColorPicker picker = new ColorPicker(value);
			picker.setOnAction(event -> onAction.accept(picker.getValue()));
			return picker;
		}
	};
	
	private PropertyValidators() {}
}

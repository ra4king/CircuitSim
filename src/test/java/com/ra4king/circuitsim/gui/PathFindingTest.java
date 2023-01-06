package com.ra4king.circuitsim.gui;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.gui.LinkWires.Wire;
import com.ra4king.circuitsim.gui.PathFinding.LocationPreference;
import com.ra4king.circuitsim.gui.PathFinding.ValidWireLocation;

/**
 * @author Roi Atalla
 */
public class PathFindingTest {
	@Test
	public void pathfinding_minimumTurns() {
		ValidWireLocation validFn = (x, y, h) -> {
			if (x == 2) {
				return LocationPreference.INVALID;
			}
			if (x == 3 && y == 4) {
				return LocationPreference.INVALID;
			}
			if (x == 4 && y == 5) {
				return LocationPreference.INVALID;
			}
			return LocationPreference.VALID;
		};
		
		assertThat(PathFinding.bestPath(3, 0, 3, 6, validFn).getKey()).containsExactly(new Wire(null, 3, 6, 2, true),
		                                                                               new Wire(null, 3, 0, 2, true),
		                                                                               new Wire(null, 5, 0, 6, false));
	}
	
	@Test
	public void complexMaze() {
		ValidWireLocation validFn = (x, y, h) -> {
			if ((x == 1 && y <= 3) || (x == 2 && y == 5)) {
				return LocationPreference.INVALID;
			}
			if (y == 4 && x >= 3 && x <= 5) {
				return LocationPreference.INVALID;
			}
			if ((x == 4 && y == 3) || (x >= 5 && y == 2)) {
				return LocationPreference.INVALID;
			}
			if (x == 5 && y == 5) {
				return LocationPreference.INVALID;
			}
			return LocationPreference.VALID;
		};
		
		assertThat(PathFinding.bestPath(0, 0, 5, 3, validFn).getKey()).containsExactly(new Wire(null, 0, 0, 6, false),
		                                                                               new Wire(null, 0, 6, 6, true),
		                                                                               new Wire(null, 6, 3, 3, false),
		                                                                               new Wire(null, 5, 3, 1, true));
	}
}

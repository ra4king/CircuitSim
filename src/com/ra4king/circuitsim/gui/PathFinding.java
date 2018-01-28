package com.ra4king.circuitsim.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ra4king.circuitsim.gui.LinkWires.Wire;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class PathFinding {
	public enum LocationPreference {
		INVALID,
		VALID,
		PREFER
	}
	
	public interface ValidWireLocation {
		LocationPreference isValidWireLocation(int x, int y, boolean horizontal);
	}
	
	private static final Cost INFINITY = new Cost(Integer.MAX_VALUE, Integer.MAX_VALUE);
	
	public static Pair<Set<Wire>, Set<Point>> bestPath(int sx, int sy, int dx, int dy, ValidWireLocation valid) {
		if(dx < 0 || dy < 0) {
			return new Pair<>(Collections.emptySet(), Collections.emptySet());
		}
		
		Point source = new Point(sx, sy, null);
		Point destination = new Point(dx, dy, null);
		
		Set<Point> closedSet = new HashSet<>();
		Map<Point, Point> cameFrom = new HashMap<>();
		Map<Point, Cost> gScore = new HashMap<>();
		Map<Point, Integer> fScore = new HashMap<>();
		
		Set<Point> openSet = new HashSet<>();
		openSet.add(source);
		
		gScore.put(source, new Cost(0, 0));
		fScore.put(source, destination.estimateCost(source));
		
		int iterations = 0;
		
		while(!openSet.isEmpty()) {
			if(Thread.currentThread().isInterrupted()) {
				return new Pair<>(Collections.emptySet(), closedSet); 
			}
			
			iterations++;
			if(iterations == 5000) {
				System.err.println("Path finding taking too long, bail...");
				return new Pair<>(Collections.emptySet(), closedSet);
			}
			
			Point current = null;
			for(Point point : openSet) {
				if(current == null ||
						   fScore.getOrDefault(point, Integer.MAX_VALUE)
								   < fScore.getOrDefault(current, Integer.MAX_VALUE)) {
					current = point;
				}
			}
			
			if(current == null) {
				throw new IllegalStateException("Impossible");
			}
			
			openSet.remove(current);
			
			if(current.equalsIgnoreDirection(destination)) {
				return new Pair<>(constructPath(cameFrom, current), closedSet);
			}
			
			closedSet.add(current);
			
			for(Direction direction : Direction.values) {
				if(direction.isOpposite(current.direction)) {
					continue;
				}
				
				Point neighbor = direction.move(current);
				if(neighbor.x < 0 || neighbor.y < 0) {
					continue;
				}
				
				if(closedSet.contains(neighbor)) {
					continue;
				}
				
				LocationPreference preference =
						valid.isValidWireLocation(neighbor.x, neighbor.y, 
						                          direction == Direction.RIGHT || direction == Direction.LEFT);
				
				if(preference == LocationPreference.INVALID) {
					continue;
				}
				
				int additionalLength = preference == LocationPreference.PREFER ? 0 : 1;
				
				int additionalTurns = 0;
				if(current.direction != null && direction != current.direction) {
					if(neighbor.x == destination.x || neighbor.y == destination.y) {
						additionalTurns = 1;
					} else {
						additionalTurns = 2;
					}
				}
				
				Cost currentCost = gScore.get(current);
				Cost totalCost = new Cost(currentCost.length + additionalLength, currentCost.turns + additionalTurns);
				
				if(totalCost.compareTo(gScore.getOrDefault(neighbor, INFINITY)) >= 0) {
					continue;
				}
				
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, totalCost);
				
				int estimateCost = destination.estimateCost(neighbor) + totalCost.length + 50 * totalCost.turns;
				fScore.put(neighbor, estimateCost);
				
				openSet.add(neighbor);
			}
		}
		
		System.err.println("No possible paths found...");
		return null;
	}
	
	private static Set<Wire> constructPath(Map<Point, Point> cameFrom, Point current) {
		Set<Wire> totalPath = new HashSet<>();
		
		Point lastEndpoint = current;
		while(cameFrom.containsKey(current)) {
			Point next = cameFrom.get(current);
			
			if(!(lastEndpoint.x == current.x && current.x == next.x)
					   && !(lastEndpoint.y == current.y && current.y == next.y)) {
				int len = (current.x - lastEndpoint.x) + (current.y - lastEndpoint.y);
				totalPath.add(new Wire(null, lastEndpoint.x, lastEndpoint.y, len, lastEndpoint.y == current.y));
				lastEndpoint = current;
			}
			
			current = next;
		}
		
		int len = (current.x - lastEndpoint.x) + (current.y - lastEndpoint.y);
		if(len != 0) {
			totalPath.add(new Wire(null, lastEndpoint.x, lastEndpoint.y, len, lastEndpoint.y == current.y));
		}
		
		return totalPath;
	}
	
	private enum Direction {
		RIGHT {
			public Point move(Point point) {
				return new Point(point.x + 1, point.y, RIGHT);
			}
			
			public boolean isOpposite(Direction other) {
				return other == LEFT;
			}
		},
		LEFT {
			public Point move(Point point) {
				return new Point(point.x - 1, point.y, LEFT);
			}
			
			public boolean isOpposite(Direction other) {
				return other == RIGHT;
			}
		},
		DOWN {
			public Point move(Point point) {
				return new Point(point.x, point.y + 1, DOWN);
			}
			
			public boolean isOpposite(Direction other) {
				return other == UP;
			}
		},
		UP {
			public Point move(Point point) {
				return new Point(point.x, point.y - 1, UP);
			}
			
			public boolean isOpposite(Direction other) {
				return other == DOWN;
			}
		};
		
		public static final Direction[] values = values();
		
		public abstract Point move(Point point);
		
		public abstract boolean isOpposite(Direction other);
	}
	
	public static class Point {
		public final int x;
		public final int y;
		private final Direction direction;
		
		Point(int x, int y, Direction direction) {
			this.x = x;
			this.y = y;
			this.direction = direction;
		}
		
		public int estimateCost(Point other) {
			int dx = this.x - other.x;
			int dy = this.y - other.y;
			return dx * dx + dy * dy;
		}
		
		@Override
		public int hashCode() {
			return x + (y << 13) + (direction == null ? 0 : direction.hashCode() << 19);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Point) {
				Point element = (Point)other;
				return element.x == x && element.y == y && element.direction == direction;
			}
			
			return false;
		}
		
		public boolean equalsIgnoreDirection(Point point) {
			return point.x == x && point.y == y;
		}
		
		@Override
		public String toString() {
			return "Point(x = " + x + ", y = " + y + ", direction = " + direction + ")";
		}
	}
	
	private static class Cost implements Comparable<Cost> {
		private final int length;
		private final int turns;
		
		Cost(int length, int turns) {
			this.length = length;
			this.turns = turns;
		}
		
		@Override
		public int compareTo(Cost other) {
			int turns = this.turns - other.turns;
			if(turns != 0) {
				return turns;
			}
			
			return this.length - other.length;
		}
		
		@Override
		public int hashCode() {
			return length + (turns << 13);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Cost) {
				Cost element = (Cost)other;
				return element.length == length && element.turns == turns;
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return "Cost(length = " + length + ", turns = " + turns + ")";
		}
	}
}

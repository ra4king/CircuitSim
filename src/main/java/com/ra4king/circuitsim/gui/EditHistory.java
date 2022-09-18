package com.ra4king.circuitsim.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsim.gui.LinkWires.Wire;

import javafx.scene.control.Tab;

/**
 * @author Roi Atalla
 */
public class EditHistory {
	public enum EditAction {
		CREATE_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				manager.getSimulatorWindow().readdCircuit(manager, (Tab)params[0], (int)params[1]);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				manager.getSimulatorWindow().deleteCircuit(manager, true, false);
			}
		}, RENAME_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				((CircuitSim)params[0]).renameCircuit((Tab)params[1], (String)params[3]);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				((CircuitSim)params[0]).renameCircuit((Tab)params[1], (String)params[2]);
			}
		}, MOVE_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				@SuppressWarnings("unchecked")
				List<Tab> tabs = (List<Tab>)params[0];
				Tab tab = (Tab)params[1];
				int fromIdx = (int)params[2];
				int toIdx = (int)params[3];
				
				if (tabs.indexOf(tab) != fromIdx) {
					throw new IllegalStateException("Something bad happened!");
				}
				
				tabs.remove(fromIdx);
				tabs.add(toIdx, tab);
				
				manager.getSimulatorWindow().refreshCircuitsTab();
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				@SuppressWarnings("unchecked")
				List<Tab> tabs = (List<Tab>)params[0];
				Tab tab = (Tab)params[1];
				int fromIdx = (int)params[2];
				int toIdx = (int)params[3];
				
				// swap to/from idx
				redo(manager, new Object[] { tabs, tab, toIdx, fromIdx });
			}
		}, DELETE_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				CREATE_CIRCUIT.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				CREATE_CIRCUIT.redo(manager, params);
			}
		}, ADD_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager.getCircuitBoard().addComponent((ComponentPeer<?>)params[0]));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ComponentPeer<?> toRemove = (ComponentPeer<?>)params[0];
				
				for (ComponentPeer<?> component : manager.getCircuitBoard().getComponents()) {
					if (component == toRemove ||
					    (component.getClass() == toRemove.getClass() && component.getX() == toRemove.getX() &&
					     component.getY() == toRemove.getY() &&
					     component.getProperties().equals(toRemove.getProperties()))) {
						manager.mayThrow(() -> manager
							.getCircuitBoard()
							.removeElements(Collections.singleton(component)));
						break;
					}
				}
			}
		}, UPDATE_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager
					.getCircuitBoard()
					.updateComponent((ComponentPeer<?>)params[0], (ComponentPeer<?>)params[1]));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager
					.getCircuitBoard()
					.updateComponent((ComponentPeer<?>)params[1], (ComponentPeer<?>)params[0]));
			}
		}, MOVE_ELEMENT {
			protected void redo(CircuitManager manager, Object[] params) {
				GuiElement element = (GuiElement)params[0];
				int dx = (int)params[1];
				int dy = (int)params[2];
				element.setX(element.getX() + dx);
				element.setY(element.getY() + dy);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				GuiElement element = (GuiElement)params[0];
				int dx = (int)params[1];
				int dy = (int)params[2];
				element.setX(element.getX() - dx);
				element.setY(element.getY() - dy);
			}
		}, REMOVE_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				ADD_COMPONENT.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ADD_COMPONENT.redo(manager, params);
			}
		}, ADD_WIRE {
			protected void redo(CircuitManager manager, Object[] params) {
				Wire wire = (Wire)params[0];
				manager.mayThrow(() -> manager
					.getCircuitBoard()
					.addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal()));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager
					.getCircuitBoard()
					.removeElements(Collections.singleton((Wire)params[0])));
			}
		}, REMOVE_WIRE {
			protected void redo(CircuitManager manager, Object[] params) {
				ADD_WIRE.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ADD_WIRE.redo(manager, params);
			}
		}, CUSTOM_EDIT_ACTION {
			@Override
			protected void redo(CircuitManager manager, Object[] params) {
				Runnable redo = (Runnable)params[2];
				redo.run();
			}
			
			@Override
			protected void undo(CircuitManager manager, Object[] params) {
				Runnable undo = (Runnable)params[1];
				undo.run();
			}
		};
		
		protected abstract void redo(CircuitManager manager, Object[] params);
		
		protected abstract void undo(CircuitManager manager, Object[] params);
	}
	
	private static class Edit {
		private final EditAction action;
		private final CircuitManager circuitManager;
		private final Object[] params;
		
		Edit(EditAction action, CircuitManager circuitManager, Object[] params) {
			this.action = action;
			this.circuitManager = circuitManager;
			this.params = params;
		}
	}
	
	public interface EditListener {
		void edit(EditAction action, CircuitManager manager, Object[] params);
	}
	
	private static final int MAX_HISTORY = 300;
	
	private final CircuitSim circuitSim;
	private final Deque<List<Edit>> editStack;
	private final Deque<List<Edit>> redoStack;
	private final List<EditListener> editListeners;
	
	public EditHistory(CircuitSim circuitSim) {
		this.circuitSim = circuitSim;
		
		editStack = new ArrayDeque<>();
		redoStack = new ArrayDeque<>();
		
		editListeners = new ArrayList<>();
	}
	
	public void clear() {
		editStack.clear();
		redoStack.clear();
	}
	
	private int disableDepth = 0;
	
	public void enable() {
		disableDepth--;
		
		if (disableDepth < 0) {
			throw new IllegalStateException("This should never happen!");
		}
	}
	
	public void disable() {
		disableDepth++;
	}
	
	public void addListener(EditListener listener) {
		editListeners.add(listener);
	}
	
	public void removeListener(EditListener listener) {
		editListeners.remove(listener);
	}
	
	private int groupDepth = 0;
	private List<List<Edit>> groups;
	
	public void beginGroup() {
		groupDepth++;
		
		if (groups == null) {
			if (groupDepth != 1) {
				throw new IllegalStateException("How the hell did this happen??");
			}
			
			groups = new ArrayList<>();
		}
		
		groups.add(new ArrayList<>());
	}
	
	public void endGroup() {
		if (groupDepth == 0) {
			throw new IllegalStateException("Mismatched call to endGroup.");
		}
		
		groupDepth--;
		
		if (groupDepth == 0) {
			if (groups == null) {
				throw new IllegalStateException("This can't be null?!");
			}
			if (groups.size() != 1) {
				throw new IllegalStateException("There should only be a single group left");
			}
			
			List<Edit> edits = groups.get(0);
			if (!edits.isEmpty()) {
				editStack.push(edits);
				if (editStack.size() > MAX_HISTORY) {
					editStack.removeLast();
				}
			}
			
			groups = null;
		} else {
			groups.get(groupDepth - 1).addAll(groups.get(groupDepth));
			groups.remove(groupDepth);
		}
	}
	
	public void clearGroup() {
		if (groups == null) {
			throw new IllegalStateException("No group started");
		}
		
		groups.get(groupDepth - 1).clear();
		groups.subList(groupDepth, groups.size()).clear();
	}
	
	public void addAction(EditAction action, CircuitManager manager, Object... params) {
		if (disableDepth == 0) {
			beginGroup();
			groups.get(groupDepth - 1).add(new Edit(action, manager, params));
			endGroup();
			
			redoStack.clear();
			
			editListeners.forEach(listener -> listener.edit(action, manager, params));
		}
	}
	
	public void addCustomAction(CircuitManager manager, String actionName, Runnable undo, Runnable redo) {
		addAction(EditAction.CUSTOM_EDIT_ACTION, manager, actionName, undo, redo);
	}
	
	public int editStackSize() {
		return editStack.size() + (groups == null || groups.isEmpty() ? 0 : 1);
	}
	
	public int redoStackSize() {
		return redoStack.size();
	}
	
	public CircuitManager undo() {
		if (editStack.isEmpty()) {
			return null;
		}
		
		List<Edit> popped = editStack.pop();
		redoStack.push(popped);
		
		circuitSim.getSimulator().runSync(() -> {
			Set<CircuitManager> circuitManagers = new HashSet<>();
			
			try {
				disable();
				
				for (int i = popped.size() - 1; i >= 0; i--) {
					Edit edit = popped.get(i);
					
					circuitManagers.add(edit.circuitManager);
					edit.circuitManager.getCircuitBoard().disableRejoinWires();
					
					edit.action.undo(edit.circuitManager, edit.params);
					editListeners.forEach(listener -> listener.edit(edit.action, edit.circuitManager, edit.params));
				}
			} finally {
				enable();
				circuitManagers.forEach(circuitManager -> circuitManager.getCircuitBoard().enableRejoinWires());
			}
		});
		
		return popped.get(0).circuitManager;
	}
	
	public CircuitManager redo() {
		if (redoStack.isEmpty()) {
			return null;
		}
		
		List<Edit> popped = redoStack.pop();
		editStack.push(popped);
		if (editStack.size() > MAX_HISTORY) {
			editStack.removeLast();
		}
		
		circuitSim.getSimulator().runSync(() -> {
			Set<CircuitManager> circuitManagers = new HashSet<>();
			
			try {
				disable();
				
				for (Edit edit : popped) {
					circuitManagers.add(edit.circuitManager);
					edit.circuitManager.getCircuitBoard().disableRejoinWires();
					
					edit.action.redo(edit.circuitManager, edit.params);
					editListeners.forEach(listener -> listener.edit(edit.action, edit.circuitManager, edit.params));
				}
			} finally {
				enable();
				circuitManagers.forEach(circuitManager -> circuitManager.getCircuitBoard().enableRejoinWires());
			}
		});
		
		return popped.get(0).circuitManager;
	}
}

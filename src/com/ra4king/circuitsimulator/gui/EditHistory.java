package com.ra4king.circuitsimulator.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.gui.LinkWires.Wire;

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
				manager.getSimulatorWindow().deleteCircuit(manager, true);
			}
		},
		RENAME_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				((CircuitSimulator)params[0]).renameCircuit((Tab)params[1], (String)params[3]);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				((CircuitSimulator)params[0]).renameCircuit((Tab)params[1], (String)params[2]);
			}
		},
		DELETE_CIRCUIT {
			protected void redo(CircuitManager manager, Object[] params) {
				CREATE_CIRCUIT.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				CREATE_CIRCUIT.redo(manager, params);
			}
		},
		ADD_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager.getCircuitBoard().addComponent((ComponentPeer<?>)params[0]));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ComponentPeer<?> toRemove = (ComponentPeer<?>)params[0];
				
				for(ComponentPeer<?> component : manager.getCircuitBoard().getComponents()) {
					if(component == toRemove ||
							   (component.getClass() == toRemove.getClass()
									    && component.getX() == toRemove.getX()
									    && component.getY() == toRemove.getY()
									    && component.getProperties().equals(toRemove.getProperties()))) {
						manager.mayThrow(() -> manager.getCircuitBoard()
						                              .removeElements(Collections.singleton(component)));
						break;
					}
				}
			}
		},
		UPDATE_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager.getCircuitBoard().updateComponent((ComponentPeer<?>)params[0],
				                                                                 (ComponentPeer<?>)params[1]));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager.getCircuitBoard().updateComponent((ComponentPeer<?>)params[1],
				                                                                 (ComponentPeer<?>)params[0]));
			}
		},
		MOVE_ELEMENTS {
			protected void redo(CircuitManager manager, Object[] params) {
				@SuppressWarnings("unchecked")
				Set<GuiElement> elements = (Set<GuiElement>)params[0];
				manager.mayThrow(() -> manager.getCircuitBoard().initMove(elements));
				manager.getCircuitBoard().moveElements((int)params[1], (int)params[2]);
				manager.mayThrow(() -> manager.getCircuitBoard().finalizeMove());
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				@SuppressWarnings("unchecked")
				Set<GuiElement> elements = (Set<GuiElement>)params[0];
				manager.mayThrow(() -> manager.getCircuitBoard().initMove(elements));
				manager.getCircuitBoard().moveElements(-(int)params[1], -(int)params[2]);
				manager.mayThrow(() -> manager.getCircuitBoard().finalizeMove());
			}
		},
		REMOVE_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				ADD_COMPONENT.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ADD_COMPONENT.redo(manager, params);
			}
		},
		ADD_WIRE {
			protected void redo(CircuitManager manager, Object[] params) {
				Wire wire = (Wire)params[0];
				manager.mayThrow(() -> manager.getCircuitBoard()
				                              .addWire(wire.getX(), wire.getY(), wire.getLength(),
				                                       wire.isHorizontal()));
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				manager.mayThrow(() -> manager.getCircuitBoard()
				                              .removeElements(Collections.singleton((Wire)params[0])));
			}
		},
		REMOVE_WIRE {
			protected void redo(CircuitManager manager, Object[] params) {
				ADD_WIRE.undo(manager, params);
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				ADD_WIRE.redo(manager, params);
			}
		};
		
		protected abstract void redo(CircuitManager manager, Object[] params);
		
		protected abstract void undo(CircuitManager manager, Object[] params);
	}
	
	private class Edit {
		EditAction action;
		
		CircuitManager circuitManager;
		Object[] params;
		
		Edit(EditAction action, CircuitManager circuitManager, Object[] params) {
			this.action = action;
			this.circuitManager = circuitManager;
			this.params = params;
		}
	}
	
	public interface EditListener {
		void edit(EditAction action, CircuitManager manager, Object[] params);
	}
	
	private Deque<List<Edit>> editStack;
	private Deque<List<Edit>> redoStack;
	
	private static final int MAX_HISTORY = 300;
	
	private List<EditListener> editListeners;
	
	public EditHistory() {
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
		
		if(disableDepth < 0) {
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
	private List<Edit> currentGroup;
	
	public void beginGroup() {
		groupDepth++;
		
		if(currentGroup == null) {
			if(groupDepth != 1) throw new IllegalStateException("How the hell did this happen??");
			
			currentGroup = new ArrayList<>();
		}
	}
	
	public void endGroup() {
		if(groupDepth == 0) throw new IllegalStateException("Mismatched call to endGroup.");
		
		groupDepth--;
		
		if(groupDepth == 0) {
			if(currentGroup == null) throw new IllegalStateException("This can't be null?!");
			
			if(!currentGroup.isEmpty()) {
				editStack.push(currentGroup);
				if(editStack.size() > MAX_HISTORY) {
					editStack.removeLast();
				}
			}
			
			currentGroup = null;
		}
	}
	
	public void addAction(EditAction action, CircuitManager manager, Object... params) {
		if(disableDepth == 0) {
			beginGroup();
			currentGroup.add(new Edit(action, manager, params));
			endGroup();
			
			redoStack.clear();
			
			editListeners.forEach(listener -> listener.edit(action, manager, params));
		}
	}
	
	public int editStackSize() {
		return editStack.size() + (currentGroup == null || currentGroup.isEmpty() ? 0 : 1);
	}
	
	public int redoStackSize() {
		return redoStack.size();
	}
	
	public CircuitManager undo() {
		if(editStack.isEmpty()) {
			return null;
		}
		
		disable();
		
		List<Edit> popped = editStack.pop();
		redoStack.push(popped);
		
		for(int i = popped.size() - 1; i >= 0; i--) {
			Edit edit = popped.get(i);
			
			edit.action.undo(edit.circuitManager, edit.params);
			editListeners.forEach(listener -> listener.edit(edit.action, edit.circuitManager, edit.params));
		}
		
		enable();
		
		return popped.get(0).circuitManager;
	}
	
	public CircuitManager redo() {
		if(redoStack.isEmpty()) {
			return null;
		}
		
		disable();
		
		List<Edit> popped = redoStack.pop();
		editStack.push(popped);
		if(editStack.size() > MAX_HISTORY) {
			editStack.removeLast();
		}
		
		for(Edit edit : popped) {
			edit.action.redo(edit.circuitManager, edit.params);
			editListeners.forEach(listener -> listener.edit(edit.action, edit.circuitManager, edit.params));
		}
		
		enable();
		
		return popped.get(0).circuitManager;
	}
}

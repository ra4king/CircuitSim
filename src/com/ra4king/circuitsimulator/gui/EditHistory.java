package com.ra4king.circuitsimulator.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.gui.LinkWires.Wire;

/**
 * @author Roi Atalla
 */
public class EditHistory {
	public enum EditAction {
		ADD_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().addComponent((ComponentPeer<?>)params[0]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					ComponentPeer<?> toRemove = (ComponentPeer<?>)params[0];
					
					for(ComponentPeer<?> component : manager.getCircuitBoard().getComponents()) {
						if(component == toRemove ||
								   (component.getClass() == toRemove.getClass()
										    && component.getX() == toRemove.getX()
										    && component.getY() == toRemove.getY()
										    && component.getProperties().equals(toRemove.getProperties()))) {
							manager.getCircuitBoard().removeElements(Collections.singleton(component));
						}
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		UPDATE_COMPONENT {
			protected void redo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().updateComponent((ComponentPeer<?>)params[1],
					                                          (ComponentPeer<?>)params[0]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().updateComponent((ComponentPeer<?>)params[0],
					                                          (ComponentPeer<?>)params[1]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		MOVE_ELEMENTS {
			protected void redo(CircuitManager manager, Object[] params) {
				try {
					@SuppressWarnings("unchecked")
					Set<GuiElement> elements = (Set<GuiElement>)params[0];
					manager.getCircuitBoard().initMove(elements);
					manager.getCircuitBoard().moveElements((int)params[1], (int)params[2]);
					manager.getCircuitBoard().finalizeMove();
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					@SuppressWarnings("unchecked")
					Set<GuiElement> elements = (Set<GuiElement>)params[0];
					manager.getCircuitBoard().initMove(elements);
					manager.getCircuitBoard().moveElements(-(int)params[1], -(int)params[2]);
					manager.getCircuitBoard().finalizeMove();
				} catch(Exception exc) {
					exc.printStackTrace();
				}
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
				try {
					Wire wire = (Wire)params[0];
					manager.getCircuitBoard().addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().removeElements(Collections.singleton((Wire)params[0]));
				} catch(Exception exc) {
					exc.printStackTrace();
				}
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
		boolean withLast;
		Object[] params;
		
		Edit(EditAction action, CircuitManager circuitManager, boolean withLast, Object[] params) {
			this.action = action;
			this.circuitManager = circuitManager;
			this.withLast = withLast;
			this.params = params;
		}
	}
	
	public interface EditListener {
		void edit(EditAction action, CircuitManager manager, Object[] params);
	}
	
	private Deque<List<Edit>> editStack;
	private Deque<List<Edit>> redoStack;
	
	private List<EditListener> editListeners;
	
	public EditHistory() {
		editStack = new ArrayDeque<>();
		redoStack = new ArrayDeque<>();
		
		editListeners = new ArrayList<>();
	}
	
	public void clear() {
		editStack.clear();
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
		if(groupDepth == 0) throw new IllegalStateException("What is going on?!?!?!?!");
		
		groupDepth--;
		
		if(groupDepth == 0) {
			if(currentGroup == null) throw new IllegalStateException("This can't be null?!");
			
			if(!currentGroup.isEmpty()) {
				editStack.push(currentGroup);
			}
			
			currentGroup = null;
		}
	}
	
	public void addAction(EditAction action, CircuitManager manager, Object... params) {
		if(disableDepth == 0) {
			beginGroup();
			currentGroup.add(new Edit(action, manager, groupDepth > 0, params));
			endGroup();
			
			redoStack.clear();
			
			editListeners.forEach(listener -> listener.edit(action, manager, params));
		}
	}
	
	public int editStackSize() {
		return editStack.size();
	}
	
	public CircuitManager undo() {
		if(editStack.isEmpty()) {
			return null;
		}
		
		disable();
		
		List<Edit> popped = editStack.pop();
		redoStack.push(popped);
		
		for(Edit edit : popped) {
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
		
		for(int i = popped.size() - 1; i >= 0; i--) {
			Edit edit = popped.get(i);
			
			edit.action.redo(edit.circuitManager, edit.params);
			editListeners.forEach(listener -> listener.edit(edit.action, edit.circuitManager, edit.params));
		}
		
		enable();
		
		return popped.get(0).circuitManager;
	}
}

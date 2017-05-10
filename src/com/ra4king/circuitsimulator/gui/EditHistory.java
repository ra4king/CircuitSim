package com.ra4king.circuitsimulator.gui;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import com.ra4king.circuitsimulator.gui.LinkWires.Wire;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class EditHistory {
	public enum EditAction {
		ADD_COMPONENT {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().removeElements(Collections.singleton((ComponentPeer<?>)params[0]));
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		SET_PROPERTIES {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().updateComponent((ComponentPeer<?>)params[0], (ComponentPeer<?>)
							                                                                       params[1]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		MOVE_COMPONENT {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().initMove(Collections.singleton((ComponentPeer<?>)params[0]));
					manager.getCircuitBoard().moveElements((int)params[1], (int)params[2]);
					manager.getCircuitBoard().finalizeMove();
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		REMOVE_COMPONENT {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().addComponent((ComponentPeer<?>)params[0]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		ADD_WIRE {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().removeElements(Collections.singleton((Wire)params[0]));
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		REMOVE_WIRE {
			protected void undo(CircuitManager manager, Object[] params) {
				try {
					manager.getCircuitBoard().addWire((int)params[0], (int)params[1], (int)params[2],
					                                  (boolean)params[3]);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		},
		REMOVE_ELEMENTS {
			protected void undo(CircuitManager manager, Object[] params) {
				for(Object o : params) {
					try {
						if(o instanceof ComponentPeer<?>) {
							manager.getCircuitBoard().addComponent((ComponentPeer<?>)o);
						} else {
							Wire w = (Wire)o;
							manager.getCircuitBoard().addWire(w.getX(), w.getY(), w.getLength(), w.isHorizontal());
						}
					} catch(Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		};
		
		protected abstract void undo(CircuitManager manager, Object[] params);
	}
	
	private class Edit {
		CircuitManager circuitManager;
		boolean withLast;
		Object[] params;
		
		Edit(CircuitManager circuitManager, boolean withLast, Object[] params) {
			this.circuitManager = circuitManager;
			this.withLast = withLast;
			this.params = params;
		}
	}
	
	private Deque<Pair<EditAction, Edit>> editStack;
	
	public EditHistory() {
		editStack = new ArrayDeque<>();
	}
	
	private boolean enabled = true;
	
	private void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public void addAction(EditAction action, CircuitManager manager, boolean withLast, Object... params) {
		if(enabled) {
			editStack.push(new Pair<>(action, new Edit(manager, withLast, params)));
		}
	}
	
	public int editStackSize() {
		return editStack.size();
	}
	
	public void undo() {
		disable();
		do {
			Pair<EditAction, Edit> popped = editStack.pop();
			popped.getKey().undo(popped.getValue().circuitManager, popped.getValue().params);
		} while(editStack.peek() != null && editStack.peek().getValue().withLast);
		enable();
	}
}

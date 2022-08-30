package com.ra4king.circuitsim.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.ra4king.circuitsim.gui.peers.arithmetic.AdderPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.BitExtenderPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.ComparatorPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.DividerPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.MultiplierPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.NegatorPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.RandomGeneratorPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.ShifterPeer;
import com.ra4king.circuitsim.gui.peers.arithmetic.SubtractorPeer;
import com.ra4king.circuitsim.gui.peers.gates.AndGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.ControlledBufferPeer;
import com.ra4king.circuitsim.gui.peers.gates.NandGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.NorGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.NotGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.OrGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.XnorGatePeer;
import com.ra4king.circuitsim.gui.peers.gates.XorGatePeer;
import com.ra4king.circuitsim.gui.peers.io.Button;
import com.ra4king.circuitsim.gui.peers.io.HexDisplay;
import com.ra4king.circuitsim.gui.peers.io.LED;
import com.ra4king.circuitsim.gui.peers.io.LEDMatrix;
import com.ra4king.circuitsim.gui.peers.memory.DFlipFlopPeer;
import com.ra4king.circuitsim.gui.peers.memory.RAMPeer;
import com.ra4king.circuitsim.gui.peers.memory.ROMPeer;
import com.ra4king.circuitsim.gui.peers.memory.RegisterPeer;
import com.ra4king.circuitsim.gui.peers.memory.SRFlipFlopPeer;
import com.ra4king.circuitsim.gui.peers.misc.Text;
import com.ra4king.circuitsim.gui.peers.plexers.DecoderPeer;
import com.ra4king.circuitsim.gui.peers.plexers.DemultiplexerPeer;
import com.ra4king.circuitsim.gui.peers.plexers.MultiplexerPeer;
import com.ra4king.circuitsim.gui.peers.plexers.PriorityEncoderPeer;
import com.ra4king.circuitsim.gui.peers.wiring.ClockPeer;
import com.ra4king.circuitsim.gui.peers.wiring.ConstantPeer;
import com.ra4king.circuitsim.gui.peers.wiring.PinPeer;
import com.ra4king.circuitsim.gui.peers.wiring.Probe;
import com.ra4king.circuitsim.gui.peers.wiring.SplitterPeer;
import com.ra4king.circuitsim.gui.peers.wiring.SimpleTransistorPeer;
import com.ra4king.circuitsim.gui.peers.wiring.TransistorPeer;
import com.ra4king.circuitsim.gui.peers.wiring.Tunnel;
import com.ra4king.circuitsim.simulator.SimulationException;

import javafx.scene.image.Image;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ComponentManager {
	private List<ComponentLauncherInfo> components;
	
	public static class ComponentLauncherInfo {
		public final Class<? extends ComponentPeer<?>> clazz;
		public final Pair<String, String> name;
		public final Image image;
		public final Properties properties;
		public final boolean showInComponentsList;
		public final ComponentCreator<?> creator;
		
		ComponentLauncherInfo(Class<? extends ComponentPeer<?>> clazz,
		                      Pair<String, String> name,
		                      Image image,
		                      Properties properties,
		                      boolean showInComponentsList,
		                      ComponentCreator<?> creator) {
			this.clazz = clazz;
			this.name = name;
			this.image = image;
			this.properties = properties;
			this.showInComponentsList = showInComponentsList;
			this.creator = creator;
		}
		
		@Override
		public int hashCode() {
			return clazz.hashCode()
				       ^ name.hashCode()
				       ^ (image == null ? 0 : image.hashCode())
				       ^ properties.hashCode()
				       ^ creator.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof ComponentLauncherInfo)) {
				return false;
			}
			
			ComponentLauncherInfo info = (ComponentLauncherInfo)other;
			return info.clazz == this.clazz && info.name.equals(this.name);
		}
	}
	
	public interface ComponentManagerInterface {
		void addComponent(Pair<String, String> name, Image image, Properties defaultProperties, boolean showInComponentsList);

		default void addComponent(Pair<String, String> name, Image image, Properties defaultProperties) {
			addComponent(name, image, defaultProperties, true);
		}
	}
	
	static <T extends ComponentPeer<?>> ComponentCreator<T> forClass(Class<T> clazz) {
		return (properties, x, y) -> {
			try {
				return clazz.getConstructor(Properties.class, Integer.TYPE, Integer.TYPE)
				            .newInstance(properties, x, y);
			} catch(NoSuchMethodException exc) {
				throw new RuntimeException("Must have constructor taking (Properties props, int x, int y)");
			} catch(InvocationTargetException exc) {
				if(exc.getTargetException() instanceof SimulationException) {
					throw (SimulationException)exc.getTargetException();
				}
				
				throw new RuntimeException(exc.getTargetException());
			} catch(RuntimeException exc) {
				throw exc;
			} catch(Exception exc) {
				throw new RuntimeException(exc);
			}
		};
	}
	
	ComponentManager() {
		components = new ArrayList<>();
		registerDefaultComponents();
	}
	
	public ComponentLauncherInfo get(Pair<String, String> name) {
		for(ComponentLauncherInfo component : components) {
			if(component.name.equals(name)) {
				return component;
			}
		}
		
		throw new IllegalArgumentException("Component not registered: " + name);
	}
	
	@SuppressWarnings("rawtypes") // To be able to use getClass(), ComponentPeer cannot be parameterized.
	public ComponentLauncherInfo get(Class<? extends ComponentPeer> clazz, Properties properties)
		throws SimulationException {
		ComponentLauncherInfo firstComponent = null;
		
		for(ComponentLauncherInfo component : components) {
			if(component.clazz == clazz) {
				firstComponent = component;
				
				if(properties.intersect(component.properties).equals(component.properties)) {
					return component;
				}
			}
		}
		
		if(firstComponent != null) {
			return firstComponent;
		}
		
		throw new SimulationException("Component not registered: " + clazz);
	}
	
	public void forEach(Consumer<ComponentLauncherInfo> consumer) {
		components.forEach(consumer);
	}
	
	public <T extends ComponentPeer<?>> void register(Class<T> clazz) {
		try {
			ComponentCreator<?> creator = forClass(clazz);
			
			Method method = clazz.getMethod("installComponent", ComponentManagerInterface.class);
			method.invoke(null,
			              (ComponentManagerInterface)(name, image, defaultProperties, showInComponentsList) -> {
				              if(name == null || defaultProperties == null) {
					              throw new NullPointerException("Name and Properties cannot be null.");
				              }
				
				              ComponentLauncherInfo info = new ComponentLauncherInfo(clazz, name, image, defaultProperties,
				                                                                     showInComponentsList, creator);
				              if(!components.contains(info)) {
					              components.add(info);
				              }
			              });
		} catch(NoSuchMethodException exc) {
			throw new RuntimeException("Must implement: public static void installComponent" +
				                           "(ComponentManagerInterface): " + clazz);
		} catch(RuntimeException exc) {
			throw exc;
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private void registerDefaultComponents() {
		register(PinPeer.class);
		register(ConstantPeer.class);
		register(Probe.class);
		register(ClockPeer.class);
		register(SplitterPeer.class);
		register(Tunnel.class);
		register(SimpleTransistorPeer.class);
		register(TransistorPeer.class);
		
		register(AndGatePeer.class);
		register(NandGatePeer.class);
		register(OrGatePeer.class);
		register(NorGatePeer.class);
		register(XorGatePeer.class);
		register(XnorGatePeer.class);
		register(NotGatePeer.class);
		register(ControlledBufferPeer.class);
		
		register(MultiplexerPeer.class);
		register(DemultiplexerPeer.class);
		register(DecoderPeer.class);
		register(PriorityEncoderPeer.class);
		
		register(RegisterPeer.class);
		register(SRFlipFlopPeer.class);
		register(DFlipFlopPeer.class);
		register(RAMPeer.class);
		register(ROMPeer.class);
		
		register(AdderPeer.class);
		register(SubtractorPeer.class);
		register(MultiplierPeer.class);
		register(DividerPeer.class);
		register(NegatorPeer.class);
		register(ComparatorPeer.class);
		register(BitExtenderPeer.class);
		register(ShifterPeer.class);
		register(RandomGeneratorPeer.class);
		
		register(Button.class);
		register(LED.class);
		register(LEDMatrix.class);
		register(HexDisplay.class);
		
		register(Text.class);
	}
	
	public interface ComponentCreator<T extends ComponentPeer<?>> {
		T createComponent(Properties properties, int x, int y);
	}
}

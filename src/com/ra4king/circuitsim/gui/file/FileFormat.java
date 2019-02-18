package com.ra4king.circuitsim.gui.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.Properties;

/**
 * @author Roi Atalla
 */
public class FileFormat {
	private static final Gson GSON;
	
	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public static String readFile(Reader reader) throws IOException {
		StringBuilder string = new StringBuilder();
		try(BufferedReader bufReader = new BufferedReader(reader)) {
			String line;
			while((line = bufReader.readLine()) != null) {
				string.append(line).append("\n");
			}
			
			return string.toString();
		}
	}
	
	public static String readFile(File file) throws IOException {
		return readFile(new FileReader(file));
	}
	
	public static void writeFile(File file, String contents) throws IOException {
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(contents);
			writer.write('\n');
		}
	}

	private static String sha256ify(String input) {
		// Shamelessly stolen from:
		// https://medium.com/programmers-blockchain/create-simple-blockchain-java-tutorial-from-scratch-6eeed3cb03fa
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//Applies sha256 to our input,
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class RevisionSignatureBlock {
		private String currentHash;
		private String previousHash;
		private String fileDataHash;
		private String timeStamp;
		private String copiedBlocks;

		private RevisionSignatureBlock(String stringifiedBlock) {
			String decodedBlock = new String(Base64.getDecoder().decode(stringifiedBlock.getBytes()));
			String[] fields = decodedBlock.split("\t");
			if (fields.length < 4) {
				throw new NullPointerException("File is corrupted. Contact Course Staff for Assistance.");
			} else {
				this.previousHash = fields[0];
				this.currentHash = fields[1];
				this.timeStamp = fields[2];
				this.fileDataHash = fields[3];
				this.copiedBlocks = "";
				for (int i = 4; i < fields.length; i++) {
					this.copiedBlocks += "\t" + fields[i];
				}
			}
		}

		private RevisionSignatureBlock(String previousHash, String fileDataHash, List<String> copiedBlocks) {
			this.previousHash = previousHash;
			this.fileDataHash = fileDataHash;
			this.timeStamp = "" + System.currentTimeMillis();
			this.copiedBlocks = "";
			for (String hash : copiedBlocks) {
				this.copiedBlocks += "\t" + hash;
			}
			this.currentHash = this.hash();
		}

		private String hash() {
			return FileFormat.sha256ify(previousHash + fileDataHash + timeStamp + this.copiedBlocks);
		}

		private String stringify() {
			// Lack of a tab between fileDataHash and copiedBlocks is intentional. copiedBlocks starts with a tab.
			String stringifiedBlock = Base64.getEncoder().encodeToString(
					String.format("%s\t%s\t%s\t%s%s", previousHash, currentHash, timeStamp, fileDataHash,
													  copiedBlocks).getBytes());
			return stringifiedBlock;
		}
	}

	private static String getLastHash(List<String> revisionSignatures) {
		if (revisionSignatures.size() == 0) {
			return "";
		} else {
			RevisionSignatureBlock tailBlock =
				new RevisionSignatureBlock(revisionSignatures.get(revisionSignatures.size() - 1));
			return tailBlock.currentHash;
		}
	}

	public static class CircuitFile {
		private final String version = CircuitSim.VERSION;
		
		public final int globalBitSize;
		public final int clockSpeed;
		public final List<String> libraryPaths;
		public final List<CircuitInfo> circuits;
		public final List<String> revisionSignatures;
		private List<String> copiedBlocks;
		
		public CircuitFile(int globalBitSize, int clockSpeed, List<String> libraryPaths, List<CircuitInfo> circuits,
						   List<String> revisionSignatures, List<String> copiedBlocks) {
			this.globalBitSize = globalBitSize;
			this.clockSpeed = clockSpeed;
			this.libraryPaths = libraryPaths;
			this.circuits = circuits;
			this.revisionSignatures = revisionSignatures;
			this.copiedBlocks = copiedBlocks;
		}


		private String hash() {
			String fileData = GSON.toJson(libraryPaths)
							+ GSON.toJson(circuits);
			return FileFormat.sha256ify(fileData);
		}

		public void addRevisionSignatureBlock() {
			String currentFileDataHash = hash();
			String previousHash = FileFormat.getLastHash(revisionSignatures);
			RevisionSignatureBlock newBlock =
				new RevisionSignatureBlock(previousHash, currentFileDataHash, copiedBlocks);
			revisionSignatures.add(newBlock.stringify());
			this.copiedBlocks = null;
		}

		public boolean revisionSignaturesAreValid() {
			if (revisionSignatures == null || revisionSignatures.size() < 1) {
				return false;
			}
			String expectedFileDataHash = this.hash();
			RevisionSignatureBlock lastBlock =
				new RevisionSignatureBlock(this.revisionSignatures.get(this.revisionSignatures.size() - 1));
			String actualFileDataHash = lastBlock.fileDataHash;
			if (!actualFileDataHash.equals(expectedFileDataHash) || !lastBlock.currentHash.equals(lastBlock.hash())) {
				return false;
			}
			String[] blocks = this.revisionSignatures.toArray(new String[]{});
			for (int i = blocks.length - 1; i > 0; i--) {
				RevisionSignatureBlock block = new RevisionSignatureBlock(blocks[i]);
				RevisionSignatureBlock prevBlock = new RevisionSignatureBlock(blocks[i - 1]);
				if (!block.currentHash.equals(block.hash()) || !block.previousHash.equals(prevBlock.currentHash)) {
					return false;
				}
			}
			return new RevisionSignatureBlock(blocks[0]).previousHash.equals("");
		}

		public List<String> getCopiedBlocks() {
			return this.copiedBlocks;
		}
	}
	
	public static class CircuitInfo {
		public final String name;
		public final List<ComponentInfo> components;
		public final List<WireInfo> wires;
		
		public CircuitInfo(String name, List<ComponentInfo> components, List<WireInfo> wires) {
			this.name = name;
			this.components = components;
			this.wires = wires;
		}
	}
	
	public static class ComponentInfo {
		public final String name;
		public final int x;
		public final int y;
		public final Map<String, String> properties;
		
		public ComponentInfo(String name, int x, int y, Map<String, String> properties) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.properties = properties;
		}
		
		public ComponentInfo(String name, int x, int y, Properties properties) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.properties = new HashMap<>();
			properties.forEach(prop -> this.properties.put(prop.name, prop.getStringValue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(name, x, y, properties);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof ComponentInfo) {
				ComponentInfo otherComp = (ComponentInfo)other;
				return name.equals(otherComp.name) && x == otherComp.x
					       && y == otherComp.y && properties.equals(otherComp.properties);
			}
			
			return false;
		}
	}
	
	public static class WireInfo {
		public final int x;
		public final int y;
		public final int length;
		public final boolean isHorizontal;
		
		public WireInfo(int x, int y, int length, boolean isHorizontal) {
			this.x = x;
			this.y = y;
			this.length = length;
			this.isHorizontal = isHorizontal;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(x, y, length, isHorizontal);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof WireInfo) {
				WireInfo otherWire = (WireInfo)other;
				return x == otherWire.x && y == otherWire.y
					       && length == otherWire.length && isHorizontal == otherWire.isHorizontal;
			}
			
			return false;
		}
	}
	
	public static void save(File file, CircuitFile circuitFile) throws IOException {
		circuitFile.addRevisionSignatureBlock();
		writeFile(file, stringify(circuitFile));
	}
	
	public static String stringify(CircuitFile circuitFile) {
		return GSON.toJson(circuitFile);
	}
	
	public static CircuitFile load(File file) throws IOException {
		CircuitFile savedFile = parse(readFile(file));
		if (!savedFile.revisionSignaturesAreValid()) {
			throw new NullPointerException("File is corrupted. Contact Course Staff for Assistance.");
		}
		return savedFile;
	}
	
	public static CircuitFile parse(String contents) {
		return GSON.fromJson(contents, CircuitFile.class);
	}
}

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
import java.util.Set;

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

    private static class SaveHistoryBlock {
        private String currentHash;
        private String previousHash;
        private String fileDataHash;
        private String timeStamp;

        private SaveHistoryBlock(String stringifiedBlock) {
            String decodedBlock = new String(Base64.getDecoder().decode(stringifiedBlock.getBytes()));
            String[] fields = decodedBlock.split("\t");
            System.out.println(decodedBlock);
            if (fields.length != 4) {
                throw new NullPointerException("File is corrupted. Contact Course Staff for Assistance.");
            } else {
                this.previousHash = fields[0];
                this.currentHash = fields[1];
                this.timeStamp = fields[2];
                this.fileDataHash = fields[3];
            }
        }

        private SaveHistoryBlock(String previousHash, String fileDataHash) {
            this.previousHash = previousHash;
            this.fileDataHash = fileDataHash;
            this.timeStamp = "" + System.currentTimeMillis();
            this.currentHash = FileFormat.sha256ify(previousHash + fileDataHash + timeStamp);
        }

        private String stringify() {
            return Base64.getEncoder().encodeToString(
                    String.format("%s\t%s\t%s\t%s", previousHash, currentHash, timeStamp, fileDataHash).getBytes());
        }
    }

    public static String getLastHash(List<String> saveHistory) {
        if (saveHistory.size() == 0) {
            return "";
        } else {
            SaveHistoryBlock tailBlock = new SaveHistoryBlock(saveHistory.get(saveHistory.size() - 1));
            return tailBlock.currentHash;
        }
    }
	
	public static class CircuitFile {
		private final String version = CircuitSim.VERSION;
		
		public final int globalBitSize;
		public final int clockSpeed;
		public final List<String> libraryPaths;
		public final List<CircuitInfo> circuits;
        public final List<String> saveHistory;
		
		public CircuitFile(int globalBitSize, int clockSpeed, List<String> libraryPaths, List<CircuitInfo> circuits,
                           List<String> saveHistory) {
			this.globalBitSize = globalBitSize;
			this.clockSpeed = clockSpeed;
			this.libraryPaths = libraryPaths;
			this.circuits = circuits;
            this.saveHistory = saveHistory;
		}


        private String hash() {
            // Previous save's hash is factored into this since the fileData will include the saveHistory
            String fileData = FileFormat.stringify(this);
            return FileFormat.sha256ify(fileData);
        }

        public void addSaveHistoryBlock() {
            String previousHash = FileFormat.getLastHash(saveHistory);
            SaveHistoryBlock newBlock = new SaveHistoryBlock(previousHash, hash());
            saveHistory.add(newBlock.stringify());
        }

        public boolean saveHistoryIsValid() {
            if (saveHistory == null || saveHistory.size() < 1) {
                return false;
            }
            return true;
        }
	}
	
	public static class CircuitInfo {
		public final String name;
		public final Set<ComponentInfo> components;
		public final Set<WireInfo> wires;
		
		public CircuitInfo(String name, Set<ComponentInfo> components, Set<WireInfo> wires) {
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
	}
	
	public static void save(File file, CircuitFile circuitFile) throws IOException {
        circuitFile.addSaveHistoryBlock();
		writeFile(file, stringify(circuitFile));
	}
	
	public static String stringify(CircuitFile circuitFile) {
		return GSON.toJson(circuitFile);
	}
	
	public static CircuitFile load(File file) throws IOException {
		CircuitFile savedFile = parse(readFile(file));
        if (!savedFile.saveHistoryIsValid()) {
            throw new NullPointerException("File is corrupted. Contact Course Staff for Assistance.");
        }
        return savedFile;
	}
	
	public static CircuitFile parse(String contents) {
		CircuitFile savedFile = GSON.fromJson(contents, CircuitFile.class);
        return savedFile;
	}
}

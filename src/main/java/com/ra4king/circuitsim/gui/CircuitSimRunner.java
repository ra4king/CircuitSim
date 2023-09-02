package com.ra4king.circuitsim.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Roi Atalla
 */
public class CircuitSimRunner {
	public static void main(String[] args) {
		try (NativeLibraryExtractor extractor = new NativeLibraryExtractor()) {
			extractor.extractNativeLibs();
			CircuitSim.run(args);
		}
	}

	/**
	 * Hack to make the CircuitSim fat jar work on both amd64 and aarch64.
	 * <p>
	 * The problem is that amd64 and aarch64 Macs (for example) would require
	 * bundling JavaFX native libraries for both amd64 and aarch64 in our fat
	 * jar... with the same filename. The reason for this is that the
	 * <a href="https://github.com/openjdk/jfx/blob/86b854dc367fb32743810716da5583f7d59208f8/modules/javafx.graphics/src/main/java/com/sun/glass/utils/NativeLibLoader.java#L330">
	 * JavaFX code for loading the native libraries</a> is quite inflexible and
	 * simply attempts to load e.g. libjavafx_font.so with no consideration for
	 * which architecture is running. We avoid this first stumbling block
	 * by having the Gradle build script sort the native libraries into
	 * architecture-specific directories inside the CircuitSim fat jar. The
	 * next hurdle is getting JavaFX to load them. Unfortunately, you
	 * <a href="https://stackoverflow.com/a/10144117/321301">cannot</a> set
	 * java.library.path (the path to JNI libraries) dynamically, so we cannot
	 * simply extract the native libraries at startup to a temporary directory
	 * and update java.library.path. Instead, we have to settle for taking
	 * advantage of the behavior of the JavaFX code previously linked and
	 * simply writing the native libraries to the same directory as the
	 * currently executing jar, which is where JavaFX is expecting them anyway.
	 * We try as hard as possible in this file to delete those libraries once
	 * CircuitSim exits. Unfortunately, this could cause some ugly situations
	 * with running multiple copies of CircuitSim.
	 **/
	private static class NativeLibraryExtractor implements AutoCloseable {
		private List<Path> libsExtracted;

		public NativeLibraryExtractor() {
			libsExtracted = new ArrayList<Path>();
		}

		public void extractNativeLibs() {
			OperatingSystem os = OperatingSystem.guess();

			String arch = System.getProperty("os.arch");
			String archDirPathName = "/" + arch;
			URL archDirResource = NativeLibraryExtractor.class.getResource(archDirPathName);

			if (archDirResource == null) {
				throw new RuntimeException("Can't find native libraries for architecture " + arch);
			}

			URI archDir;
			try {
				archDir = archDirResource.toURI();
			} catch (URISyntaxException exc) {
				// Checked exception
				throw new RuntimeException(exc);
			}

			if (!archDir.getScheme().equals("jar")) {
				throw new RuntimeException("I'm expecting to be executing inside a jar, but it seems I am not");
			}
			String jarFilePath = archDir.getSchemeSpecificPart();
			int lastBangIndex = jarFilePath.lastIndexOf('!');
			if (!jarFilePath.startsWith("file:") || lastBangIndex == -1) {
				throw new RuntimeException("Bad format for resource URI");
			}
			String jarPathName = jarFilePath.substring(5, lastBangIndex);
			Path jarPath = Paths.get(jarPathName);

			try (FileSystem fs = FileSystems.newFileSystem(archDir, Collections.<String, String>emptyMap())) {
				Path dir = fs.getPath(archDirPathName);
				Files.list(dir).forEach(nativeLib -> {
					// Performance optimization: we are on the critical path
					// here, so avoid copying libraries we don't need
					String baseName = nativeLib.getFileName().toString();
					if ((os == OperatingSystem.WINDOWS && baseName.endsWith(".dll"))
					    || (os == OperatingSystem.LINUX && baseName.endsWith(".so"))
					    || (os == OperatingSystem.MACOS && baseName.endsWith(".dylib"))) {
						Path dest = jarPath.resolveSibling(nativeLib.getFileName().toString());
						try {
							Files.copy(nativeLib, dest, StandardCopyOption.REPLACE_EXISTING);
							libsExtracted.add(dest);
						} catch (IOException exc) {
							throw new RuntimeException("Could not copy native library from jar to disk", exc);
						}
					}
				});
			} catch (IOException exc) {
				throw new RuntimeException("Could not copy native libraries from jar to disk", exc);
			}
		}

		@Override
		public void close() {
			libsExtracted.forEach(fp -> {
				// Obvious race condition here, but this is better than
				// spamming the console with stack traces when you open two
				// copies of CircuitSim and close both
				if (!Files.exists(fp)) {
					System.err.println("Warning: native library " + fp.toString() + " deleted out from under me");
				} else {
					try {
						Files.delete(fp);
					} catch (IOException exc) {
						// Write this as a println instead of a throw
						// intentionally: even if one deletion fails, we want to
						// try and delete as many of the rest as we can
						System.err.println("Could not delete native library:");
						exc.printStackTrace();
					}
				}
			});
		}

		private static enum OperatingSystem {
			WINDOWS,
			LINUX,
			MACOS;

			public static OperatingSystem guess() {
				String os = System.getProperty("os.name").toLowerCase();

				if (os.contains("windows")) {
					return WINDOWS;
				} else if (os.contains("linux")) {
					return LINUX;
				} else if (os.contains("mac")) {
					return MACOS;
				} else {
					throw new RuntimeException("Unsupported operating system " + os);
				}
			}
		}
	}
}

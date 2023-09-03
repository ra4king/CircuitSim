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
import java.util.Collections;
import java.util.stream.Stream;

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
	 * Naively supporting both amd64 and aarch64 Macs (for example) in one fat
	 * jar would require bundling JavaFX native libraries for both amd64 and
	 * aarch64 in our fat jar... with the same filename. The reason for this is
	 * that the
	 * <a href="https://github.com/openjdk/jfx/blob/86b854dc367fb32743810716da5583f7d59208f8/modules/javafx.graphics/src/main/java/com/sun/glass/utils/NativeLibLoader.java#L201">
	 * JavaFX code for loading the native libraries from the running jar</a> is
	 * quite inflexible and simply attempts to load e.g. libjavafx_font.so with
	 * no consideration for which architecture is running. We avoid this first
	 * stumbling block by having the Gradle build script sort the native
	 * libraries into architecture-specific directories inside the CircuitSim
	 * fat jar.
	 * <p>
	 * The next hurdle is getting JavaFX to load the native libraries for the
	 * current architecture. Thankfully, when JavaFX cannot find native
	 * libraries at the root of the running jar,
	 * <a href="https://github.com/openjdk/jfx/blob/86b854dc367fb32743810716da5583f7d59208f8/modules/javafx.graphics/src/main/java/com/sun/glass/utils/NativeLibLoader.java#L143">
	 * JavaFX manually queries java.library.path</a> and walks through it,
	 * looking for the native libraries. (If it simply called
	 * System.loadLibrary(),
	 * <a href="https://stackoverflow.com/a/10144117/321301">we could not
	 * modify java.library.path.</a>) So our job here is to create a temporary
	 * directory, fill it with the native libraries for this OS and
	 * architecture, and put that temporary directory in java.library.path.
	 * Then JavaFX will load the native libraries! (Our job is also to delete
	 * the temporary directory when CircuitSim exits.)
	 * <p>
	 * Make this class public in case some dependency like the autograder
	 * library needs to use it.
	 **/
	public static class NativeLibraryExtractor implements AutoCloseable {
		private Path tempDir;

		public NativeLibraryExtractor() {
			try {
				tempDir = Files.createTempDirectory("circuitsim-libs");
			} catch (IOException exc) {
				tempDir = null;
				throw new RuntimeException("Couldn't create temporary directory for native libraries", exc);
			}
		}

		// Return .dll on Windows, .so on Linux, .dylib on macOS, etc.
		private String guessNativeLibraryExtension() {
			String foo_dot_dll = System.mapLibraryName("foo");
			int dot_idx;
			if (foo_dot_dll == null || (dot_idx = foo_dot_dll.lastIndexOf('.')) == -1) {
				throw new RuntimeException("Unsupported format of native library filenames. Bug in JRE?");
			}
			return foo_dot_dll.substring(dot_idx);
		}

		public void extractNativeLibs() {
			String nativeLibraryExtension = guessNativeLibraryExtension();
			String arch = System.getProperty("os.arch");
			if (arch == null) {
				throw new RuntimeException("JRE did not give us an architecture, no way to load native libraries");
			}

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

			try (FileSystem fs = FileSystems.newFileSystem(archDir, Collections.<String, String>emptyMap())) {
				Path dir = fs.getPath(archDirPathName);
				Files.list(dir).forEach(nativeLib -> {
					// Performance optimization: we are on the critical path
					// here, so avoid copying libraries we don't need
					String baseName = nativeLib.getFileName().toString();
					if (baseName.endsWith(nativeLibraryExtension)) {
						Path dest = tempDir.resolve(nativeLib.getFileName().toString());
						try {
							Files.copy(nativeLib, dest);
						} catch (IOException exc) {
							throw new RuntimeException("Could not copy native library from jar to disk", exc);
						}
					}
				});
			} catch (IOException exc) {
				throw new RuntimeException("Could not copy native libraries from jar to disk", exc);
			}

			String existingNativeLibPath = System.getProperty("java.library.path");
			if (existingNativeLibPath == null) {
				System.setProperty("java.library.path", tempDir.toString());
			} else {
				System.setProperty("java.library.path", existingNativeLibPath + File.pathSeparatorChar + tempDir.toString());
			}
		}

		@Override
		public void close() {
			// Creating the temporary directory failed, nothing to do here
			if (tempDir == null) {
				return;
			}

			// When we catch IOExceptions here, print the errors instead of
			// re-throwing. The idea is that even if one deletion fails, we
			// want to try and delete as many of the rest as we can
			Stream<Path> children = null;
			try {
				children = Files.list(tempDir);
			} catch (IOException exc) {
				System.err.println("Could not get children of temporary directory with native libraries. Trying to delete it anyway...");
				exc.printStackTrace();
			}

			if (children != null) {
				children.forEach(fp -> {
					try {
						Files.delete(fp);
					} catch (IOException exc) {
						System.err.println("Could not delete native library:");
						exc.printStackTrace();
					}
				});
			}

			try {
				Files.delete(tempDir);
			} catch (IOException exc) {
				throw new RuntimeException("Could not delete temporary directory " + tempDir.toString(), exc);
			}
		}
	}
}

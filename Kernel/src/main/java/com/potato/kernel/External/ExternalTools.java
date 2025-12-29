package com.potato.kernel.External;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the ExternalTools directory in a way that works for IDE runs, Gradle runs,
 * and packaged (jpackage) app-images.
 *
 * <p>Rules:
 * <ol>
 *   <li>If {@code -Dclinic.externalToolsDir} is an absolute path, use it.</li>
 *   <li>If it is relative, resolve it against {@code user.dir} (jpackage sets it to the app-image root).</li>
 *   <li>If the property is missing/blank, default to {@code ExternalTools} next to {@code user.dir}.</li>
 * </ol>
 */
public final class ExternalTools {
    private ExternalTools() {}

    public static Path resolveToolsDir() {
        String toolsDirProp = System.getProperty("clinic.externalToolsDir");
        String relOrAbs = (toolsDirProp == null || toolsDirProp.isBlank()) ? "ExternalTools" : toolsDirProp;

        Path configured = Paths.get(relOrAbs);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }

        String userDir = System.getProperty("user.dir");
        if (userDir != null && !userDir.isBlank()) {
            return Paths.get(userDir).resolve(configured).normalize();
        }

        return configured.toAbsolutePath().normalize();
    }
}


import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
}

group = "com.potato"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":Kernel"))
    implementation("commons-io:commons-io:2.21.0")
    implementation("org.apache.commons:commons-csv:1.14.1")
}

java {
    modularity.inferModulePath.set(true)
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web")
}

application {
    mainModule.set("com.potato.desktop")
    mainClass.set("com.potato.desktop.MainApp")
}

// Modern packaging for JavaFX on JDK 11+.
// Produces a custom runtime image: `gradlew :Desktop:jlink`
// Produces a native installer/bundle (Windows): `gradlew :Desktop:jpackage`
jlink {
    // Put the image in a versioned directory so rebuilding doesn't need to delete
    // a possibly-running/locked previous image on Windows.
    imageName.set("ClinicAssistant-${project.version}")

    launcher {
        name = "ClinicAssistant"
    }

    options.set(
        listOf(
            "--strip-debug",
            "--compress", "2",
            "--no-header-files",
            "--no-man-pages"
        )
    )

    jpackage {
        // jpackage requires a numeric version like 1.0.0 (it rejects 1.0-SNAPSHOT)
        val rawVersion = project.version.toString()
        val packagingVersion = rawVersion.substringBefore('-')
            .ifBlank { "1.0.0" }
            .let { v -> if (v.count { it == '.' } == 1) "$v.0" else v }

        appVersion = packagingVersion
        vendor = "com.potato"
        // For Windows you can set `installerType = "exe"` or "msi" if desired.
        // installerType = "exe"

        // Build an installer instead of manually copying the app-image.
        // This is more reliable on Windows (shortcuts, correct layout, runtime discovery).
        installerOptions.clear()

        // If you later install WiX, you can enable MSI like this:
        // installerOptions.addAll(listOf("--type", "msi"))

        // Ensure we always have a deterministic tools dir relative to where the launcher is run.
        imageOptions.addAll(
            listOf(
                "--java-options", "-Dclinic.externalToolsDir=ExternalTools"
                ,"--java-options", "-Dprism.order=sw"
            )
        )
    }
}

val externalToolsDir = (rootProject.findProperty("externalToolsDir") as String?) ?: "ExternalTools"
val externalToolsAbs = rootProject.projectDir.resolve(externalToolsDir).absolutePath

// Ensure LibreHardwareMonitorWrapper is self-contained for distribution (VMs often lack .NET runtime).
val publishLhmWrapper by tasks.registering(Exec::class) {
    group = "distribution"
    description = "Publishes LibreHardwareMonitorWrapper as a self-contained single-file exe into ExternalTools."

    val wrapperProject = rootProject.projectDir.resolve("ExternalTools/LibreHardwareMonitorWrapper/LibreHardwareMonitorWrapper.csproj")
    val wrapperOutput = rootProject.projectDir.resolve("ExternalTools/LibreHardwareMonitorWrapper/build")

    inputs.file(wrapperProject)
    outputs.dir(wrapperOutput)

    doFirst {
        // Avoid stale outputs (e.g., old net10 runtimeconfig) causing VM failures.
        if (wrapperOutput.exists()) {
            wrapperOutput.deleteRecursively()
        }
        wrapperOutput.mkdirs()
    }

    // Publish into the exact folder the Java app expects.
    commandLine(
        "dotnet", "publish",
        wrapperProject.absolutePath,
        "-c", "Release",
        "-r", "win-x64",
        "--self-contained", "true",
        "-p:PublishDir=${wrapperOutput.absolutePath}\\"
    )

    doLast {
        val sys = rootProject.projectDir.resolve("ExternalTools/LibreHardwareMonitorWrapper/LibreHardwareMonitorWrapper.sys")
        if (sys.exists()) {
            sys.copyTo(wrapperOutput.resolve(sys.name), overwrite = true)
        }
    }
}

// Copy ExternalTools into the jpackage app-image so the packaged app can find it
tasks.named("jpackageImage").configure {
    dependsOn(publishLhmWrapper)
    doLast {
        val sourceExternalTools = rootProject.layout.projectDirectory.dir("ExternalTools")
        val targetExternalTools = layout.buildDirectory.dir("jpackage/ClinicAssistant/ExternalTools")
        
        // Delete target if it exists
        targetExternalTools.get().asFile.deleteRecursively()
        
        // Copy ExternalTools into the app-image
        copy {
            from(sourceExternalTools)
            into(targetExternalTools)
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dclinic.externalToolsDir=$externalToolsAbs")
}

tasks.withType<Test>().configureEach {
    systemProperty("clinic.externalToolsDir", externalToolsAbs)
}

tasks.test {
    useJUnitPlatform()
}

// Creates a publishable zip containing the jpackage app-image + ExternalTools.
// Output: Desktop/build/release/ClinicAssistant-<version>-windows.zip
val releaseZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "Packages the Windows app-image plus ExternalTools into a single zip for distribution."

    dependsOn(tasks.named("jpackageImage"))

    val appImageDir = layout.buildDirectory.dir("jpackage/ClinicAssistant")

    from(appImageDir) {
        into("ClinicAssistant")
    }

    // Ship ExternalTools next to the exe (app code assumes this layout)
    from(rootProject.layout.projectDirectory.dir("ExternalTools")) {
        into("ClinicAssistant/ExternalTools")
    }

    destinationDirectory.set(layout.buildDirectory.dir("release"))
    archiveFileName.set("ClinicAssistant-${project.version}-windows.zip")
}

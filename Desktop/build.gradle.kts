plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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

tasks.test {
    useJUnitPlatform()
}
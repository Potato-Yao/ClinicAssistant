plugins {
    id("java")
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
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.google.code.gson:gson:2.13.2")
}

java {
    modularity.inferModulePath.set(true)
}

val externalToolsDir = (rootProject.findProperty("externalToolsDir") as String?) ?: "ExternalTools"
val externalToolsAbs = rootProject.projectDir.resolve(externalToolsDir).absolutePath

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dclinic.externalToolsDir=$externalToolsAbs")
}

tasks.withType<Test>().configureEach {
    systemProperty("clinic.externalToolsDir", externalToolsAbs)
}

tasks.test {
    useJUnitPlatform()
}
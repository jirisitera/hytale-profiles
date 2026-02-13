plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}
group = "com.japicraft"
version = "1.1.0"
repositories {
    mavenCentral()
    maven { url = uri("https://maven.hytale.com/release") }
}
dependencies {
    compileOnly("com.hypixel.hytale:Server:+")
}
tasks {
    shadowJar {
        minimize()
        archiveBaseName.set("profiles")
        archiveVersion.set(version.toString())
        archiveClassifier.set("")
    }
}

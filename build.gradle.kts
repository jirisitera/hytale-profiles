plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
    id("com.japicraft.hytale") version "2.2"
}
group = "com.japicraft"
version = "1.1.0"
repositories {
    mavenCentral()
    maven { url = uri("https://maven.hytale.com/release") }
}
dependencies {
    compileOnly("com.hypixel.hytale:Server:2026.02.06-aa1b071c2")
}
tasks {
    shadowJar {
        archiveBaseName.set("profiles")
        archiveVersion.set("")
        archiveClassifier.set("")
    }
    runServer {
        buildLocation.set("build/libs/profiles.jar")
        xmx.set("3G")
        xms.set("3G")
    }
}

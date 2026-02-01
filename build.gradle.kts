plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
    id("com.japicraft.hytale") version "2.2"
}
group = "com.japicraft"
version = "1.0"
repositories {
    mavenCentral()
}
dependencies {
    compileOnly(files("run/Server/HytaleServer.jar"))
}
tasks {
    shadowJar {
        archiveBaseName.set("Profiles")
        archiveVersion.set("")
        archiveClassifier.set("")
    }
    runServer {
        buildLocation.set("build/libs/Profiles.jar")
        xmx.set("3G")
        xms.set("3G")
    }
}

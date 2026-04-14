plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.5"
}

group = "org"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainModule.set("ordex.main")
    mainClass.set("org.Main")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

jlink {
    imageName = "ordex"

    launcher {
        name = "ordex"
    }

    jpackage {
        installerType = "msi"
        appVersion = "1.0.0"
        vendor = "Inoue"
        installerName = "ordexInstaller"

        icon = "src/main/resources/ordex.ico"

        installerOptions.add("--install-dir")
        installerOptions.add("MyTool")
        installerOptions.add("--win-dir-chooser")
    }
}
plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "4.0.0"
}

group = "org"
version = "1.0.1"

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

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

jlink {
    imageName = "ordex"

    launcher {
        name = "ordex"
    }

    jpackage {
        installerType = "msi"
        appVersion = "1.0.1"
        vendor = "witchcrafttwo"
        installerName = "ordexInstaller"
        imageName = "ordex"
        icon = "src/main/resources/ordex.ico"

        installerOptions = listOf(
            "--win-menu",
            "--win-menu-group", "ordex",
            "--win-shortcut",
            "--win-per-user-install",
            "--install-dir",
            "ordex",
            "--win-dir-chooser"
        )

    }
}
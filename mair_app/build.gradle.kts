import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "life.biomedis"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation ("org.jsoup:jsoup:1.13.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("dist")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ru.biomedis.biomedismair3.App"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClassName = "ru.biomedis.biomedismair3.App"
}

import  java.net.URI

//group = "life.biomedis"
//version = "1.0"



val javaVersion = JavaVersion.VERSION_1_8
val encoding = "UTF-8"
object DependencyVersions {

	const val commonLangVersion = "3.8.1"
	const val collectionUtilsVersion = "4.3"
	const val mockitoVersion = "3.2.4"
	const val logbackVersion = "1.2.3"
	const val lombokVersion = "1.18.8"
	const val slf4jVersion = "1.7.26"
}
plugins {
	java
	id("application")
	//id("org.openjfx.javafxplugin") version "0.0.8"

}

subprojects{

	apply(plugin = "java")

	apply(plugin = "application")

	buildscript {
		repositories {
			mavenCentral()
		}
//		repositories {
//			maven {
//				setUrl("https://plugins.gradle.org/m2/")
//			}
//		}
//		dependencies {
//			classpath("org.openjfx:javafx-plugin:0.0.8")
//		}
	}
//	apply(plugin = "org.openjfx.javafxplugin")
//
//	javafx {
//		modules("javafx.controls", "javafx.fxml", "javafx.web", "javafx.base", "javafx.graphics", "javafx.swing")
//	}

	repositories {
		mavenLocal()
		mavenCentral()

//		maven {
//			url = URI("https://raw.github.com/lightway82/updater_repo/mvn-repo")
//			name = "updater_repo-mvn-repo"
//		}

	}

	configurations {
		compileOnly {
			extendsFrom(configurations.annotationProcessor.get())
		}
	}

	dependencies {
		
		testImplementation ("org.junit.jupiter:junit-jupiter-params:5.6.0")
		testImplementation ("org.junit.jupiter:junit-jupiter-api:5.6.0")
		testImplementation ("org.junit.platform:junit-platform-engine:1.6.0")
		testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.6.0")
		testImplementation ("org.junit.platform:junit-platform-commons:1.6.0")
		testImplementation("org.mockito", "mockito-core", DependencyVersions.mockitoVersion)
		testImplementation ("org.mockito","mockito-junit-jupiter",DependencyVersions.mockitoVersion) 

		compileOnly("org.projectlombok", "lombok", DependencyVersions.lombokVersion)
		annotationProcessor("org.projectlombok", "lombok", DependencyVersions.lombokVersion)
		implementation("ch.qos.logback", "logback-classic", DependencyVersions.logbackVersion)
		implementation("ch.qos.logback", "logback-core", DependencyVersions.logbackVersion)
		implementation("org.slf4j", "slf4j-api", DependencyVersions.slf4jVersion)
	   
		implementation("org.apache.commons", "commons-lang3", DependencyVersions.commonLangVersion)
		implementation("org.apache.commons", "commons-collections4", DependencyVersions.collectionUtilsVersion)
		implementation( "commons-io",  "commons-io",  "2.6")     

		implementation( "org.imgscalr","imgscalr-lib", "4.2")

		implementation( "com.h2database","h2", "1.4.196")
		implementation( "org.eclipse.persistence","eclipselink", "2.6.2")
		implementation( "org.reflections","reflections", "0.9.10")

		implementation( "org.swinglabs","pdf-renderer", "1.0.5")
		implementation( "com.mpatric","mp3agic", "0.8.3")
		implementation( "org.hid4java","hid4java", "0.5.0")
		implementation( "org.anantacreative","updater", "0.15.2")//https://github.com/lightway82/updater
		implementation( "io.github.openfeign","feign-core", "11.0")
		implementation( "io.github.openfeign","feign-jackson", "11.0")
	}

	configure<JavaPluginConvention> {
		sourceCompatibility = javaVersion
		targetCompatibility = javaVersion

	}


	tasks.compileJava {
		this.options.encoding = "UTF-8"

	}



	tasks.named<Test>("test") {
		useJUnitPlatform()
		maxHeapSize = "1G"
		systemProperties["spring.profiles.active"]="test"
	}
}

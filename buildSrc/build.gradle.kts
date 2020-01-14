import java.util.Properties

plugins {
	`kotlin-dsl`
}

/**
 * When accessing the gradle.properties file, it is preferable to use the singleton object
 * 	com.deliveryhero.alfred.connector.sdk.util.Properties.
 *
 * But since here we don't have access to it yet, it is been loaded here too.
 */
val gradleProperties by lazy {
	val properties = Properties()

	val file: File = rootProject.file("../gradle.properties")

	if (file.canRead()) {
		file.inputStream().use { properties.load(it) }
	}

	properties
}

val kotlinVersion = gradleProperties["kotlin.version"]

repositories {
	mavenCentral()
	jcenter()
	mavenLocal()
	maven { url = uri("https://repo.gradle.org/gradle/libs-releases-local")}
	gradlePluginPortal()
}

dependencies {
	compile("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
	compile(gradleApi())
	compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
	compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}

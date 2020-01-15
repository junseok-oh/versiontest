import com.deliveryhero.alfred.build_src.util.Properties
import com.deliveryhero.alfred.build_src.util.Util.executeExternalCommand
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.nio.file.Paths

buildscript {
    // This is needed in order to enable the build process.
    // We need to use the full package here:
    com.deliveryhero.alfred.build_src.util.Properties.modules.root.project = project(":")
}

plugins {
    // We need to use the full package here:
    val kotlinVersion = com.deliveryhero.alfred.build_src.util.Properties.vendor.kotlin

    id("org.springframework.boot") version "2.2.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm")
    kotlin("plugin.spring") version kotlinVersion

    id("org.jetbrains.dokka") version "0.9.18"

//    id("net.researchgate.release") version "2.8.1"

    `maven-publish`
}

group = Properties.modules.root.group!!
version = Properties.modules.root.version!!

java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven {
        name = "Spring Milestones"
        url = uri("https://repo.spring.io/milestone")
    }
    maven {
        name = "Delivery_Hero-Alfred-Snapshots"
        url = uri(Properties.get("artifactory.snapshot.url")!!)
        credentials {
            username = Properties.get("artifactory.username")
            password = Properties.get("artifactory.password")
        }
    }
    maven {
        name = "Delivery_Hero-Alfred-Releases"
        url = uri(Properties.get("artifactory.release.url")!!)
        credentials {
            username = Properties.get("artifactory.username")
            password = Properties.get("artifactory.password")
        }
    }
}

dependencies {
    api("org.slf4j:slf4j-api:1.7.28")

    implementation("org.springframework.boot:spring-boot-starter") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    implementation("io.arrow-kt:arrow-core:0.10.3")

    //temp dependency to Adyen SDK
    api("com.adyen:adyen-java-api-library:2.7.3")
    implementation("org.apache.commons:commons-lang3:3.9")

    implementation("com.github.wnameless:json-flattener:0.7.1")
    implementation("io.github.kostaskougios:cloning:1.10.1")
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

tasks.register<Jar>("javadocJar") {
    //  from(tasks.javadoc)
    archiveClassifier.set("javadoc")
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    from(dokka)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>("main") {
            groupId = group.toString()
            artifactId = project.name
            version = version

            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }

    repositories {
        maven {
            name = "Delivery_Hero-Alfred"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) {
                    Properties.get("artifactory.snapshot.url")!!
                } else {
                    Properties.get("artifactory.release.url")!!
                }
            )


            credentials {
                username = Properties.get("artifactory.username")
                password = Properties.get("artifactory.password")
            }
        }
    }
}

tasks.register<Exec>("myRelease") {
    val gradlePropertiesFile = Paths.get(Properties.modules.root.path!!.absolutePath, "gradle.properties").toFile()
    val remote = "origin"
    val masterBranch = "master"
    val snapshotSuffix = "-SNAPSHOT"

    val currentBranch = executeExternalCommand("git rev-parse --abbrev-ref HEAD")

    val currentVersion = Properties.modules.root.version!!

    val regex = "([0-9]+)\\.([0-9]+)\\.([0-9]+)-?(.*)".toRegex()
    val matchResult = regex.find(currentVersion)

    val (major, minor, patch, suffix) = matchResult!!.destructured

    if (suffix.isEmpty())
        throw Exception(
            """
            |Not possible to release because the current version ($currentVersion) is a release version.
            |Hint: try to append a SNAPSHOT suffix to your version.
            """.trimMargin()
        )

    val newReleaseVersion = "$major.$minor.$patch"
    val newSnapshotVersion = "$major.$minor.${patch.toInt().plus(1)}$snapshotSuffix"
    val tagName = newReleaseVersion

    // Iterating to the release version
    gradlePropertiesFile.writeText(
        gradlePropertiesFile
            .readText()
            .replace(
                "version=$currentVersion",
                "version=$newReleaseVersion"
            )
    )

    // Publishing to Artifactory
    executeExternalCommand("./gradlew publish")

    // Commiting the new version
    executeExternalCommand("git add ${gradlePropertiesFile.absolutePath}")
    executeExternalCommand("git commit -m \"Releasing $newReleaseVersion. Previous version was $currentVersion.\"")

    // Merging with the master branch
    executeExternalCommand("git checkout $masterBranch")
    executeExternalCommand("git merge $currentBranch")

    // Tagging and pushing to origin
    executeExternalCommand("git tag -a -m $tagName \"$newReleaseVersion release.\"")
    executeExternalCommand("git push $remote $tagName")
    executeExternalCommand("git push $remote $masterBranch")

    // Releasing a new version to GitHub
    executeExternalCommand("hub release create -m \"$newReleaseVersion release.\" $tagName")

    // Returning to the previous branch and merging master branch
    executeExternalCommand("git checkout $currentBranch")
    executeExternalCommand("git merge $masterBranch")

    // Iterating to the new snapshot version
    gradlePropertiesFile.writeText(
        gradlePropertiesFile
            .readText()
            .replace(
                "version=$newReleaseVersion",
                "version=$newSnapshotVersion"
            )
    )
    executeExternalCommand("git add ${gradlePropertiesFile.absolutePath}")
    executeExternalCommand("git commit -m \"Iterating to the $newSnapshotVersion version.\"")
    executeExternalCommand("git push $remote $currentBranch")
}

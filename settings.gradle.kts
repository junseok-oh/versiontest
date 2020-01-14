import com.deliveryhero.alfred.build_src.util.Properties

// This is needed in order to enable the build process.
Properties.modules.root.descriptor = rootProject

pluginManagement {
    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.springframework.boot") {
                useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = Properties.modules.root.artifact!!

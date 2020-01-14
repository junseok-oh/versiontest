package com.deliveryhero.alfred.build_src.util

import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File
import java.nio.file.Paths
import java.util.Properties as JavaProperties

/**
 *
 */
object Properties {

    /**
     *
     */
    object modules {

        /**
         *
         */
        object root {

            /**
             *
             */
            var descriptor: ProjectDescriptor? = null
                set(value) {
                    if (field != null) return

                    path = value?.projectDir

                    field = value
                }

            /**
             *
             */
            var project: Project? = null
                set(value) {
                    if (field != null) return

                    path = value?.projectDir

                    field = value
                }

            /**
             *
             */
            var path: File? = null
                set(value) {
                    if (field != null) return

                    loadProperties(local, Paths.get(value!!.absolutePath, "local.properties").toFile())
                    loadProperties(gradle, Paths.get(value.absolutePath, "gradle.properties").toFile())
                    loadProperties(globalGradle, Paths.get(System.getProperty("user.home"), ".gradle", "gradle.properties").toFile())

                    group = get("group")
                    artifact = get("artifact")
                    version = get("version")

                    field = value
                }

            /**
             *
             */
            var group: String? = null
                set(value) {
                    if (field != null) return

                    field = value
                }

            /**
             *
             */
            var artifact: String? = null
                set(value) {
                    if (field != null) return

                    field = value
                }

            /**
             *
             */
            var version: String? = null
                set(value) {
                    if (field != null) return

                    field = value
                }
        }

        /**
         *
         */
        object buildSrc {
            val group = root.group
            val artifact = "buildSrc"
            val version = root.version
        }
    }

    /**
     *
     */
    val local: JavaProperties = JavaProperties()

    /**
     *
     */
    val environment by lazy {
        System.getenv()
    }

    /**
     *
     */
    val system: JavaProperties = JavaProperties()

    /**
     *
     */
    val gradle: JavaProperties = JavaProperties()

    /**
     *
     */
    val globalGradle: JavaProperties = JavaProperties()

    /**
     *
     */
    fun containsKey(vararg keys: String): Boolean {
        if (keys.isNullOrEmpty()) return false

        keys.forEach {
            when {
                local.containsKey(it) -> return true
                environment.containsKey(it) -> return true
                system.containsKey(it) -> return true
                gradle.containsKey(it) -> return true
                globalGradle.containsKey(it) -> return true
            }
        }

        return false
    }

    /**
     *
     */
    fun get(vararg keys: String): String? {
        return getAndCast<String>(*keys)
    }

    /**
     *
     */
    inline fun <reified R> getOrDefault(vararg keys: String, default: R?): R? {
        return getAndCast<R>(*keys) ?: default
    }

    /**
     *
     */
    inline fun <reified R> getAndCast(vararg keys: String): R? {
        if (keys.isNullOrEmpty()) return null

        keys.forEach {
            val value = when {
                local.containsKey(it) -> local[it]
                environment.containsKey(it) -> environment[it]
                system.containsKey(it) -> system[it]
                gradle.containsKey(it) -> gradle[it]
                globalGradle.containsKey(it) -> globalGradle[it]
                else -> null
            }

            // TODO Improve the casting using Jackson
            if (value != null) return value as R
        }

        return null
    }

    /**
     *
     */
    object vendor {
        val kotlin = get("kotlin.version")
    }

    /**
     *
     */
    object util {

        /**
         *
         */
        val os by lazy {
            org.gradle.internal.os.OperatingSystem.current()!!
        }

        /**
         *
         */
        val jvm by lazy {
            org.gradle.internal.jvm.Jvm.current()
        }
    }

    /**
     *
     */
    private fun loadProperties(target: JavaProperties, file: File) {
        if (file.canRead()) {
            file.inputStream().use { target.load(it) }
        }
    }
}

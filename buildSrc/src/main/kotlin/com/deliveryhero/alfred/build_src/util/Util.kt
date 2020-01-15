package com.deliveryhero.alfred.build_src.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 *
 */
object Util {

    /**
     *
     */
    private val jacksonObjectMapper by lazy {
        ObjectMapper()
            .registerModule(KotlinModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
    }

    /**
     *
     */
    val logger = LoggerFactory.getLogger(Util::class.java)

    /**
     *
     */
    fun executeExternalCommand(vararg commands: String): String {
        val process = Runtime.getRuntime().exec(commands)

        val error = process.errorStream.bufferedReader().use { it.readText() }.trim()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()

        process.waitFor(10, TimeUnit.SECONDS)

        if (process.exitValue() != 0) {
            throw Exception("Error when executing command \"$commands\". Error: $error")
        }

        return output
    }

    /**
     *
     */
    fun dump(target: Any?, title: String? = null) {
        val dump = try {
            jacksonObjectMapper.writeValueAsString(target)
        } catch (exception: Exception) {
            target.toString()
        }

        logger.warn(
            "${if(title != null && title.isNotBlank()) title else "Dump"}: $dump"
        )
    }
}

package com.deliveryhero.alfred.build_src.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory

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

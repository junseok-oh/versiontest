package com.deliveryhero.alfred.connector.sdk.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.wnameless.json.flattener.JsonFlattener
import com.rits.cloning.Cloner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

object SerializationUtils {

    val jacksonObjectMapper: ObjectMapper by lazy {
        ObjectMapper()
    }

    val ritsCloner: Cloner by lazy {
        Cloner()
    }

    fun toMutableMap(input: String?): MutableMap<String, Any> {
        val returningType: MutableMap<String, Any> = mutableMapOf()

        return jacksonObjectMapper.readValue(input, returningType::class.java)
    }

    fun toFlatMap(input: Any?, isAlreadyValidJson: Boolean = false): MutableMap<String, Any> {
        return when(isAlreadyValidJson) {
            true -> JsonFlattener.flattenAsMap(input.toString())
            else -> JsonFlattener.flattenAsMap(toJson(input))
        }
    }

    fun toJson(input: Any?): String {
        return jacksonObjectMapper.writeValueAsString(input)
    }

    fun toFlatJson(input: Any?): String {
        return JsonFlattener.flatten(toJson(input))
    }

    fun toFlatMdcMap(input: Any?, isAlreadyValidJson: Boolean = false): MutableMap<String, String> {
        val map = toFlatMap(input, isAlreadyValidJson)

        map.forEach { (k, v) -> map[k] = "$v"}

        @Suppress("UNCHECKED_CAST")
        return map as MutableMap<String, String>
    }

    /**
     * This approach can work with serializable and non-serializable objects, but it also has some limitations.
     */
    inline fun <reified T: Any> deepCloneWithRits(target: T?): T? {
        return ritsCloner.deepClone(target)
    }

    /**
     * This approach can fail with the object contains a loop or do not have constructors.
     */
    inline fun <reified T: Any> deepCloneWithJackson(target: T?): T? {
        return jacksonObjectMapper.readValue(jacksonObjectMapper.writeValueAsString(target), T::class.java)
    }

    /**
     * This approach can fail if the object (or the nested ones) is not serializable.
     */
    inline fun <reified T : Serializable> deepCloneWithStreams(target: T?): T? = when(target) {
        null -> null
        else -> ByteArrayOutputStream().use { baos ->
            ObjectOutputStream(baos).use {
                it.writeObject(target)
            }
            ByteArrayInputStream(baos.toByteArray()).use {
                ObjectInputStream(it).readObject() as T
            }
        }
    }
}

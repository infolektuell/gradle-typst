package de.infolektuell.gradle.typst.model

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/** A service class that loads and caches [Properties] data */
class PropertiesCache {
    private val pathProperties = mutableMapOf<Path, Properties>()
    private val resourceProperties = mutableMapOf<String, Properties>()

    /* Returns [Properties] from a [path], merges into [default] properties if given */
    fun load(path: Path, default: Properties? = null): Properties {
        return pathProperties.computeIfAbsent(path) { k ->
            default ?: Properties().apply {
                load(Files.newBufferedReader(k))
            }
        }
    }

    /* Returns [Properties] from a [resource], merges into [default] properties if given */
    fun load(resource: String, default: Properties? = null): Properties {
        return resourceProperties.computeIfAbsent(resource) { k ->
            default ?: Properties().apply {
                object {}.javaClass.getResourceAsStream(k)?.use {
                    load(it)
                }
            }
        }
    }
}

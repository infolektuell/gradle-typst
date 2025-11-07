package de.infolektuell.gradle.typst.providers

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class GitHashValueSource @Inject constructor(private val execOperations: ExecOperations) :
    ValueSource<String, GitHashValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val revision: Property<String>
        val length: Property<Int>
    }

    override fun obtain(): String {
        return ByteArrayOutputStream().use { s ->
            execOperations.exec { spec ->
                spec.executable("git")
                spec.args("rev-parse")
                if (parameters.length.isPresent) spec.args("--short=${parameters.length.get()}")
                spec.args(parameters.revision.getOrElse("HEAD"))
                spec.standardOutput = s
            }
            s.toString(Charset.defaultCharset()).trim()
        }
    }
}

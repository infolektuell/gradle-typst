package de.infolektuell.gradle.typst.providers

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

abstract class GitCommitDateValueSource @Inject constructor(private val execOperations: ExecOperations) :
    ValueSource<String, GitCommitDateValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val revision: Property<String>
    }

    override fun obtain(): String {
        return ByteArrayOutputStream().use { s ->
            execOperations.exec { spec ->
                spec.executable("git")
                spec.args("--no-pager", "show", "-s", "--format=%ct", parameters.revision.getOrElse("HEAD"))
                spec.standardOutput = s
            }
            s.toString(Charset.defaultCharset()).trim()
        }
    }
}

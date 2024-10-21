import de.infolektuell.gradle.typst.providers.GitHashValueSource
import de.infolektuell.gradle.typst.providers.GitCommitDateValueSource

plugins {
    base
    id("de.infolektuell.typst")
}

val gitHashProvider = providers.of(GitHashValueSource::class) {
    parameters {
        length = 8
        revision = "main"
    }
}
version = gitHashProvider.get()

val timestamp = providers.of(GitCommitDateValueSource::class) {
    parameters {
        revision = "main"
    }
}

typst {
    version = "v0.12.0"
    sourceSets {
        create("main") {
            documents.add("document")
            inputs.put("gitHash", project.version.toString())
            format {
                pdf.enabled = true
                svg.enabled = true
                png {
                    enabled = true
                    ppi = 72
                }
            }
        }
    }
    creationTimestamp = timestamp.get()
}

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
    creationTimestamp = timestamp.get()
    groups {
    register("main") {
    inputs.put("x", "y")
    documents {
    register("document") {
            inputs.put("gitHash", project.version.toString())
            inputs.put("x", "hi")
            val targets = pdf {
            register("pdf")
            register("pdf2")
            }
            merge.register("full") {
            source.addAll(targets)
            }
    }
    }
    }
    }
}

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
    version = "v0.13.1"
    creationTimestamp = timestamp.get()
}

val common by typst.sourceSets.registering {
    inputs.put("gitHash", project.version.toString())
}

val main by typst.sourceSets.registering {
    addSourceSet(common)
    documents.add("document")
    format {
        png.enabled = true
        svg.enabled = true
    }
    val copyTask by tasks.registering(Copy::class) {
        from(format.pdf.output.getting("document"))
        into("build/copy")
    }
    tasks.named("assemble") { dependsOn(copyTask) }
}



package de.infolektuell.gradle.typst.tasks

import de.infolektuell.gradle.typst.service.GithubClient
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@CacheableTask
abstract class DownloadTask @Inject constructor(private val problems: Problems) : DefaultTask() {
    @get:Input
    abstract val asset: Property<GithubClient.Asset>

    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    protected fun download() {
        val client = GithubClient()
        val latestTag = client.findLatestTag("typst", "typst")
        val asset = asset.get()
        if (asset.tag != latestTag) {
            problems.reporter.report(LATEST_RELEASE_ADVICE_ID) { builder ->
                builder
                    .severity(Severity.ADVICE)
                    .details("You're not using the latest Typst version.")
                    .solution("Consider to update your project to the latest release by setting typst.version to $latestTag in the plugin extension.")
            }
        }
        val target = target.get().asFile.toPath()
        Files.createDirectories(target.parent)
        val body = client.downloadAsset(asset)
        Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING)
    }

    companion object {
        val GROUP = ProblemGroup.create("de.infolektuell.typst.download", "Typst Downloading Problems")
        val LATEST_RELEASE_ADVICE_ID = ProblemId.create("latest-release", "Newer release detected", GROUP)
    }
}

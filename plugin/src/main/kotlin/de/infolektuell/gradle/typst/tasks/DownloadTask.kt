package de.infolektuell.gradle.typst.tasks

import de.infolektuell.gradle.typst.service.GithubClient
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class DownloadTask : DefaultTask() {
    @get:Input
    abstract val asset: Property<GithubClient.Asset>
    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    protected fun download() {
        val asset = asset.get()
        val target = target.get().asFile.toPath()
        val client = GithubClient()
        Files.createDirectories(target.parent)
        val body = client.downloadAsset(asset)
        Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING)
    }
}

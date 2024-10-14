package de.infolektuell.gradle.typst.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import javax.inject.Inject

@CacheableTask
abstract class DownloadTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    @get:Input
    val url: Property<URI> = objects.property(URI::class.java)
    @get:Internal
    val fileName: Provider<String> = url.map { it.path.replaceBeforeLast('/', "").trim('/') }
    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    protected fun download() {
        val url = url.get()
        val targetPath = target.get().asFile.toPath()
        Files.createDirectories(targetPath.parent)
        download(url, targetPath)
    }

    private fun download(source: URI, target: Path) {
        val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(source)
            .GET()
            .build()
        val response = client.send(request, ofInputStream())
        if (response.statusCode() != 200) throw GradleException("Downloading from $source failed with status code ${response.statusCode()}.")
        Files.copy(response.body(), target, StandardCopyOption.REPLACE_EXISTING)
    }
}

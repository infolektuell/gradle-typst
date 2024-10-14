package de.infolektuell.gradle.typst.providers

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.Duration

abstract class GithubLatestRelease : ValueSource<String, GithubLatestRelease.Parameters> {
  interface Parameters : ValueSourceParameters {
    val owner: Property<String>
    val repo: Property<String>
  }
    override fun obtain(): String {
        val owner = parameters.owner.get()
        val repo = parameters.repo.get()
        return latestGithubRelease(owner, repo)
  }
    companion object {
        fun latestGithubRelease(owner: String, repo: String): String {
            val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
            val source = URI.create("https://api.github.com/repos/${owner}/${repo}/releases/latest")
            val request: HttpRequest = HttpRequest.newBuilder()
                .uri(source)
                .GET()
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Accept", "application/vnd.github+json")
                .build()
            val response = client.send(request, ofString())
            if (response.statusCode() != 200) throw GradleException("Downloading from $source failed with status code ${response.statusCode()}.")
            val obj = JSONObject(response.body())
            return obj.getString("tag_name")
        }
    }
}

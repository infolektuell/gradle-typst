package de.infolektuell.gradle.typst.service

import org.gradle.api.GradleException
import org.json.JSONObject
import java.io.InputStream
import java.io.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofInputStream
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.Duration

class GithubClient {
    data class Asset(val owner: String, val repo: String, val tag: String, val filename: String) : Serializable {
        val url: URI get() = URI.create("https://github.com/$owner/$repo/releases/download/$tag/$filename")
    }

    fun downloadAsset(asset: Asset): InputStream {
        val client: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(asset.url)
            .GET()
            .build()
        val response = client.send(request, ofInputStream())
        if (response.statusCode() != 200) throw GradleException("Downloading from ${asset.url} failed with status code ${response.statusCode()}.")
        return response.body()
    }

    fun findLatestTag(owner: String, repo: String): String {
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

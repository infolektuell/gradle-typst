import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.nio.file.Files
import java.time.Duration
import java.util.*

tasks.register("prepareTypstReleases", PrepareGitHubReleases::class) {
    owner = "typst"
    repo = "typst"
    val resourceDir = layout.projectDirectory.dir("src/main/resources")
    target = resourceDir.file("typst-releases.properties")
}

abstract class PrepareGitHubReleases @Inject constructor(private val fileSystem: FileSystemOperations) : DefaultTask() {
    data class Release(
        val tagName: String,
        val assets: List<Asset>,
    )
    data class Asset(
        val url: URI,
        val name: String,
        val digest: String,
    )

    @get:Input
    abstract val owner: Property<String>
    @get:Input
    abstract val repo: Property<String>
    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    protected fun prepareReleases() {
        fileSystem.delete {
            delete(target)
        }
        val releases = listReleases(owner.get(), repo.get())
        val data = Properties()
        releases.forEach { r ->
            val base = "typst.${r.tagName}"
            r.assets.associateByNotNull { asset ->
                val archKey = if (asset.name.contains("aarch64")) "aarch64" else if (asset.name.contains("x86_64")) "x64" else null
                val osKey = if (asset.name.contains("apple")) "mac" else if (asset.name.contains("windows")) "windows" else if (asset.name.contains("linux")) "linux" else null
                val key = if (archKey != null && osKey != null) {
                    "$archKey.$osKey"
                } else null
                key
            }
                .forEach { (k, v) ->
                    data.setProperty("$base.$k.url", v.url.toString())
                    data.setProperty("$base.$k.name", v.name)
                    if (v.digest.isNotEmpty()) {
                        data.setProperty("$base.$k.digest", v.digest)
                    }
                }
        }

        val target = target.get().asFile.toPath()
        Files.createDirectories(target.parent)
        Files.newOutputStream(target).use { s ->
            data.store(s, "Typst releases data for downloading binaries")
        }
    }

    private fun listReleases(owner: String, repo: String): List<Release> {
        val baseUrl = URI.create("https://api.github.com/repos/${owner}/${repo}/releases")
        val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        val request =         HttpRequest.newBuilder()
            .GET()
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("Accept", "application/vnd.github+json")
            .uri(baseUrl).build()
        val response = client.send(request, ofString())
        if (response.statusCode() != 200) throw RuntimeException("Couldn't list the Typst releases")
        return createReleasesFromJson(JSONArray(response.body()))
    }

    private fun createReleasesFromJson(arr: JSONArray): List<Release> {
        val releases = mutableListOf<Release>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val prerelease = obj.optBoolean("prerelease", false)
            if (!prerelease) {
                val release = createReleaseFromJson(obj)
                releases.add(release)
            }
        }
        return releases
    }

    private fun createReleaseFromJson(json: JSONObject): Release {
        val tagName = json.getString("tag_name")
        val assetJson = json.getJSONArray("assets")
        val assets = mutableListOf<Asset>()
        for (i in 0 until assetJson.length()) {
            val obj = assetJson.getJSONObject(i)
            val url = obj.getString("url")
            val name = obj.getString("name")
            val digest = obj.optString("digest")
            val asset = Asset(URI.create(url), name, digest)
            assets.add(asset)
        }
        return Release(tagName, assets)
    }

// Source - https://stackoverflow.com/a
// Posted by LostMekka
// Retrieved 2025-11-07, License - CC BY-SA 4.0

    fun <T, K : Any> Iterable<T>.associateByNotNull(
        keySelector: (T) -> K?
    ): Map<K, T> = buildMap {
        for (item in this@associateByNotNull) {
            val key = keySelector(item) ?: continue
            this[key] = item
        }
    }
}

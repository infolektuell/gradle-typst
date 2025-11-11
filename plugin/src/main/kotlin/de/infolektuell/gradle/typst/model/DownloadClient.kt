package de.infolektuell.gradle.typst.model

import java.io.BufferedInputStream
import java.io.OutputStream
import java.io.Serializable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.time.Duration

/**
 * A client for downloading GitHub release assets
 * @constructor Creates a new client
 */
class DownloadClient {
    /** Indicates that the client couldn't download a given asset */
    class FailedDownloadException(message: String, err: Throwable? = null) : RuntimeException(message, err)
    /** Indicates that the integrity of a downloaded asset couldn't be verified with the given digest */
    class FailedIntegrityCheckException(message: String, err: Throwable? = null) : RuntimeException(message, err)

    /**
     * Data describing an asset
     *
     * An asset consists of an [url], a [name], and an optional [digest] which consist of an integrity verification algorithm and a checksum separated by a colon.
     *
     * @constructor Creates a new asset
     */
    data class Asset(val url: URI, val name: String, val digest: String?) : Serializable {
        companion object {
            const val serialVersionUID: Long = 1
        }
    }

    /** Checks if a given [asset] is already downloaded into a given [directory][dir] */
    fun exists(asset: Asset, dir: Path): Boolean {
        val (_, name, digest) = asset
        val target = dir.resolve(name)
        if (!Files.exists(target) || !Files.isRegularFile(target)) return false
        if (digest == null) return true
        val (algorithm, checksum) = digest.split(':')
        if (hashFile(algorithm, target) != checksum) {
            Files.deleteIfExists(target)
            return false
        }
        return true
    }

    /**
     * Downloads an [asset], checks its integrity if digest is available, and stores the file to a [target directory][dir].
     * @throws FailedDownloadException If the resource couldn't be downloaded
     * @throws FailedIntegrityCheckException If the expected and actual checksum of the downloaded file don't match.
     * @return The file path where the asset has been stored
     */
    fun download(asset: Asset, dir: Path): Path {
        val (url, name, digest) = asset
        val target = dir.resolve(name)
        if (exists(asset, dir)) return target
        val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .header("accept", "application/octet-stream")
            .build()
        val response = client.send(request, ofInputStream())
        if (response.statusCode() != 200) throw FailedDownloadException("Downloading from $url failed with status code ${response.statusCode()}.")
        Files.createDirectories(dir)
        if (digest != null) {
            val (algorithm, checksum) = digest.split(':')
            val md = MessageDigest.getInstance(algorithm)
            DigestInputStream(response.body(), md).use { s ->
                Files.copy(s, target, StandardCopyOption.REPLACE_EXISTING)
            }
            val calculatedChecksum = md.digest().toHexString()
            val isValid = calculatedChecksum == checksum
            if (!isValid) {
                Files.deleteIfExists(target)
                throw FailedIntegrityCheckException("Data integrity of downloaded file $url could not be verified, checksums do not match.")
            }
        } else {
            Files.copy(response.body(), target, StandardCopyOption.REPLACE_EXISTING)
        }
        return target
    }

    private fun hashFile(algorithm: String, file: Path): String {
        val md = MessageDigest.getInstance(algorithm)
        BufferedInputStream(Files.newInputStream(file)).use { s ->
            DigestOutputStream(OutputStream.nullOutputStream(), md).use { out ->
                s.transferTo(out)
            }
        }
        return md.digest().toHexString()
    }
}

package de.infolektuell.gradle.typst.model

import java.io.Serializable
import java.util.regex.Pattern

/** Indicates that a malformed version string couldn't be parsed into a Version object */
class VersionFormatException(str: String) : RuntimeException("Failed to parse version from $str, invalid semver string")

/**
 * Represents a release version that resembles to the semantic version standard, but without prerelease or build information
 */
data class Version(
    val major: Int, val minor: Int, val patch: Int
) : Serializable, Comparable<Version> {
    override fun toString() = "$major.$minor.$patch"
    override fun compareTo(other: Version): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)
        return 0
    }

    companion object {
        val pattern: Pattern by lazy { Pattern.compile("^[Vv]?(\\d+)\\.(\\d+)\\.(\\d+)$") }
        const val serialVersionUID: Long = 1

        /**
         * Returns a version parsed from a [semver string][str]
         *
         * @throws VersionFormatException
         */
        fun parse(str: String): Version {
            val matcher = pattern.matcher(str)
            if (!matcher.find()) throw VersionFormatException(str)
            val major = matcher.group(1).toInt()
            val minor = matcher.group(2).toInt()
            val patch = matcher.group(3).toInt()
            return Version(major, minor, patch)
        }

        /** Returns a version parsed from a [semver string][str], null if parsing fails */
        fun parseOrNull(str: String): Version? {
            return try {
                parse(str)
            } catch (_: VersionFormatException) {
                null
            }
        }
    }
}

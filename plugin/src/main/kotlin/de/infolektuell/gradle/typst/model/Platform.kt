package de.infolektuell.gradle.typst.model

import java.nio.file.Path

class Platform(val operatingSystem: OperatingSystem, val architecture: Architecture) {
    enum class OperatingSystem {
        WINDOWS, MAC, LINUX;

        val homeDir: Path get() = Path.of(System.getProperty("user.home"))

        val dataDir: Path
            get() {
                return when (this) {
                    WINDOWS -> Path.of(System.getenv("APPDATA"))
                    MAC -> homeDir.resolve("Library/Application Support")
                    LINUX -> homeDir.resolve(".local/share")
                }
            }

        val cacheDir: Path
            get() {
                return when (this) {
                    WINDOWS -> Path.of(System.getenv("LOCALAPPDATA"))
                    MAC -> homeDir.resolve("Library/Caches")
                    LINUX -> homeDir.resolve(".cache")
                }
            }

        companion object {
            fun create(value: String): OperatingSystem {
                return if (value.contains("Windows", true)) {
                    WINDOWS
                } else if (value.contains("Mac", true)) {
                    MAC
                } else {
                    LINUX
                }
            }
        }
    }

    enum class Architecture {
        AARCH64, X64;

        companion object {
            fun create(value: String): Architecture {
                return if (value.contains("aarch64", true)) {
                    AARCH64
                } else {
                    X64
                }
            }
        }
    }

    companion object {
        fun getCurrentPlatform(): Platform {
            val os = OperatingSystem.create(System.getProperty("os.name"))
            val arch = Architecture.create(System.getProperty("os.arch"))
            return Platform(os, arch)
        }
    }
}

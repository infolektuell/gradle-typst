package de.infolektuell.gradle.typst.service

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.nio.file.Path
import java.nio.file.Files

class TypstDataStore {
    enum class OS {
        LINUX, MACOS, WINDOWS;
        override fun toString(): String {
            return when(this) {
                LINUX -> "unknown-linux-musl.tar.xz"
                MACOS -> "apple-darwin.tar.xz"
                WINDOWS -> "pc-windows-msvc.zip"
            }
        }
    }
    enum class Arch {
        AARCH64, X64;
        override fun toString(): String {
            return when(this) {
                AARCH64 -> "aarch64"
                X64 -> "x86_64"
            }
        }
    }
    val os: OS get() {
        val currentOs = DefaultNativePlatform.getCurrentOperatingSystem()
        return if(currentOs.isLinux) {
            OS.LINUX
        } else if (currentOs.isMacOsX) {
            OS.MACOS
        } else {
            OS.WINDOWS
        }
    }
    val arch: Arch get() {
        val currentArch = DefaultNativePlatform.getCurrentArchitecture()
        return if (currentArch.isArm) {
            Arch.AARCH64
        } else {
            Arch.X64
        }
    }
    val dataDir: Path get() {
        val homeDir = Path.of(System.getProperty("user.home"))
        return when(os) {
            OS.LINUX -> homeDir.resolve(".local/share")
            OS.MACOS -> homeDir.resolve("Library/Application Support")
            OS.WINDOWS -> Path.of(System.getenv("APPDATA"))
        }
    }
    val cacheDir: Path get() {
        val homeDir = Path.of(System.getProperty("user.home"))
        return when(os) {
            OS.LINUX ->homeDir.resolve(".cache")
            OS.MACOS -> homeDir.resolve("Library/Caches")
            OS.WINDOWS -> Path.of(System.getenv("LOCALAPPDATA"))
        }
    }
    val packageDir: Path = dataDir.resolve("typst/packages")
    val packageCacheDir: Path = cacheDir.resolve("typst/packages")
    val hasPackages: Boolean get() = Files.exists(packageDir)
    fun asset(tag: String) = GithubClient.Asset("typst", "typst", tag, "typst-$arch-$os")
}

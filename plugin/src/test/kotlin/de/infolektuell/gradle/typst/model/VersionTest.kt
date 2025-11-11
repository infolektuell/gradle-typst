package de.infolektuell.gradle.typst.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VersionTest {
    @Test
    fun `Distinguishes correct and incorrect strings`() {
        assertNotNull(Version.parseOrNull("0.10.0"))
        assertNotNull(Version.parseOrNull("v0.10.0"))
        assertNull(Version.parseOrNull("vv0.10.0"))
    }

    @Test
    fun `Recognizes version numbers`() {
        val version = Version.parse("0.10.0")
        val (major, minor, patch) = version
        assertEquals(0, major)
        assertEquals(10, minor)
        assertEquals(0, patch)
    }

    @Test
    fun `Compares versions semantically`() {
        val lower = Version.parse("0.2.0")
        val middle = Version.parse("0.10.0")
        val higher = Version.parse("0.14.0")
        assertTrue(lower < higher)
        assertTrue(middle in lower..higher)
        assertFalse(lower in middle..higher)
        assertFalse(higher in lower ..< higher)
        assertTrue(higher in lower .. higher)
        assertTrue(lower in lower .. higher)
    }
}

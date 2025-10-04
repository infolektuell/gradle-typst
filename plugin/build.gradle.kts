plugins {
    kotlin("jvm") version "2.2.0"
    signing
    id("com.gradle.plugin-publish") version "1.3.1"
}

val releaseVersion = releaseVersion()
val releaseNotes = releaseNotes()
version = releaseVersion.get()

gradlePlugin {
    website = "https://github.com/infolektuell/gradle-typst"
    vcsUrl = "https://github.com/infolektuell/gradle-typst.git"
    plugins.register("typstPlugin") {
        id = "de.infolektuell.typst"
        displayName = "Typst Plugin"
        description = releaseNotes.get()
        tags = listOf("Typst", "PDF", "typesetting")
        implementationClass = "de.infolektuell.gradle.typst.GradleTypstPlugin"
    }
}

signing {
    // Get credentials from env variables for better CI compatibility
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.tukaani:xz:1.10")
    implementation("org.json:json:20250517")
    implementation("org.apache.pdfbox:pdfbox:3.0.5")
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}

fun releaseVersion(): Provider<String> {
    val releaseVersionFile = rootProject.layout.projectDirectory.file("release/version.txt")
    return providers.fileContents(releaseVersionFile).asText.map(String::trim)
}

fun releaseNotes(): Provider<String> {
    val releaseNotesFile = rootProject.layout.projectDirectory.file("release/changes.md")
    return providers.fileContents(releaseNotesFile).asText.map(String::trim)
}

# Gradle Typst Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.typst)](https://plugins.gradle.org/plugin/de.infolektuell.typst)

[Typst] is a new markup-based typesetting system that is designed to be as powerful as LaTeX while being much easier to learn and use.
A Typst document can be compiled from a single _.typ_ file, but a complex project can also contain many files, including data, images, and fonts.
The Typst compiler needs the correct file paths to find everything and compile such projects successfully.
This Gradle plugin offers a way to maintain such projects:

## Features

- [x] Compile multiple documents in parallel for faster builds
- [x] Incremental build: Edit files and rebuild only affected documents
- [x] Typst can either be automatically downloaded from GitHub releases, or use a local installation
- [x] Define multiple source sets in one project to produce variants of your content, e.g., versions for printing and web publishing
- [x] Track changes in locally installed Typst packages
- [x] Convert unsupported image formats to format supported by Typst (ImageMagick required)
- [x] Merge generated PDF files into one file using [PDFBox]
- [x] Works well with Gradle's [Configuration Cache] and [Build cache]

## Requirements

The plugin expects these tools being installed locally:

- [ImageMagick] for image conversion (Optional)

## Usage

### Plugin setup

After creating a new Gradle project with `gradle init`, the plugin needs to be configured in _build.gradle.kts_:

```gradle kotlin dsl
plugins {
    // Good practice to have some standard tasks like clean, assemble, build
    id("base")
    // Apply the Typst plugin
    id("de.infolektuell.typst") version "0.3.0"
}

// The release tag for the Typst version to be used, defaults to latest stable release on GitHub
typst.version = "v0.12.0"
```

### Adding sources

A source set is a directory in your project under _src_ that may contain subfolders for Typst files, data, images, and fonts.
The Typst files that should be treated as input documents to be compiled must explicitly be configured.
There can be one or as many of them as needed.
Having multiple source sets can be useful if multiple variants of similar content should be produced, especially for data-driven documents.
Otherwise, a single source set is sufficient.
So let's add two of them in _build.gradle.kts_:

```gradle kotlin dsl
// The source sets container
typst.sourceSets {
    // Sources for documents intended for web publishing in src/web folder
    val web by registering {
        // The files to compile (without .typ extension)
        documents = listOf("frontmatter", "main", "appendix", "backmatter")
        // Setting this creates a merged PDF file from the documents list
        merged = "thesis-web-$version"
        // Values set in this map are passed to Typst as --input options
        inputs.put("version", version.toString())
    }

    // Sources for documents intended for printing in src/printing folder
    val printing by registering {
        documents = listOf("frontmatter", "main", "poster", "appendix", "backmatter")
    }
}
```

In a source set folder, these subfolders are watched for changes:

- _data_: Files in YAML, TOML or JSON format
- _fonts_: Additional font files for your documents
- _images_: Image files included in your documents
- _typst_: Typst files, can be documents or contain declarations for importing

Running `gradlew build` now will compile all documents.

### Shared sources

If multiple source sets have many files in common, they could go into their own source set without documents.
The source sets using these files can depend on this new shared source set.

```gradle kotlin dsl
typst.sourceSets {
    // Sources used by other source sets in src/shared
    val shared by registering

    val printing by registering {
        // Shared sources are also watched when printing documents are compiled
        addSourceSet(shared)
    }
}
```

### Images

Image files in _src/<source set>/images_ are copied to _build/generated/typst/images_.
If the format ist not supported by Typst, they are converted to png before copying.
Typst runs after image processing, so the images can be referenced by their path in Typst files.
Typst receives the project directory as root (not the root project), so absolute import paths start with _/src/_.

### Fonts

A document receives the fonts subfolders of their source set and added shared source sets as font paths.
Since version 0.2.0 of this plugin, system fonts are ignored by default for higher reproducibility.
If a Typst version below 0.12.0 is in use or if system fonts should be considered, this must be turned off per configuration:

```gradle kotlin dsl
import de.infolektuell.gradle.typst.tasks.TypstCompileTask

// Configure all typst tasks
tasks.withType(TypstCompileTask::class) {
    // Override the convention (false by default)
    useSystemFonts = true
}
```

### Creation date

For better build reproducibility, Typst accepts a fixed creation date in UNIX timestamp format.
See [SOURCE_DATE_EPOCH specification] for a format description.
If no timestamp is set, it is determined by Typst.

```gradle kotlin dsl
import de.infolektuell.gradle.typst.providers.GitCommitDateValueSource

// Use the included utility to get a timestamp from git commit
val timestamp = providers.of(GitCommitDateValueSource::class) {
    parameters {
        revision = "main"
    }
}

// Configure the Typst extension with this timestamp (eagerly for configuration cache compatibility)
typst.creationTimestamp = timestamp.get()
```

## License

[MIT License](LICENSE.txt)

[typst]: https://typst.app/
[configuration cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[build cache]: https://docs.gradle.org/current/userguide/build_cache.html
[imagemagick]: https://imagemagick.org/
[pdfbox]: https://pdfbox.apache.org/
[SOURCE_DATE_EPOCH specification]: https://reproducible-builds.org/specs/source-date-epoch/

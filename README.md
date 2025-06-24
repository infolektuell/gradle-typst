# Gradle Typst Plugin

[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/de.infolektuell.typst)](https://plugins.gradle.org/plugin/de.infolektuell.typst)

[Typst] is a new markup-based typesetting system designed to be as powerful as LaTeX while being much easier to learn and use.
A Typst document can be compiled from a single _.typ_ file, but a complex project can also contain many files, including data, images, and fonts.
The Typst compiler needs the correct file paths to find everything and compile such projects successfully.
This Gradle plugin offers a way to maintain such projects:

## Features

- [x] Compile multiple documents in parallel for faster builds
- [x] Generate all output formats supported by Typst (PDF, PNG, and SVG)
- [x] Incremental build: Edit files and rebuild only affected documents
- [x] Typst can either be automatically downloaded from GitHub releases or use a local installation
- [x] Define multiple source sets in one project to produce variants of your content, e.g., versions for printing and web publishing
- [x] Track changes in locally installed Typst packages
- [x] Convert unsupported image formats to format supported by Typst (ImageMagick required)
- [x] Merge generated PDF files into one file using [PDFBox]
- [x] Works well with Gradle's [Configuration Cache] and [Build cache]
- [x] Runs on JVM 17 and above

## Requirements

The plugin expects these locally installed tools:

- Java 17 or above
- [ImageMagick] for image conversion (Optional)

## Usage

### Plugin setup

After creating a new Gradle project with `gradle init`, the plugin needs to be configured in _build.gradle.kts_:

```gradle kotlin dsl
plugins {
    id("base") // some standard tasks like clean, assemble, build
    id("de.infolektuell.typst") version "0.6.2" // Apply the Typst plugin
}

typst.version = "v0.13.1" // The release tag for the Typst version to be used, defaults to latest stable release on GitHub
```

### Adding sources

A source set is a directory in your project under _src_ that may contain Typst files, data, images, fonts, etc.
It can depend on other source sets, e.g., shared templates.
So there are two kinds of source sets:

- Primary source sets contain documents to be compiled.
- Secondary or transient source sets have no documents, but they contain material which is used by primary source sets.

Documents in a primary source set are recompiled if files in one of its secondary source sets have changed.

Having multiple source sets offers flexibility and build performance at once.
It makes use of parallel compilation and incrementally re-compiles those documents where the source set files have changed.

- You can, e.g., have multiple variants of the same documents for web publishing and printing.
- You can have many document groups that follow a similar schema. For video production, each video could be a source set and requires a script, slides, and a thumbnail.

The Typst documents must explicitly be configured in primary source sets.
So let's add a source set to _build.gradle.kts_:

```gradle kotlin dsl
typst.sourceSets {
    // Sources in src/main
    val main by registering {
        // The files to compile (without .typ extension) in src/main/typst
        documents = listOf("document") // src/main/typst/document.typst
    }
```

The plugin will search for Typst files under _src/main/typst/_.
Next, create _document.typ_ in this directory.
Running `gradlew build` now will compile your _document.typ_ file into _build/typst/<source set>/pdf/document.pdf_.

This example configures a multi-source-set project:

```gradle kotlin dsl
typst.sourceSets {
    val shared by registering // Sources in src/shared

    // Sources in src/web
    val web by registering {
        addSourceSet(shared) // Depends on shared files
        // The files to compile (without .typ extension)
        documents = listOf("frontmatter", "main", "appendix", "backmatter")
        // Values set in this map are passed to Typst as --input options
        inputs.put("version", version.toString())
    }

    // Sources for documents intended for printing in src/printing folder
    val printing by registering {
        addSourceSet(shared) // Depends on shared files
        documents = listOf("frontmatter", "main", "poster", "appendix", "backmatter")
    }
}
```

### Output formats

Currently, Typst can output a document as PDF or as a series of images in PNG or SVG format.
HTML will be supported by this plugin when it becomes a stable feature.
The desired output options can be configured per source set, e.g., PDF for printing and PNG for web publishing.

```gradle kotlin dsl
typst.sourceSets {
    val web by registering {
        documents = listOf("frontmatter", "main", "appendix", "backmatter")
        format {
            // The PNG format is right
            png {
                enabled = true
                // Customized resolution (144 by default)
                ppi = 72
            }
            // Disable the PDF format which is active by default
            pdf.enabled = false
        }
    }

    val printing by registering {
        documents = listOf("frontmatter", "main", "poster", "appendix", "backmatter")
        format {
            // Setting this creates a merged PDF file from the documents list
            pdf.merged = "thesis-$version"
            // Typst can produce PDF which is compliant to 1.7, a-2b, or a-3b
            pdf.standard = "a-3b"
        }
    }
}
```

PDF merging is done via [Pdfbox].

### Images

In every primary and secondary source set, a special folder is searched for images (by default src/<sourceSetName>/images).
This folder is customizable in the DSL via `images.source` property in a source set definition.
Before Typst runs, image files in that folder are copied to a folder in the build directory which is also customizable.
If the format is not supported by Typst, they are converted to png using [Imagemagick] before copying.
Alternately, there are some community packages like [Grayness] for more image formats.

The paths relative to the Typst root path are passed to documents as input variables named `<sourceSetName>-converted-images`.
This enables loading of images in a build-system-agnostic manner, the Typst documents don't need to know about Gradle's build directory.
In this example document, a document uses an image from a common source set.

```typst
#let commitHash = sys.inputs.at("gitHash", default: "")
#let convertedImages = sys.inputs.at("common-converted-images")

= Test document version #commitHash

#image(convertedImages + "/test-image.png")

#lorem(5000)
```

If this should be compiled without Gradle, the image paths must be supplied as input variables and the document remains untouched.

### Fonts

In every primary and secondary source set, a certain subfolder is searched for fonts (by default _src/<sourceSetName>/fonts_).
This is customizable in the source set DSL.
The Typst compiler looks for font files in the fonts directories of a primary and its secondary source sets.

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

For better build reproducibility, Typst accepts a fixed creation date in UNIX timestamp format which can be set in the plugin's `typst` DSL.
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

### Using a local executable

The given Typst version is downloaded into the build folder by default, but using a locally installed Typst binary is possible too.
Anyway, the version should be specified explicitly.

```gradle kotlin dsl
typst {
    version = "0.13.1"
    executable = project.layout.file("/usr/local/bin/typst")
}
```

### Local packages

Typst 0.12.0 added a CLI option to pass the path where local packages are stored.
This plugin sets this explicitly to Typst's platform-dependent convention, so both are working with the same files.
To use an older version of Typst, you have to opt out of this behavior.

```gradle kotlin dsl
import de.infolektuell.gradle.typst.tasks.TypstCompileTask

// Configure all typst tasks
tasks.withType(TypstCompileTask::class) {
    // Unset the package path
    packagePath.unset()
    // Optionally add the local packages folder from the typst extension to the source set to keep change tracking
    sourceSets.register("main") {
        typst.add(localPackages)
    }
}
```

### Excluding files from watching

If the project contains many or large files that slow down the build, they can be excluded via `excludePatterns` in the DSL (top-level or per source set).

## License

[MIT License](LICENSE.txt)

[typst]: https://typst.app/
[configuration cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[build cache]: https://docs.gradle.org/current/userguide/build_cache.html
[imagemagick]: https://imagemagick.org/
[grayness]: https://typst.app/universe/package/grayness/
[pdfbox]: https://pdfbox.apache.org/
[SOURCE_DATE_EPOCH specification]: https://reproducible-builds.org/specs/source-date-epoch/

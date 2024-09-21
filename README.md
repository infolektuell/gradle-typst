# Gradle Typst Plugin

[Typst] is a new markup-based typesetting system that is designed to be as powerful as LaTeX while being much easier to learn and use.
A Typst document can be compiled from a single _.typ_ file, but a complex project can also contain many files, including data, images, and fonts.
The Typst compiler needs the correct file paths to find everything and compile such projects successfully.
This Gradle plugin offers a way to maintain such projects:

## Features

- [x] Define multiple source sets in one project, e.g., versions for printing and web publishing
- [x] Compile multiple documents in parallel for faster builds
- [x] Can track changes in your local packages directory
- [x] Convert unsupported image formats to format supported by Typst (ImageMagick required)
- [x] Merge generated PDF files using [PDFBox]
- [x] Compatible with [Configuration Cache]

## Requirements

The plugin expects these tools being installed locally:

- [Typst]
- Optional for image conversion: [ImageMagick]

## Usage

A source set is a directory in your project under _src_.
There can be as many of them as you need.
Every source set tracks changes in the following subdirectories:

- _data_: Files in YAML, TOML or JSON format
- _fonts_: Additional font files for your documents
- _images_: Image files included in your documents
- _typst_: Typst files, can be documents or contain declarations for importing

In the _build.gradle.kts_ next to _src_, the plugin must be configured:

```
plugins {
    // Good to have some standard tasks like clean
    id("base")
    // Apply the Typst plugin
    id("de.infolektuell.typst") version "0.1.0"
}

// Define your source sets
typst.sourceSets {
    // Shared files under src/common/
    val common by registering {
        // Do your documents use local packages?
        useLocalPackages()
    }

    // Preview version under src/preview, run gradlew previewTypst to build
    val preview by registering {
        // Entry points to be compiled by Typst
        documents.addAll("frontmatter", "thesis")
  // You can merge all generated documents into one PDF
  merged = "thesis-preview"
  // Track changes in common files
  addSourceSet(common)
}

    // Production version under src/production, run gradlew productionTypst to build
    val production by registering {
        // Entry points to be compiled by Typst
        documents.addAll("frontmatter", "thesis", "poster")
  // Track changes in common files
  addSourceSet(common)
}
```

Running `gradlew build` compiles all documents.

## License

[MIT License](LICENSE.txt)

[typst]: https://typst.app/
[configuration cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[imagemagick]: https://imagemagick.org/
[pdfbox]: https://pdfbox.apache.org/

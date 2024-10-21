# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `localPackages` property in `TypstExtension` which is configured with a platform-dependent convention where local Typst packages are installed.
- `packagePath` can be set for `TypstCompileTask` which lets Gradle track changes in local package files and Typst to look for packages in the given directory. This is configured with `localPackages` from the Typst extension by default.
- Typst source set got a format section where the output formats supported by Typst can be enabled and configured. So the documents of a source set can be output in multiple formats at once.

### Changed

- `TypstSourceSet.merged` was moved to `TypstSourceSet.format.pdf.merged`.

### Removed

- `useLocalPackages` function in `TypstSourceSet`

## [0.3.0] - 2024-10-19

### Added

- `TypstCompileTask` now takes creation date as an optional task input, also configurable via extension. See [SOURCE_DATE_EPOCH specification] for more information.
- Utility value source that queries the date from a git revision as a UNIX timestamp that can be passed to `typst.creationTimestamp` extension property

[SOURCE_DATE_EPOCH specification]: https://reproducible-builds.org/specs/source-date-epoch/

## [0.2.0] - 2024-10-19

### Changed

- System fonts are ignored by default. For Typst versions older than 0.12.0 to run successfully, the `useSystemFonts` property of `TypstCompileTask` must be set to `true`.

## [0.1.0] - 2024-10-15

### Added

- Task to compile multiple Typst documents in parallel
- Task to merge multiple PDF files
- Task to convert images using ImageMagick
- Tasks to download and install Typst from GitHub releases
- DSL extension to configure Typst version and multiple source sets
- Some auxiliary value sources for git has and latest GitHub release

[unreleased]: https://github.com/infolektuell/gradle-typst/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/infolektuell/gradle-typst/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/infolektuell/gradle-typst/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/infolektuell/gradle-typst/releases/tag/v0.1.0

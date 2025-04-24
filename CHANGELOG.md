# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.5.0] - 2025-04-24

### Added

- Option to set PDF standard

## [0.4.2] - 2025-01-09

### Fixed

- The plugin now requires at least JVM 17 to run instead of JVM 22. This solves some compatibility problems when the JVM running Gradle cannot be upgraded.

## [0.4.1] - 2024-10-31

### Fixed

- If a task input or output is a collection property (list/map/set), additional items can be manually added during configuration instead of just replacing the convention.

## [0.4.0] - 2024-10-22

### Added

- Support for generating documents of a source set in multiple output formats (PDF, PNG, and SVG).
- Target file names in Typst tasks became a task input.
- Typst tasks have optional `packagePath` and `packageCachePath` inputs. The plugin tries to set them if possible.
- `localPackages` property in `TypstExtension` which is configured with a platform-dependent convention where local Typst packages are installed.

### Changed

- `TypstSourceSet.merged` was moved to PDF output configuration.
- Tracking files in local packages and passing to Typst CLI is opt-out now and must be disabled for older Typst versions. Package directory is tracked in Gradle only if it exists.

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

[unreleased]: https://github.com/infolektuell/gradle-typst/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/infolektuell/gradle-typst/compare/v0.4.2...v0.5.0
[0.4.2]: https://github.com/infolektuell/gradle-typst/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/infolektuell/gradle-typst/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/infolektuell/gradle-typst/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/infolektuell/gradle-typst/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/infolektuell/gradle-typst/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/infolektuell/gradle-typst/releases/tag/v0.1.0

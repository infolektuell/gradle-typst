# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Every source set in the DSL now provides the names for its related tasks to facilitade dependency declaration.

### Changed

- Downloaded and extracted Typst distributions are copied into a temporary directory instead of a build directory.

### Fixed

- Typst compilation tasks depend on converted images from inherited source sets.

## [0.6.1] - 2025-05-26

### Added

- CI Validates plugin on Linux and Windows.

### Fixed

- Downloaded Typst executable is also found under Windows.

## [0.6.0] - 2025-05-25

### Added

- The paths for generated images of a source set are forwarded to Typst via input variables.
  The paths are relativized to the passed root directory, so the images can be loaded by Typst in a build-system-agnostic manner.
- A source set inherits (extends) input variables from inherited source sets.
- A separate GitHub client to find the latest release tag and download assets
- A Typst business model for platform-specific data like asset file conventions, package and cache locations etc.

### Changed

- Source set inheritance is not managed via lists of directories anymore.
  The source set objects are domain objects and directly added to the `includes` set of the inheriting source set.
  Using the `addSourceSet` method in build scripts remains working as usual.
- Instead of prescribing certain kinds of subdirectories in a source set, its location can be set via `root` property.
  This folder will be watched for changes, but files can be excluded from watching via `excludePatterns`.
- The `images` property in the source set DSL was replaced with a nested section where the paths to source and generated images can be customized.
- `TypstCompileTask` and DSL extension use `executable` file property for the location of the Typst binary.
- `TypstCompileTask` uses a `ConfigurableFileCollection` named `includes` to track all files that could be part of a Typst document. Font paths are tracked separately.
- The download task now gets an asset from the GitHub client instead of a URI and uses the GitHub client for downloading.

### Removed

- The `data` and `typst` properties in the source set DSL
- The GithubLatestRelease value source is not needed anymore, the plugin uses the GitHub client.
- The nested `sources` property from `TypstCompileTask`
- The `compiler` property from `TypstCompileTask` and `TypstExtension`

### Fixed

- The plugin properly reacts to the `base` plugin without depending on plugin application order.

## [0.5.0] - 2025-04-24

### Added

- Option to set the PDF standard

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
- Some auxiliary value sources to retrieve the git commit hash and the latest GitHub release

[unreleased]: https://github.com/infolektuell/gradle-typst/compare/v0.6.1...HEAD
[0.6.1]: https://github.com/infolektuell/gradle-typst/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/infolektuell/gradle-typst/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/infolektuell/gradle-typst/compare/v0.4.2...v0.5.0
[0.4.2]: https://github.com/infolektuell/gradle-typst/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/infolektuell/gradle-typst/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/infolektuell/gradle-typst/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/infolektuell/gradle-typst/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/infolektuell/gradle-typst/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/infolektuell/gradle-typst/releases/tag/v0.1.0

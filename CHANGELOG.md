# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
[unreleased]: https://github.com/infolektuell/gradle-typst/compare/v0.8.0...HEAD

## [0.8.0] - 2025-11-12
[0.8.0]: https://github.com/infolektuell/gradle-typst/compare/v0.7.1...v0.8.0

### Added

- Because Typst is adding support for more image formats, image formats that don't need to be converted become configurable per source set.
- Output format for converted images is configurable per source set.
- Errors from executing command line tools are handled more gracefully via Gradle's problem reporting API:
  - If imagemagick couldn't be found, an error is reported, but the build doesn't fail and all images will be copied instead of converted.

### Changed

- If no Typst version is configured in the build, the plugin uses a hardcoded Typst version instead of the latest release.
  This improves build reproducibility and stability (no network request during configuration and no dynamically changing Typst version).
  If the latest release differs from the configured one, this is reported without failing the build.
  The plugin's version convention might be updated when new Typst releases are available.
- Conventions have been updated:
  - Uses Typst v0.14.0
  - Includes webp and pdf in the supported image formats

## [0.7.1] - 2025-07-06
[0.7.1]: https://github.com/infolektuell/gradle-typst/compare/v0.7.0...v0.7.1

### Fixed

- The Typst outputs carry their task dependency.

## [0.7.0] - 2025-07-04
[0.7.0]: https://github.com/infolektuell/gradle-typst/compare/v0.6.2...v0.7.0

### Added

- A filename template can be supplied in the output format DSL for png or svg.
- The DSL extension exposes convenience properties to access the Typst output files per document.
- Output directories for png and svg are cleaned before compilation to avoid stale files.

### Changed

- If documents are compiled to png or svg output, a document-specific subdirectory is created in the respective output directory.

### Removed

- In `TypstCompileTask` the `compiled` provider has been removed, because it contains non-existing files for png and svg formats.

## [0.6.2] - 2025-06-24
[0.6.2]: https://github.com/infolektuell/gradle-typst/compare/v0.6.1...v0.6.2

### Added

- Every source set in the DSL now provides the names for its related tasks to facilitate dependency declaration.

### Changed

- Downloaded and extracted Typst distributions are copied into a temporary directory instead of a build directory.

### Fixed

- Typst compilation tasks depend on converted images from inherited source sets.

## [0.6.1] - 2025-05-26
[0.6.1]: https://github.com/infolektuell/gradle-typst/compare/v0.6.0...v0.6.1

### Added

- CI Validates plugin on Linux and Windows.

### Fixed

- Downloaded Typst executable is also found under Windows.

## [0.6.0] - 2025-05-25
[0.6.0]: https://github.com/infolektuell/gradle-typst/compare/v0.5.0...v0.6.0

### Added

- The paths for generated images of a source set are forwarded to Typst via input variables.
  The paths are relativized to the passed root directory, so the images can be loaded by Typst in a build-system-agnostic manner.
- A source set inherits (extends) input variables from inherited source sets.
- A separate GitHub client to find the latest release tag and download assets
- A Typst business model for platform-specific data like asset file conventions, package and cache locations, etc.

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
[0.5.0]: https://github.com/infolektuell/gradle-typst/compare/v0.4.2...v0.5.0

### Added

- Option to set the PDF standard

## [0.4.2] - 2025-01-09
[0.4.2]: https://github.com/infolektuell/gradle-typst/compare/v0.4.1...v0.4.2

### Fixed

- The plugin now requires at least JVM 17 to run instead of JVM 22. This solves some compatibility problems when the JVM running Gradle cannot be upgraded.

## [0.4.1] - 2024-10-31
[0.4.1]: https://github.com/infolektuell/gradle-typst/compare/v0.4.0...v0.4.1

### Fixed

- If a task input or output is a collection property (list/map/set), additional items can be manually added during configuration instead of just replacing the convention.

## [0.4.0] - 2024-10-22
[0.4.0]: https://github.com/infolektuell/gradle-typst/compare/v0.3.0...v0.4.0

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
[0.3.0]: https://github.com/infolektuell/gradle-typst/compare/v0.2.0...v0.3.0

### Added

- `TypstCompileTask` now takes creation date as an optional task input, also configurable via extension. See [SOURCE_DATE_EPOCH specification] for more information.
- Utility value source that queries the date from a git revision as a UNIX timestamp that can be passed to `typst.creationTimestamp` extension property

[SOURCE_DATE_EPOCH specification]: https://reproducible-builds.org/specs/source-date-epoch/

## [0.2.0] - 2024-10-19
[0.2.0]: https://github.com/infolektuell/gradle-typst/compare/v0.1.0...v0.2.0

### Changed

- System fonts are ignored by default. For Typst versions older than 0.12.0 to run successfully, the `useSystemFonts` property of `TypstCompileTask` must be set to `true`.

## [0.1.0] - 2024-10-15
[0.1.0]: https://github.com/infolektuell/gradle-typst/releases/tag/v0.1.0

### Added

- Task to compile multiple Typst documents in parallel
- Task to merge multiple PDF files
- Task to convert images using ImageMagick
- Tasks to download and install Typst from GitHub releases
- DSL extension to configure Typst version and multiple source sets
- Some auxiliary value sources to retrieve the git commit hash and the latest GitHub release

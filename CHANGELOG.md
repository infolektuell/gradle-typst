# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- System fonts are ignored by default. For Typst versions older than 0.12.0 to run successfully, the `useSystemfonts` property of `TypstCompileTask` must be set to `false`.

## [0.1.0] - 2024-10-15

### Added

- Task to compile multiple Typst documents in parallel
- Task to merge multiple PDF files
- Task to convert images using ImageMagick
- Tasks to download and install Typst from GitHub releases
- DSL extension to configure Typst version and multiple source sets
- Some auxiliary value sources for git has and latest GitHub release

[unreleased]: https://github.com/infolektuell/gradle-jextract/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/infolektuell/gradle-jextract/releases/tag/v0.1.0

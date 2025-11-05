---
title: Introduction
description: The features offered by the Gradle Typst plugin
sidebar:
  order: 1
---

[Typst] is a modern markup-based typesetting system for writing beautiful and professionally-looking documents.
If you want to become productive, focused and efficient, and if your results should be convincing, Typst is the right tool for you.
Visit the [Typst homepage][typst] in order to learn how it works and how it compares to alternatives like MS Word, LaTeX, or Google Docs.
The docs and tutorial are well-written and helpful.

## Who should use Typst with Gradle?

I made this because I wanted to automate my complex Typst projects.
Academic projects are a typical use-case, but I also used it, e.g., for a [german card game](https://propter.app) that consists of a production version, a downloadable preview version for printing at home, and a website.
The cards are fed from data, the production version contains images, and certain fonts are used that I wanted to be included in the project, so the fonts are not required to be installed on the computer.
the project had to be completely self-contained.

You should use this, if …

- … your project grew in complexity: multiple input and output documents, input data, custom fonts, image conversion, multiple output formats.
- … you already started to write shell scripts to run the Typst compiler on your project files. Even a single line of shell script is a reason for switching to Gradle and say goodbye to shell or python scripts.
- … you don't want to rely on cloud services but want your project to be portable and collaboration-friendly.
- … you want to work with established open source tools that won't disappear.

## What is Gradle?

[Gradle] is a build automation tool that can deal with quite complex projects.
Non-developers could imagine it as an alternative to shell scripts, but more stable, robust, secure, and convenient.

- Shell scripts become Gradle tasks. What sets them apart is that they have clearly pre-defined inputs and outputs. Thus, Gradle can skip running them, if no inputs didn't change.
- Plugins extend Gradle's capabilities and add more tasks to use more compilers and tools.

## Features

This plugin was designed for complex project scenarios, build performance, and convenience.

- Automatically downloads and installs the required Typst version either from GitHub releases or uses a local installation.
- Compiles multiple documents to multiple formats in parallel for faster builds.
- Tracks changes in project files and locally installed Typst packages, re-builds only if changes were detected.
- Lets you define multiple source sets in one project to produce variants of your content, e.g., versions for printing and web publishing.
- Converts unsupported image formats to format supported by Typst (ImageMagick required).
- Can merge generated PDF files into one file.
- Was written with [Gradle's best practices](https://docs.gradle.org/current/userguide/best_practices.html) in mind.

## Requirements

Gradle is the only build tool that doesn't need to be installed itself.
A typical Gradle project contains a small helper program (wrapper) that downloads and bootstraps the correct Gradle version.
So others don't need to think about whether they have a compatible Gradle and Typst version to build your project.
But it expects JVM 17 to be installed.
If you need the image conversion feature, [ImageMagick] is required, too.

[typst]: https://typst.app/
[gradle]: https://docs.gradle.org/current/userguide/userguide.html
[imagemagick]: https://imagemagick.org/

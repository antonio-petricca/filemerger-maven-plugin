# filemerger-maven-plugin

A plugin to load and merge text/code/configuration/binary external files into another one by placeholders.

The **Tester** module is a self explanatory example:

- [`tester/pom.xml`](tester/pom.xml): the plugin example configuration.
- [`template-file.txt`](tester/src/main/assets/template-file.txt): the resource template to be merged with external files.
- [`tester/src/main/assets/`](tester/src/main/assets): the template and the external files folder.

---

[![Github CI](https://github.com/antonio-petricca/filemerger-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/antonio-petricca/filemerger-maven-plugin/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.antonio-petricca/filemerger-maven-plugin)](https://central.sonatype.com/artifact/io.github.antonio-petricca/filemerger-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://img.shields.io/github/license/apache/maven.svg?label=License)

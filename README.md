# filemerger-maven-plugin

A plugin to load and merge text/code/configuration external files into another one by placeholders.

The **Tester** module is a self explanatory example:

- [`tester/pom.xml`](tester/pom.xml): the plugin example configuration.
- [`tester/src/main/resources/tester-resource.txt`](tester/src/main/resources/tester-resource.txt): the resource template to be merged with external files.
- [`tester/src/main/assets/`](tester/src/main/assets): the external files folder.

---

![Maven CI](https://github.com/antonio-petricca/filemerger-maven-plugin/workflows/maven-ci/badge.svg)

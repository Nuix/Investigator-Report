Investigator Report
===================

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0) ![This script was last tested in Nuix 8.6](https://img.shields.io/badge/Script%20Tested%20in%20Nuix-8.6-green.svg)

View the GitHub project [here](https://github.com/Nuix/Investigator-Report) or download the latest release [here](https://github.com/Nuix/Investigator-Report/releases).

# Overview

**Written By:** Jason Wells

This script builds an offline HTML report based on data exported from a Nuix case with the intent of providing a means to review some details of this data with only a browser.

# Getting Started

## Setup

Begin by downloading the latest release of this code.  Extract the contents of the archive into your Nuix scripts directory.  In Windows the script directory is likely going to be either of the following:

- `%appdata%\Nuix\Scripts` - User level script directory
- `%programdata%\Nuix\Scripts` - System level script directory

## Building from Source

This script relies on 2 JAR files:

- `Nx.jar`: Source code (or already built JAR file) can be found in this repository: https://github.com/Nuix/Nx
- `InvestigatorReportGUI.jar`: Source code can be found in this respository

To build `InvestigatorReportGUI.jar`, load the source files into your IDE of choice.  Make sure the build path includes `\lib\*.jar` from the Nuix install directory.  For example, if you have Nuix 7.4 installed, include all JAR files located in `C:\Program Files\Nuix\Nuix 8.6\lib` in you build path.

# License

```
Copyright 2020 Nuix

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
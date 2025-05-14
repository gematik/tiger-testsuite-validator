<img alt="gematik logo" align="right" width="250" height="47" src="doc/images/Gematik_Logo_Flag.png"/> <br/> 

# Tiger-Testsuite-Validator

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
       <ul>
        <li><a href="#release-notes">Release Notes</a></li>
      </ul>
	</li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About The Project
About The Project: 
The tiger testsuite validator is a library that allows validating a given set of feature files against a zipped serenity report. 
It checks whether all mandatory scenarios and test steps are performed successfully and also if any optional scenario failed.

### Release Notes
See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

## Getting Started
Not provided as it is used only internally at gematik.

### Prerequisites
Not provided as it is used only internally at gematik.

### Installation
Not provided as it is used only internally at gematik.

## Usage
Not provided as it is used only internally at gematik.

### Constraints
Feature file names must be unique in the suite, so having two files in different folders with the same name is NOT supported.

Datatables are not checked for unmodified values in reports.

@Mandatory and @Optional Tags are to be assigned to specific Scenarios. If @Mandatory is added to a feature, then all Scenarios are marked as @Mandatory implicitly. Scenarios marked explicitly with @Optional stay optional though.

Annotations in Example sections are ignored for the validity check.

@OptGroup:XXXXXXX allows grouping optional scenarios,
meaning if any of the opt group scenarios are found in the report, all must be executed and green.
You may spread the opt group tags to multiple files
but be aware that filtering of execution does NOT apply to the validator,
so if you perform only a subset of scenarios (due to additional tag filtering),
the validator rightfully will reject the report as missing some scenarios.
The workaround is to "group"
such scenarios in separate files and provide multiple feature bundle files on Titus.

As of now,
executing a scenario that has two opt groups automatically enforces both groups to be checked;
it would be also possible to change this
so that only if a scenario with an explicit ONE opt group tag is executed, 
then the opt group will be checked.
Not sure as of now which way is the one we will need in the future. 

## License

Copyright [2024] gematik GmbH

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.

Unless required by applicable law the software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.

The software is the result of research and development activities, therefore not necessarily quality assured and without the character of a liable product. For this reason, gematik does not provide any support or other user assistance (unless otherwise stated in individual cases and without justification of a legal obligation). Furthermore, there is no claim to further development and adaptation of the results to a more current state of the art.

Gematik may remove published results temporarily or permanently from the place of publication at any time without prior notice or justification.

## Contact
Not provided as it is used only internally at gematik.

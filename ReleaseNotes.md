# Release 1.1.4

TGRTVAL-4: Features annotated with @Mandatory did not cause validator to fail if scenarios where missing in report

# Release 1.1.2

TGRTVAL-3: Introducing support for @OptGroup:XXXXX tags in test suites. If any scenario with an opt group tag is foun din the report, all scenarios from the feature bundle with matching opt group tags must be found in the report too.

# Release 1.1.1

TGRTVAL-2: detecting and rejecting manually Mac OS packed zip archives, which contains special __MACOSX folders that mess up the validator.
TGRTVAL-1: do not allow empty report zip archives as a valid report

# Release 1.1.0

TITUS-1320: zip archive now should also contain the pom.xml file.
The pom.xml project version value must match the bundled version.
For this a new method has been added 

``` [java]
RoportValidator.parseTitusReport(ZipInputStream reportStream, 
        Map<String, String> featureFiles, String version)
```

# Release 1.0.6

- exceptions are now end-user-readable (and in german)
- test coverage improved
- added copyright headers

# Release 1.0.1

- release on github
- adding licenses, ReleaseNotes, security, etc

# Release 1.0.0

- initial version

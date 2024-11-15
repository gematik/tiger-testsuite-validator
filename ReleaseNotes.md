# Release 1.1.1

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

# Alfred - Connector - SDK

This project is a SDK for other projects, enabling them to be connecting with
the Alfred services.

## Prerequisites

- Java 12+

## Setup

- Install SDKMAN: `curl -s "https://get.sdkman.io" | bash`
- Install Java 12+: `sdk install java 12.0.1.h` or superior
- Create a `local.properties` file in the root folder of the project.
- Add the following content to the `local.properties` file, changing the credentials.
```properties
    artifactory.username=${ARTIFACTORY_USERNAME}
    artifactory.password=${ARTIFACTORY_PASSWORD}
```

## Building

- `./gradlew clean build`

## Publish

Publishing packages to a package registry.

### Maven Local

- `./gradlew clean build publishToMavenLocal`

### Artifactory

- `./gralew clean build publish`

<!--
## Signing
- Install Maven: `sdk install maven`
- Install Gradle: `sdk install gradle`
- Install GnuPG: `brew install gnupg`
- Generate a new GnuPG key pair: `gpg --gen-key`
- Extract the last 8 symbols from the keyId using: `gpg -K`
- Export the gpg key using: `gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg`
- Create a `gradle.properties` file inside `GRADLE_USER_HOME` (default `~/.gradle`)
- Add the following to the `gradle.properties` file:
    ```shell script
    signing.keyId=<last 8 symbols from the keyId>
    signing.password=<key password>
    signing.secretKeyRingFile=</path/to/the/exported/key.gpg>
    ```
-->

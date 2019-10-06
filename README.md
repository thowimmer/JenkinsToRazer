# JenkinsToRazer
[![GitHubActionsCIBuild](https://github.com/thowimmer/JenkinsToRazer/workflows/JenkinsToRazer%20CI%20Build/badge.svg)](https://github.com/thowimmer/JenkinsToRazer/actions?workflow=JenkinsToRazer%20CI%20Build)

Visualize build status of Jenkins jobs on a Razer Chroma compatible keyboard.

## Currently supported platforms
* Windows

## Downloads

### Latest stable release
Download the executable binary from the [latest release section](https://github.com/thowimmer/JenkinsToRazer/releases/latest)

## Configuration
The sample [jenkins2razer.json](/config/jenkins2razer.json) shows how to configure the job visualization on your keyboard with the following properties:

#### Option: `url`
The URL of the Jenkins instance.

#### Option: `auth.username`
The Jenkins user used retrieve the build job information.

#### Option: `auth.password`
The Jenkins API token or password.

#### Option: `razer.backgroundEffect`
The id of the background effect. Currently only effects of type *STATIC* are allowed.

#### Option: `buildJobs`
Array of build jobs to display on the keyboard.

#### Option: `buildJobs[i].id`
Unique numerical identifier of the job.

#### Option: `buildJobs[i].job`
Identifier of the Jenkins job.

#### Option: `buildJobs[i].branch`
Optional branch name. Only required if the job is a Multibranch Pipeline job.

#### Option: `buildJobs[i].keyColumn`
The column index of the keyboard key used to visualize the build status.

#### Option: `buildJobs[i].keyRow`
The row index of the keyboard key used to visualize the build status.

#### Option: `buildJobs[i].buildInProgressEffect`
Identifier of the effect which is visualized if the build is in progess.

#### Option: `buildJobs[i].buildSuccessfulEffect`
Identifier of the effect which is visualized if the build finished successfully.

#### Option: `buildJobs[i].buildFailedEffect`
Identifier of the effect which is visualized if the build failed.

#### Option: `effects`
Array of effects which are referenced in the razer and buildJob configuration.

#### Option: `effects[i].id`
Unique alphanumeric identifier of the effect.

#### Option: `effects[i].type`
Discriminator for the effect type. One of *STATIC* or *BLINK*.

---
#### Options for *STATIC* effect types
#### Option: `effects[i].rgbHex`
RGB hex string of the static color.

---
#### Options for *BLINK* effect types
#### Option: `effects[i].rgbOnHex`
RGB hex string if blink toggle is on.
#### Option: `effects[i].rgbOffHex`
RGB hex string if blink toggle is off.


## How to use
### Windows
#### 1) Install Razer Synapse 3
Download and install [Razer Synapse 3](https://www.razer.com/synapse-3)

#### 2) Copy configuration to user home
Copy your [jenkins2razer.json](/config/jenkins2razer.json) configuration file to your users home directory (C:\Users\<username>).

#### 3) Execute JenkinsToRazer.exe
Execute JenkinsToRazer.exe in the Windows command line.

## References
* [Jenkins Remote Access API Documentation](https://wiki.jenkins.io/display/JENKINS/Remote+access+API)
* [Razer Chroma SDK REST Documentation](https://assets.razerzone.com/dev_portal/REST/html/index.html)
* [Circle CI config](/.circleci/config.yml)
* [Docker Image for buidling Kotlin Native/MP applications](https://hub.docker.com/r/thowimmer/kotlin-native-multiplatform)

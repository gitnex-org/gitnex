[![License: GPL v3](https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/license.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Pipeline status](https://img.shields.io/gitlab/pipeline/mmarif4u/gitnex-ci/main)](https://gitlab.com/mmarif4u/gitnex-ci/-/pipelines) [![Release](https://img.shields.io/badge/dynamic/json.svg?label=release&url=https://codeberg.org/api/v1/repos/gitnex/GitNex/releases&query=$[0].tag_name)](https://codeberg.org/gitnex/GitNex/releases) [![Crowdin](https://badges.crowdin.net/gitnex/localized.svg)](https://crowdin.com/project/gitnex) [![Join the Discord chat at https://discord.gg/FbSS4rf](https://img.shields.io/discord/632219664587685908.svg)](https://discord.gg/FbSS4rf)

[<img alt="Become a Patreon" src="https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/patreon.png" height="40"/>](https://www.patreon.com/mmarif) [<img alt="Buy me a coffee" src="https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/buy-me-a-coffee.png" height="40"/>](https://www.buymeacoffee.com/mmarif)

# GitNex - Android client for Gitea

GitNex is a free/paid, open-source Android client for Git repository management tool Gitea. Gitea is a community managed fork of Gogs, lightweight code hosting solution written in Go.

GitNex is licensed under GPLv3 License. See the LICENSE file for the full license text. **No trackers are used** and source code is available here for anyone to audit.

## Downloads
[<img alt='Get it on F-Droid' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/fdroid.png' height="80"/>](https://f-droid.org/en/packages/org.mian.gitnex/)
[<img alt='Get it on Google Play' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/google-play.png' height="80"/>](https://play.google.com/store/apps/details?id=org.mian.gitnex.pro)
[<img alt='Download builds and releases' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/apk-badge.png' height="82"/>](https://cloud.swatian.com/s/DN7E5xxtaw4fRbE)

## Note about Gitea version
Please make sure that you are on latest stable release or later for better app experience.

Check the versions [compatibility page](https://codeberg.org/gitnex/GitNex/wiki/Compatibility) which lists all the supported versions with compatibility ratio.

## Build from source
Option 1 - Download the source code, open it in Android Studio and build it there.

Option 2 - Open terminal(Linux) and cd to the project dir. Run `./gradlew assembleFree`.

## Features
- Multiple accounts support
- File and directory browser
- File viewer
- Create files
- Explore repositories
- Pull requests
- Files diff for PRs
- Notifications
- Drafts
- Repositories / issues / org list
- [MANY MORE](https://codeberg.org/gitnex/GitNex/wiki/Features)

## Contributing
[CONTRIBUTING](https://codeberg.org/gitnex/GitNex/src/branch/main/CONTRIBUTING.md)

## Translation
Help us translate GitNex to your native language.

We use [Crowdin](https://crowdin.com/project/gitnex) for translation. If your language is not listed, please request [here](https://codeberg.org/gitnex/GitNex/issues) to add it to the project.

**Link: https://crowdin.com/project/GitNex**

## Screenshots:

<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/001.png" alt="001.png" width="200"/>  | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/002.png" alt="002.png" width="200"/> | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/003.png" alt="003.png" width="200"/> | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/004.png" alt="004.png" width="200"/>
---|---|---|---
<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/005.png" alt="005.png" width="200"/> | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/006.png" alt="006.png" width="200"/> | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/007.png" alt="007.png" width="200"/>  | <img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/008.png" alt="008.png" width="200"/>

## Links
[Website](https://gitnex.com)

[Wiki](https://codeberg.org/gitnex/GitNex/wiki/Home)

[Troubleshoot Guide](https://codeberg.org/gitnex/GitNex/wiki/Troubleshoot-Guide)

[Faq](https://codeberg.org/gitnex/GitNex/wiki/FAQ)

[Release Blog](https://gitnex.codeberg.page)

## Thanks
Thanks to all the open source libraries, contributors and donators.

#### Open source libraries
- [square/retrofit](https://github.com/square/retrofit)
- [google/gson](https://github.com/google/gson)
- [square/okhttp](https://github.com/square/okhttp)
- [square/picasso](https://github.com/square/picasso)
- [wasabeef/picasso-transformations](https://github.com/wasabeef/picasso-transformations)
- [cats-oss/android-gpuimage](https://github.com/cats-oss/android-gpuimage)
- [noties/Markwon](https://github.com/noties/Markwon)
- [noties/Prism4j](https://github.com/noties/Prism4j)
- [ocpsoft/prettytime](https://github.com/ocpsoft/prettytime)
- [amulyakhare/TextDrawable](https://github.com/amulyakhare/TextDrawable)
- [vdurmont/emoji-java](https://github.com/vdurmont/emoji-java)
- [Pes8/android-material-color-picker-dialog](https://github.com/Pes8/android-material-color-picker-dialog)
- [HamidrezaAmz/BreadcrumbsView](https://github.com/HamidrezaAmz/BreadcrumbsView)
- [Baseflow/PhotoView](https://github.com/Baseflow/PhotoView)
- [apache/commons](https://github.com/apache/commons-io)
- [ge0rg/MemorizingTrustManager](https://github.com/ge0rg/MemorizingTrustManager)
- [mikaelhg/urlbuilder](https://github.com/mikaelhg/urlbuilder)
- [ACRA/acra](https://github.com/ACRA/acra)
- [chrisvest/stormpot](https://github.com/chrisvest/stormpot)

#### Icon sets
- [lucide-icons/lucide](https://github.com/lucide-icons/lucide)
- [primer/octicons](https://github.com/primer/octicons)
- [google/material-design-icons](https://github.com/google/material-design-icons)

[Follow me on Fediverse - mastodon.social/@mmarif](https://mastodon.social/@mmarif)

*All trademarks and logos are the properties of their respective owners.*

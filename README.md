[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/badge/dynamic/json.svg?label=release&url=https://gitea.com/api/v1/repos/mmarif/GitNex/releases&query=$[0].tag_name)](https://gitea.com/mmarif/GitNex/releases)

[<img alt="Become a Patroen" src="https://c5.patreon.com/external/logo/become_a_patron_button@2x.png" height="40"/>](https://www.patreon.com/mmarif)
[<img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="40"/>](https://liberapay.com/mmarif/donate)

# GitNex - Android client for Gitea

GitNex is a free, open-source Android client for Git repository management tool Gitea. Gitea is a community managed fork of Gogs, lightweight code hosting solution written in Go.

GitNex is licensed under GPLv3 License. See the LICENSE file for the full license text.
No trackers are used and source code is available here for anyone to audit.

## Downloads
[<img alt='Get it on F-droid' src='https://gitlab.com/fdroid/artwork/raw/master/badge/get-it-on.png' height="80"/>](https://f-droid.org/en/packages/org.mian.gitnex/)
[<img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="80"/>](https://play.google.com/store/apps/details?id=org.mian.gitnex)
[<img alt='Download APK' src='https://gitnex.com/img/download-apk.png' height="80"/>](https://gitea.com/mmarif/GitNex/releases)

## Note about Gitea version
Please make sure that you are on Gitea **1.9.x** stable release or later. Below this may not work as one would expect because of the newly added objects to the API at later versions. Please consider updating your Gitea server.

Check the versions [compatibility page](https://gitea.com/mmarif/GitNex/wiki/Compatibility) which lists all the supported versions with compatibility ratio.

## Build from source
Option 1 - Download the source code, open it in Android Studio and build it there.

Option 2 - Open terminal(Linux) and cd to the project dir. Run `./gradlew build`.

## Features
- My Repositories
- Repositories list
- Organizations list
- Create repository
- Create organization
- Issues list
- [MANY MORE](https://gitea.com/mmarif/GitNex/wiki/Features)

## Contributing
[CONTRIBUTING](https://gitea.com/mmarif/GitNex/src/branch/master/CONTRIBUTING.md)

## Screenshots:
[Screenshots](https://gitea.com/mmarif/GitNex/src/branch/master/fastlane/metadata/android/en-US/images/phoneScreenshots)

## FAQ
[Faq](https://gitea.com/mmarif/GitNex/wiki/FAQ)

## Links
[Website](https://gitnex.com)

[Wiki](https://gitea.com/mmarif/GitNex/wiki/Home)

[Website Repository](https://gitlab.com/mmarif4u/gitnex-website)

[Troubleshoot Guide](https://gitea.com/mmarif/GitNex/wiki/Troubleshoot-Guide)

## Thanks
Thanks to all the open source libraries, contributors and donators.

Open source libraries
- Retrofit
- Gson
- Okhttp
- ViHtarb/tooltip
- Picasso
- Markwon
- Prettytime
- Amulyakhare/textdrawable
- Vdurmont/emoji-java
- Abumoallim/android-multi-select-dialog
- Pes/materialcolorpicker
- Hendraanggrian/socialview

[Follow me on Fediverse - mastodon.social/@mmarif](https://mastodon.social/@mmarif)

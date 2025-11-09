[![License: GPL v3](https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/license.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Pipeline status](https://ci.codeberg.org/api/badges/gitnex/GitNex/status.svg)](https://ci.codeberg.org/gitnex/GitNex) [![Release](https://img.shields.io/badge/dynamic/json.svg?label=release&url=https://codeberg.org/api/v1/repos/gitnex/GitNex/releases&query=$[0].tag_name)](https://codeberg.org/gitnex/GitNex/releases) [![Crowdin](https://badges.crowdin.net/gitnex/localized.svg)](https://crowdin.com/project/gitnex) [![Join the Discord chat at https://discord.gg/FbSS4rf](https://img.shields.io/discord/632219664587685908.svg)](https://discord.gg/FbSS4rf)

[<img alt="Become a Patreon" src="https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/patreon.png" height="80"/>](https://www.patreon.com/mmarif)

# GitNex - Android Client for Forgejo and Gitea

GitNex is a FOSS Android client (available both as gratis & paid) for the Git repository management tools Forgejo and Gitea.

Licensed under the GPLv3 License. Please refer to the LICENSE file for the full text of the license. **No trackers are used**, and the source code is available here for anyone to audit.

## Downloads

[<img alt='Get it on F-Droid' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/fdroid.png' height="80"/>](https://f-droid.org/en/packages/org.mian.gitnex/)
[<img alt='Get it on Google Play' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/google-play.png' height="80"/>](https://play.google.com/store/apps/details?id=org.mian.gitnex.pro)
[<img alt='Download builds and releases' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/apk-badge.png' height="82"/>](https://cloud.swatian.com/s/WS4k3seXnmfQppo)
[<img alt='Get it on OpenAPK' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/openapk.png' height="82"/>](https://www.openapk.net/gitnex-for-forgejo-and-gitea/org.mian.gitnex/)
[<img alt='Get it on IzzyOnDroid' src='https://codeberg.org/gitnex/GitNex/raw/branch/main/assets/IzzyOnDroid.png' height="82"/>](https://apt.izzysoft.de/fdroid/index/apk/org.mian.gitnex)

## Note About Forgejo and Gitea Version

For the best experience, please ensure that you are using the latest stable release or later of your Forgejo or Gitea instance.

## Build from Source

**Option 1:** Download the source code, open it in Android Studio, and build it there.

**Option 2:** Open the terminal (Linux) and navigate to the project directory. Then, run: `./gradlew assembleFree`

## Features

- Multiple accounts support
- File and directory browser
- File viewer
- Create files
- Explore repositories / issues / organizations / users
- Pull requests
- Files diff for PRs
- Notifications
- Notes
- Repositories / issues list
- [And much more...](https://codeberg.org/gitnex/GitNex/wiki/Features)

## Contributing

We welcome contributions! For information regarding contribution guidelines, please click [here](https://codeberg.org/gitnex/GitNex/wiki/Contributing).

## Translation

We use [Crowdin](https://crowdin.com/project/gitnex) for translations. If you can, please help improve the translation for your language. If your language is not listed, please request to add it to the project [here](https://codeberg.org/gitnex/GitNex/issues).

**Translation Portal: https://crowdin.com/project/GitNex**

## Screenshots

[<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/001.png" alt="001.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/001.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/002.png" alt="002.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/002.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/003.png" alt="003.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/003.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/004.png" alt="004.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/004.png)
---|---|---|---
[<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/005.png" alt="005.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/005.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/006.png" alt="006.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/006.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/007.png" alt="007.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/007.png) | [<img src="https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/008.png" alt="008.png" width="200"/>](https://codeberg.org/gitnex/GitNex/raw/branch/main/fastlane/metadata/android/en-US/images/phoneScreenshots/008.png)

## Add a Custom URL Scheme

Starting with version 11.0.0, GitNex supports a custom URL scheme. This feature allows you to seamlessly open links directly in GitNex for issues, pull requests, commits, profiles, and repositories by using third-party apps like [URL Check](https://github.com/TrianguloY/URLCheck).

### How to Configure URL Check

1. Install the URL Check app from F-Droid or the Google Play Store.
2. Open the app and tap on **Module**.
3. Select **Pattern Checker** and then tap on **Json edit**.
4. Copy and paste the following JSON configuration into the editor. You can customize the `regex` parameter to add your own instances.
5. Save your changes.

JSON Configuration:
```
"GitNex": {
  "regex": "^https?://(?:[a-z0-9-]+\\.)*?(codeberg\\.org|gitea\\.com|.*\\.gitea\\.io)(/.*)",
  "replacement": "gitnex://$1$2"
}
```

## Links

- [Website](https://gitnex.com)
- [Wiki](https://codeberg.org/gitnex/GitNex/wiki/Home)
- [Troubleshooting Guide](https://codeberg.org/gitnex/GitNex/wiki/Troubleshoot-Guide)
- [FAQ](https://codeberg.org/gitnex/GitNex/wiki/FAQ)
- [Release Blog](https://gitnex.codeberg.page)

## Thanks

Thank you to all the open source libraries, contributors, and donors who make GitNex possible.

#### Open Source Libraries

- [square/retrofit](https://github.com/square/retrofit)
- [google/gson](https://github.com/google/gson)
- [square/okhttp](https://github.com/square/okhttp)
- [bumptech/glide](https://github.com/bumptech/glide)
- [noties/Markwon](https://github.com/noties/Markwon)
- [ocpsoft/prettytime](https://github.com/ocpsoft/prettytime)
- [ramseth001/TextDrawable](https://github.com/ramseth001/TextDrawable)
- [vdurmont/emoji-java](https://github.com/vdurmont/emoji-java)
- [skydoves/ColorPickerView](https://github.com/skydoves/ColorPickerView)
- [Baseflow/PhotoView](https://github.com/Baseflow/PhotoView)
- [apache/commons](https://github.com/apache/commons-io)
- [ge0rg/MemorizingTrustManager](https://github.com/ge0rg/MemorizingTrustManager)
- [mikaelhg/urlbuilder](https://github.com/mikaelhg/urlbuilder)
- [ACRA/acra](https://github.com/ACRA/acra)
- [chrisvest/stormpot](https://github.com/chrisvest/stormpot)
- [AmrDeveloper/CodeView](https://github.com/AmrDeveloper/CodeView)

#### Icon Sets

- [lucide-icons/lucide](https://github.com/lucide-icons/lucide)
- [primer/octicons](https://github.com/primer/octicons)
- [google/material-design-icons](https://github.com/google/material-design-icons)
- [tabler/tabler-icons](https://github.com/tabler/tabler-icons)

## Social

Follow on social media:

- [@mmarif@mastodon.social](https://mastodon.social/@mmarif)
- [mmarif.bsky.social](https://bsky.app/profile/mmarif.bsky.social)
- [X profile](https://x.com/mmarif08)

*All trademarks and logos are the properties of their respective owners.*
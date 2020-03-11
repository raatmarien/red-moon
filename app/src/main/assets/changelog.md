# Changelog
All notable changes to this project will be documented in this file. The format is based on [Keep a Changelog], and this project follows [Semantic Versioning].

## [4.0.0] — ????-??-??
### Added
- Start and stop times can be set to sunset and sunrise individually

### Changed
- All non-filter preferences moved to their own screen
- Replaced top switch with floating action button
- Prevent changing filter preferences until overlay permission is granted

### Fixed
- Time preferences display summary in proper locale

### Removed
- Removed "Sunrise to Sunset" preference, since you can set them individually

## [3.5.0] — 2020-03-01
### Added
- Filter toggles when the schedule is updated

### Changed
- Update translations

### Fixed
- Fix alignment in on/off switch at the top of schedule/exclude screens

### Removed
- Remove prompt to rate the app from the Google Play version

## [3.4.0] — 2019-02-02
### Added
- Preliminary support for devices running Android Oreo (8.0) and newer — @AdamNiederer
- Support for devices running Android versions going back to Ice Cream Sandwitch (4.0). Previous minimum was Jelly Bean (4.2.x).
- Added Spanish (American) translation — @mvelizbravo
- Added Spanish (Argentina) translation — @Andy-thor
- Added Telagu translation — @veeven

### Changed
- More intuitive behavior when saving filters with the same name — @david-hil
- Updated slider labels
- New changelog format
- Updated Arabic translation — Ammar
- Updated Basque translation Dabid — Martinez
- Updated Chinese (Simplified) translation — @gensitu, @Therhokar
- Updated Chinese (Traditional) translation — @gensitu, @louies0623
- Updated Dutch translation — @ltGuillaume, @wb9688
- Updated French translation — @dmaulat, @wellinkstein
- Updated German translation — Andreas Kleinert, @Atalanttore, @elumbella, @JoKeyser
- Updated Greek translation — @AndLydakis
- Updated Hungarian translation — @notramo
- Updated Italian translation — random r
- Updated Japanese translation — librada, @naofum
- Updated Lithuanian translation — @mobtechpd, @welaq
- Updated Norwegian translation Bokmål — @comradekingu
- Updated Persian translation — @ahangarha
- Updated Polish translation — @verdulo
- Updated Portuguese (Brazil) translation — @elchevive, @mv-santos
- Updated Russian translation — @mesnevi
- Updated Spanish translation — Alberto, ChemaBautista, @larjona, @mvelizbravo, @wakutiteo
- Updated Turkish translation — @emintufan, @monolifed
- Updated Ukrainian translation — Володимир Бриняк

### Fixed
- Schedule now updates correctly when changing time zones — @SeonD
- Notification no longer can become out of date while filter is paused — @david-hil
- Changelog works with the dark theme

## [3.3.2] — 2017-10-09
### Changed
- Updated Czech translation — @petrkle

### Fixed
- Fixed build — @petrkle

## [3.3.1] — 2017-10-02
### Changed
- Updated Serbian translation
- Updated Japanese translation
- Updated Polish translation
- Updated Turkish translation
- Updated Romanian translation
- Updated Ukrainian translation
- Updated Korean translation

## [3.3.0] — 2017-09-28
### Added
- You can now customize which apps are excluded from filtering. Exclude an app from the Red Moon notification while that app is running.

### Fixed
- Fixed a bug where backlight brightness was not restored
- Fixed a crash when the filter was turned on.

### Changed
- Updated Chinese translation — @Therhokar
- Updated Dutch translation — @wb9688
- Updated Turkish translation — @mission712
- Updated Romanian translation — Ervin Bolat
- Updated Russian translation — @antonv6
- Updated Norwegian (Bokmal) translation — Tale Haukbjørk, @comradekingu

## [3.2.0] — 2017-09-14
### Added
- Link to translation page on the about screen
- List translators on the about screen. Note to translators: The list of translators is just another localized string — Add your name or handle to it if you'd like to be included.
- Show a warning toast in apps where some features will not work when the filter is running (proton mail).
- Created irc & matrix chat rooms, see the [project page](https://github.com/LibreShift/red-moon/) for links.

### Changed
- Automatic pauses behave identically to manual pauses.
- Rename "timer" to "schedule" and show the times on the main screen.

### Fixed
- Rewrote the "Pause in secure apps" feature to fix crashes and bugs and reduce battery use. Note: this release has added logging which may increase battery use; it will be removed in the next release if no bugs are found.
- Fix a bug where the filter would not start as scheduled on Android 6.0+
- When turning off, don't change the backlight brightness if was changed since Red Moon lowered it.
- Fix crash when upgrading between 3.1.x versions.
- Fix a bug where profiles would not be saved until Red Moon was closed and re-opened.

## [3.1.2] — 2017-09-07
### Fixed
- Fixed a bug where widgets and tiles did not work

## [3.1.1] — 2017-09-05
### Fixed
- Fixed a crash on Android 6.0 and earlier

## [3.1.0] — 2017-09-02
### Added
- Long press on the quick settings tile to open Red Moon

### Changed
- Turn the timer on by default
- Updated Chinese (simplified) translation — @frankzheng43
- Updated Esperanto translation — @Reedych
- Updated French translation — @gwenhael-le-moine, @swalladge, @Remy J
- Updated Italian translation — @matteocoder
- Updated Japanese translation — @naofum
- Updated Norwegian (Bokmål) translation — Tale Haukbjørk
- Updated Russian translation — @antonv6
- Updated Serbian translation — @pejakm
- Updated Turkish translation — @monolifed, @oguz-ismail
- Updated Ukrainian translation — Володимир Бриняк

### Fixed
- Fixed a bug where the top switch did not toggle the filter
- Fixed a bug where the fade transition would not play if the filter was toggled mid-transition.
- Fixed bugs when Red Moon is killed throug Android

## [3.0.0] — 2017-04-23
### Added
- The custom (unsaved) filter remembers last used values
- You can now switch to the custom filter in the notification
- Added a third default filter: "Dim only"
- Default filters can be deleted
- Default filters can be restored from the menu (other filters are preserved)
- Added basic fade-in support (currently hard-coded to 1 hour after sunset and sunrise)

### Changed
- Lower bightness applies per-filter

### Fixed
- Fixed a bug where sunrise and sunset times would not be updated each day
- Many minor bug fixes

## [2.10.2] — 2017-03-28
### Added
- Added changelog

### Changed
- When using sunset times, update location automatically
- Display location update status more prominently, in a bottom bar
- Moved dark theme preference to three dot menu
- Small performance improvements

### Fixed
- Updated the intro so it doesn't overflow on small screens
- Fall back to GPS if network location is turned off (network location is still used by default, for battery savings)
- More accurate location update messages
- Fixed a bug where changes to 'Pause in secure apps' did not apply until Red Moon was killed

### Removed
- Remove location preference

## [2.10.1] — 2017-03-12
### Added
- Allow choosing how hardware buttons are dimmed
- Notification shows when Red Moon is paused in a secure app

### Changed
- Update notification icon
- Update translations
- Improved logging

### Fixed
- Fixed a bug where the filter wouldn't automatically turn on/off (#126)
- Small performance improvements

## [2.10.0] — 2017-03-03
### Added
- Added timer subscreen
- Added about page
- Use GPS on devices that can't get a network location
- [For developers and power users] Notify other apps when Red Moon toggles (details on the about page).
- Added Italian translation — @matteocoder

### Changed
- Refer to "Red Moon" instead of "The filter"
- Top switch toggles Red Moon on and off (instead of enabling/disabling all functionality)
- Update notification and widget phrasing/icons
- Updated translations

### Fixed
- Don't stop searching for location when the app is closed
- Fixed a bug where Red Moon didn't pause in new versions of the package installer
- Many small bugfixes

### Removed
- Remove the floating button (since the top switch does this now)


[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Semantic Versioning]: https://semver.org/spec/v2.0.0.html
[4.0.0]: https://github.com/LibreShift/red-moon/compare/v3.5.0...v4.0.0
[3.4.0]: https://github.com/LibreShift/red-moon/compare/v3.4.0...v3.5.0
[3.4.0]: https://github.com/LibreShift/red-moon/compare/v3.3.2...v3.4.0
[3.3.2]: https://github.com/LibreShift/red-moon/compare/v3.3.1...v3.3.2
[3.3.1]: https://github.com/LibreShift/red-moon/compare/v3.3.0...v3.3.1
[3.3.0]: https://github.com/LibreShift/red-moon/compare/v3.2.0...v3.3.0
[3.2.0]: https://github.com/LibreShift/red-moon/compare/v3.1.2...v3.2.0
[3.1.2]: https://github.com/LibreShift/red-moon/compare/v3.1.1...v3.1.2
[3.1.1]: https://github.com/LibreShift/red-moon/compare/v3.1.0...v3.1.1
[3.1.0]: https://github.com/LibreShift/red-moon/compare/v3.0.0...v3.1.0
[3.0.0]: https://github.com/LibreShift/red-moon/compare/v2.10.2...v3.0.0
[2.10.2]: https://github.com/LibreShift/red-moon/compare/v2.10.1...v2.10.2
[2.10.1]: https://github.com/LibreShift/red-moon/compare/v2.10.0...v2.10.1
[2.10.0]: https://github.com/LibreShift/red-moon/compare/v2.9.2...v2.10.0

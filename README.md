<img align="right" height="256" src="https://lut.im/3IqLwsAZWH/piFLRMOgNLWmiqB8.png">

# Red Moon [![Translation status](https://hosted.weblate.org/widgets/red-moon/-/svg-badge.svg)](https://hosted.weblate.org/engage/red-moon/?utm_source=widget)

Blue light can suppress the production of melatonin, the sleep hormone. Red Moon
filters out blue light and dims your screen below the normal minimum, so you can
use your phone comfortably at night. It is simple and flexible:

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/repository/browse/?fdid=com.jmstudios.redmoon)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" 
      alt="Download from Google Play" 
      height="80">](https://play.google.com/store/apps/details?id=com.jmstudios.redmoon)
[<img src="art/direct-download.png" 
      alt="Direct download" 
      height="80">](https://github.com/raatmarien/red-moon/releases)

* Set the timer to automatically turn on at night, or at custom times.
* Use the default filters, or fine-tune your own with separate color, intensity and dim levels.
* The notification, tile (Android 7.0+), and widget let you start, stop, and switch filters without opening the app.

## Get involved

<a href="https://hosted.weblate.org/engage/red-moon/?utm_source=widget">
<img align="right" src="https://hosted.weblate.org/widgets/red-moon/-/multi-auto.svg" alt="Translation status" />
</a>

*All help is equally welcome!*

The easiest way is to **report bugs** and **give feedback** in the [issues section]
or the [matrix chat room]. If you're feeling adventurous, you can [test new features]
before they're released!

[Weblate] is the simplest way for most people to **translate**. It's entirely in
your browser, and your changes will be automatically merged. If you'd prefer
to make a pull request, please keep code and translations in separate PRs.

> **Each string will only be used in one place.**  
> This way, if you want to tweak the phrasing in one particular place, you don't
> need to worry about accidentally changing another place in the app.

PRs for issues tagged with [`bug`] or [`patches welcome`] are welcome at any time.
Otherwise, please [open a new issue] or comment on the existing one first. We don't
want to waste your time on something we might not merge! We have some loose coding
style guidelines [below](#coding-style). 

## Screenshots

<img src="https://lut.im/uMUMujZSZU/rfHRfhIrDnDznetz.png" width="180" height="320" /> <img src="https://lut.im/nZBsmMs4KI/RutzvgfCCPSR2vDd.png" width="180" height="320" />
<img src="https://lut.im/MxfTcNiz5b/xhJDuKvyxEOJlc39.png" width="180" height="320" />
<img src="https://lut.im/t3Ll6xBLle/XrhZCJmIcggRHeHf.png" width="180" height="320" />
<img src="https://lut.im/O5bUIZVPDR/ddcBY3akDK6Sq1zU.png" width="180" height="320" />
<img src="https://lut.im/0YrVNYZbj2/lr0SDpIqy7jlpg13.png" width="180" height="320" />
<img src="https://lut.im/LPf77AuSRG/MsJbEeHXHxyQ7XSf.png" width="180" height="320" />
<img src="https://lut.im/7eRSVHlsoS/2OJNIqCG3NQTExZI.png" width="180" height="320" />

## Building

To build the app on GNU+Linux, clone the repository and run

`./gradlew build`

in the root directory.

Use

`./gradlew installFdroidDebug` or `./gradlew installPlaystoreDebug`

to install the app on a connected device or running emulator.

### Coding Style

**Prioritize legibility over dogmatism.** That said, consistency is nice, so here's a short list of what I've been doing. 

- 100 characters per line; 80 if you can.
- Indent 4 spaces normally; 8 spaces if a variable assignment needs to wrap.
- Constants use `ALL_CAPS`, persistent vars use `mCamelCase`, everything else uses `camelCase`.
- Form suggests function: Group and align similar actions, and *don't* do that for dissimmilar ones, even if you could.
- Good comments explain *why* something is done; if you find yourself describing *what* the code does, consider:
    - Refactoring into smaller functions with descriptive names
    - Converting comments to logs. Code that requires comments probably also requires good logs to debug.
- Always use brackets with `if`, with one exception: one-liners with an `else` branch (`x = if (p) q else r`)
    - note: I (@smichel17) used to prefer omitting parenthesis from one-liners; some code still uses that style.

Again, **prioritize legibility over dogmatism.**

## License

[<img src="https://www.gnu.org/graphics/gplv3-127x51.png"
      align="right"
      alt="GNU GPLv3 Image">](http://www.gnu.org/licenses/gpl-3.0.en.html)

"Red Moon" is a derivative of
"[Shades](https://github.com/cngu/shades)" by
[Chris Nguyen](https://github.com/cngu) used under the
[MIT License](https://github.com/cngu/shades/blob/e240edc1df3e6dd319cd475a739570ff8367d7f8/LICENSE). "Red
Moon" is licensed under the
[GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html),
or (at your option) any later version by [the contributors](https://github.com/raatmarien/red-moon/graphs/contributors).

All used artwork is released into the public domain. Some of the icons
use cliparts from [openclipart.org](https://openclipart.org/), which
are all released in the public domain, namely:

* https://openclipart.org/detail/121903/full-moon
* https://openclipart.org/detail/219211/option-button-symbol-minimal-svg-markup
* https://openclipart.org/detail/20806/wolf-head-howl-1
* https://openclipart.org/detail/213998/nexus-5-flat
* https://openclipart.org/detail/192689/press-button

---

\* Google Play and the Google Play logo are trademarks of Google Inc.

[`patches welcome`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22patches+welcome%22
[matrix chat room]: https://matrix.to/#/#red-moon:matrix.org
[issues section]: https://github.com/raatmarien/red-moon/issues
[open a new issue]: https://github.com/raatmarien/red-moon/issues/new
[test new features]: https://github.com/raatmarien/red-moon/issues/146
[`bug`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3Abug
[Weblate]: https://hosted.weblate.org/projects/red-moon/strings/

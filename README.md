<img align="right" height="256" src="https://lut.im/3IqLwsAZWH/piFLRMOgNLWmiqB8.png">
# Red Moon [![Translation status](https://hosted.weblate.org/widgets/red-moon/-/svg-badge.svg)](https://hosted.weblate.org/engage/red-moon/?utm_source=widget)

Red Moon is a screen filter app for night time phone use. It helps
you sleep healthier by filtering out blue light, which can disrupt
your sleep cycle (circadian rhythm). And when your phone's lowest
brightness isn't low enough, Red Moon protects your eyes by making
your screen even darker. 

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/repository/browse/?fdid=com.jmstudios.redmoon)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" 
      alt="Download from Google Play" 
      height="80">](https://play.google.com/store/apps/details?id=com.jmstudios.redmoon)

[Direct download](https://github.com/raatmarien/red-moon/releases)

## Features

* Use the default filters or create your own, with separate color, intensity and dim settings.
* Automatically turn Red Moon on from sunset to sunrise, or set custom times.
* Switch between filters right in the notification, without leaving the app you're using.
* Turn Red Moon on and off without opening the app using:
  * A widget or a shortcut on your home screen.
  * A quick settings tile, like WiFi or Airplane mode (Android 7.0+). 
  * The notification (if Red Moon is on, or was recently on).

## Screenshots

<img src="https://lut.im/uMUMujZSZU/rfHRfhIrDnDznetz.png" width="180" height="320" />
<img src="https://lut.im/nZBsmMs4KI/RutzvgfCCPSR2vDd.png" width="180" height="320" />
<img src="https://lut.im/MxfTcNiz5b/xhJDuKvyxEOJlc39.png" width="180" height="320" />
<img src="https://lut.im/t3Ll6xBLle/XrhZCJmIcggRHeHf.png" width="180" height="320" />
<img src="https://lut.im/O5bUIZVPDR/ddcBY3akDK6Sq1zU.png" width="180" height="320" />
<img src="https://lut.im/0YrVNYZbj2/lr0SDpIqy7jlpg13.png" width="180" height="320" />
<img src="https://lut.im/LPf77AuSRG/MsJbEeHXHxyQ7XSf.png" width="180" height="320" />
<img src="https://lut.im/7eRSVHlsoS/2OJNIqCG3NQTExZI.png" width="180" height="320" />
<img src="https://lut.im/CU0bQLa8RN/3o90N3QNCuNrOKnu.png" width="180" height="320" />
<img src="https://lut.im/7QAYwUXjqi/pWiBEF8fP8D3fpAp.png" width="180" height="320" />
<img src="https://lut.im/d9a4QT8Nek/9jgcvxedO1pnAfXl.png" width="180" height="320" />
<img src="https://lut.im/BQODkgLLGg/VGFnSMufAVBQoOSt.png" width="180" height="320" />

## License

"Red Moon" is a derivative of
"[Shades](https://github.com/cngu/shades)" by
[Chris Nguyen](https://github.com/cngu) used under the
[MIT License](https://github.com/cngu/shades/blob/e240edc1df3e6dd319cd475a739570ff8367d7f8/LICENSE). "Red
Moon" is licensed under the
[GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html),
or (at your option) any later version by [the contributors](https://github.com/raatmarien/red-moon/graphs/contributors).

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

All used artwork is released into the public domain. Some of the icons
use cliparts from [openclipart.org](https://openclipart.org/), which
are all released in the public domain, namely:

* https://openclipart.org/detail/121903/full-moon
* https://openclipart.org/detail/219211/option-button-symbol-minimal-svg-markup
* https://openclipart.org/detail/20806/wolf-head-howl-1
* https://openclipart.org/detail/213998/nexus-5-flat
* https://openclipart.org/detail/192689/press-button

## Building

To build the app on GNU+Linux, clone the repository and run

``` ./gradlew build ```

in the root directory.

Use

``` ./gradlew installDebug ```

to install the app on a connected device.

## How it works

Red Moon dims and colors the screen by displaying a constant transparent overlay
If, for example, you have the intensity set to 0%, then the overlay will be
black with a transparency equal to the dim level you selected. If you choose a
higher intensity, the overlay will be saturated with the selected color.

## Contributing

All help is welcome! If you have found a bug or have an idea for a new
feature, just
[open a new issue](https://github.com/raatmarien/red-moon/issues/new). If
you can implement it yourself, simply fork this repository, make your
changes and open a pull request.

## Translating

Translating is very much appreciated! Just go to
[Weblate](https://hosted.weblate.org/projects/red-moon/strings/), create
an account and start a new translation or update an existing one.

## Footnotes

\* Google Play and the Google Play logo are trademarks of Google Inc.

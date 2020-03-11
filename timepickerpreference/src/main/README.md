Eventually, this TimePreference library will be broken out of Red Moon. When that happens, this readme will become the main readme for that repo. I'm writing it now so that I can write documentation as I develop, because if I leave it to the end, I will likely not end up writing it.

## Usage

Examples of preference xml files (eg, `res/xml/preferences.xml`) TimePreference

Optional properties:

- `android:defaultValue` is a string in the format `HH:mm` (default "00:00").
- `app:showNeutralButton` is `"true"` or `"false"` (default false)
- `app:neutralButtonText` is a string or reference to one (`"@string/..."`) (default "Default")
- `app:useSimpleSummary` is `"true"` or `"false"` (default true)
- `app:is24HourView` is `"true"` or `"false"` (default system locale)

### Simplest

TODO: Add screenshot of preference
TODO: Add screenshot of picker

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- All values inline -->
    <org.libreshift.preferences.TimePreference
        android:key="pref_key_time_start"
        android:title="Start Time"
        android:defaultValue="19:30" />

    <!-- Values by reference -->
    <org.libreshift.preferences.TimePreference
        android:key="@string/pref_key_end_time"
        android:title="@string/pref_title_end_time"
        android:defaultValue="6:30" />
</PreferenceScreen>
```

### Add "reset to default" option in dialog

TODO: Add 2 screenshot of picker with 3rd option

```xml
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:libreshift="http://schemas.android.com/apk/res/org.libreshift">
    <!-- Use default text for neutral button ("Default") -->
    <org.libreshift.preferences.TimePreference
        android:key="@string/pref_key_time_start"
        android:title="@string/pref_title_start_time"
        android:defaultValue="19:30"
        app:showNeutralButton="true" />

    <!-- Custom text for neutral button -->
    <org.libreshift.preferences.TimePreference
        android:key="@string/pref_key_end_time"
        android:title="@string/pref_title_end_time"
        android:defaultValue="6:30"
        app:showNeutralButton="true"
        app:neutralButtonText="Reset"/>
</PreferenceScreen>
```

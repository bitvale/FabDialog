FabDialog
=================

<img src="/art/preview.gif" alt="sample" title="sample" width="320" height="540" align="right" vspace="52" />

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

This is an Android project allowing to animate a custom Floating Action Button into a custom Dialog.

USAGE
-----

Just add FabDialog view in your layout XML and FabDialog library in your project via Gradle:

```gradle
dependencies {
  implementation 'com.bitvale:fabdialog:1.0.1'
}
```

XML
-----

```xml
<com.bitvale.fabdialog.widget.FabDialog
    android:id="@+id/dialog_fab"
    android:layout_width="@dimen/fab_size"
    android:layout_height="@dimen/fab_size"
    android:layout_marginEnd="@dimen/fab_margin"
    android:layout_marginBottom="@dimen/fab_margin"
    android:padding="@dimen/fab_padding"
    app:dialogBackgroundColor="@color/dialogColor"
    app:fabIcon="@drawable/android_icon" />
```

You must use the following properties in your XML to change your FabDialog.


##### Properties:

* `app:fabBackgroundColor`          (color)     -> default  ?attr/colorAccent
* `app:dialogBackgroundColor`       (color)     -> default  ?attr/colorBackgroundFloating
* `app:dialogCornerRadius`          (dimension) -> default  8dp
* `app:dimBackgroundEnabled`        (boolean)   -> default  true
* `app:dimBackgroundColor`          (color)     -> default  BLACK with transparency (#99000000)
* `app:closeOnTouchOutside`         (boolean)   -> default  true

Kotlin
-----

```kotlin
with(dialog_fab) {
    setTitle(R.string.dialog_title)
    setMessage(R.string.dialog_message)
    setDialogIcon(R.drawable.android_icon)
    setFabIcon(R.drawable.android_icon)
    setFabBackgroundColor(ContextCompat.getColor(context, R.color.fabColor))
    setDialogBackgroundColor(ContextCompat.getColor(context, R.color.dialogColor))
    setPositiveButton(R.string.positive_btn) { // some action }
    setNegativeButton(R.string.negative_btn) { dialog_fab.collapseDialog() }
    setOnClickListener { dialog_fab.expandDialog() }
    setListener(this@MainActivity)
}
```

LICENCE
-----

FabDialog by [Alexander Kolpakov](https://play.google.com/store/apps/dev?id=7044571013168957413) is licensed under a [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
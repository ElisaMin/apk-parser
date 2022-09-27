# Re-Revived ApkParser

apk parser for kotlin/compose-jb, forked from **[here](https://github.com/AndroidDeveloperLB/apk-parser)** after converted to kotlin and re-support jvm

I personally use it for my own tools, **[Heizi Flash Tools](https://github.com/ElisaMin/Heizi-Flashing-Tools)**.

# Why use this library, as we can do it using the Android framework instead?

(multiplatform version maybe on processing) 

While the Android framework is more official and should work better in most cases, there are multiple reasons to use this library :
 
1. Can handle APK files that are not on the file system. The Android framework requires you to use a file path, always (using **[PackageManager.getPackageArchiveInfo](https://developer.android.com/reference/android/content/pm/PackageManager#getPackageArchiveInfo(java.lang.String,%20int))**). 
2. Can handle split APK files too. The Android framework can only handle the base ones or stand-alone files.
3. Can find some APK properties that aren't available on older Android versions, such as **[ApplicationInfo.minSdkVersion](https://developer.android.com/reference/android/content/pm/ApplicationInfo#minSdkVersion)**.
4. While the Android framework is technically open sourced, it has various things that are protected and can't be reached, and also hard to import as your own code.

So, what I suggest is to first try to use what Android officially offers, and if it fails, use this library.

# Usage in gradle file

https://jitpack.io/#ElisaMin/apk-parser

# How to use

You can use what's on the original repository (meaning creating a new instance of ApkFile, as shown **[here](https://github.com/hsiafan/apk-parser#usage)**), or you can have a more advanced usage of parsing the exact things you want, as on the sample.

# Known issues and notes

- The sample app shows that in some rare cases it fails to parse the label/icon of the app, and even completely (incredibly rare). For most of the cases, it happens for fetching the app icons, perhaps because they are the most complex (Adaptive icons, VectorDrawable,...). I hope that some day it could be fixed. Reported here: https://github.com/AndroidDeveloperLB/apk-parser/issues/3 https://github.com/AndroidDeveloperLB/apk-parser/issues/4 https://github.com/AndroidDeveloperLB/apk-parser/issues/1
- The sample shows how to parse a VectorDrawable, but of course it will work only when it's simple enough. If there are references within (to colors etc...), sadly it won't work. I hope it will be possible one day to parse it properly. Same goes for AdaptiveIcon and what it can have.
- Could be nice to have better optimization in memory usage and speed, because somehow the framework seems to be more efficient on both. I think a better optimization is needed. Maybe some sort of way to tell exactly what we want to get out of it, it would minimize such memory usage.

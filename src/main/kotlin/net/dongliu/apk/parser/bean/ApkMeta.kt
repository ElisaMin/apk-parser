package net.dongliu.apk.parser.bean

/**
 * Apk meta info
 *
 * @author dongliu
 */
data class ApkMeta(
    val packageName: String? = "",
    val label: String? = "",
    private val icon: String? = "",
    val versionName: String? = "",
    val versionCode: Long = 0,
    val revisionCode: Long? = 0,
    val sharedUserId: String? = "",
    val sharedUserLabel: String? = "",
    val split: String? = "",
    val configForSplit: String? = "",
    val isFeatureSplit: Boolean = false,
    val isSplitRequired: Boolean = false,
    val isolatedSplits: Boolean = false,
    val installLocation: String? = "",
    val minSdkVersion: String? = "",
    val targetSdkVersion: String? = "",
    val maxSdkVersion: String? = "",
    val compileSdkVersion: String? = "",
    val compileSdkVersionCodename: String? = "",
    val platformBuildVersionCode: String? = "",
    val platformBuildVersionName: String? = "",
    val glEsVersion: GlEsVersion? = null,
    val isAnyDensity: Boolean = false,
    val isSmallScreens: Boolean = false,
    val isNormalScreens: Boolean = false,
    val isLargeScreens: Boolean = false,
    val isDebuggable: Boolean = false,
    val usesPermissions: MutableList<String?> = arrayListOf(),
    val usesFeatures: MutableList<UseFeature> = arrayListOf(),
    val permissions: MutableList<Permission> = arrayListOf(),
)
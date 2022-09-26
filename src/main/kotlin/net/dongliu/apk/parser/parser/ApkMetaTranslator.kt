package net.dongliu.apk.parser.parser

import net.dongliu.apk.parser.bean.*
import net.dongliu.apk.parser.struct.ResourceValue.ReferenceResourceValue
import net.dongliu.apk.parser.struct.resource.Densities
import net.dongliu.apk.parser.struct.resource.ResourceTable
import net.dongliu.apk.parser.struct.xml.*
import java.util.*

/**
 * trans binary xml to apk meta info
 *
 * @author Liu Dong dongliu@live.cn
 */
class ApkMetaTranslator(private val resourceTable: ResourceTable, private val locale: Locale?) : XmlStreamer {
    private val tagStack = arrayOfNulls<String>(100)
    private var depth = 0
    var apkMeta = ApkMeta()
        private set
    var iconPaths = emptyList<IconPath>()
        private set

    override fun onStartTag(xmlNodeStartTag: XmlNodeStartTag) {
        val attributes = xmlNodeStartTag.attributes
        when (xmlNodeStartTag.name) {
            "application" -> {
                apkMeta = apkMeta.copy(isDebuggable = attributes.getBoolean("debuggable", false))
                //TODO fix this part in a better way. Workaround for this: https://github.com/hsiafan/apk-parser/issues/119
                if (apkMeta.split == null) apkMeta = apkMeta.copy(split = attributes.getString("split"))
                if (apkMeta.configForSplit == null) apkMeta = apkMeta.copy(configForSplit = attributes.getString("configForSplit"))
                if (!apkMeta.isFeatureSplit) apkMeta = apkMeta.copy(isFeatureSplit =
                    attributes.getBoolean(
                        "isFeatureSplit",
                        false
                    )
                )
                if (!apkMeta.isSplitRequired) apkMeta = apkMeta.copy(isSplitRequired =
                    attributes.getBoolean(
                        "isSplitRequired",
                        false
                    )
                )
                if (!apkMeta.isolatedSplits) apkMeta = apkMeta.copy(isolatedSplits =
                    attributes.getBoolean(
                        "isolatedSplits",
                        false
                    )
                )
                val label = attributes.getString("label")
                if (label != null) {
                    apkMeta = apkMeta.copy(label = label)
                }
                val iconAttr = attributes["icon"]
                if (iconAttr != null) {
                    val resourceValue = iconAttr.typedValue
                    if (resourceValue is ReferenceResourceValue) {
                        val resourceId = resourceValue.referenceResourceId
                        val resources = resourceTable.getResourcesById(resourceId)
                        if (resources.isNotEmpty()) {
                            val icons: MutableList<IconPath> = ArrayList()
                            var hasDefault = false
                            for (resource in resources) {
                                val type = resource.type
                                val resourceEntry = resource.resourceEntry
                                val path = resourceEntry.toStringValue(resourceTable, locale)
                                if (type.density == Densities.DEFAULT) {
                                    hasDefault = true
                                    apkMeta = apkMeta.copy(icon = path)
                                }
                                val iconPath = IconPath(path, type.density)
                                icons.add(iconPath)
                            }
                            if (!hasDefault) {
                                apkMeta = apkMeta.copy(icon = icons[0].path)
                            }
                            iconPaths = icons
                        }
                    } else {
                        val value = iconAttr.value
                        if (value != null) {
                            apkMeta = apkMeta.copy(icon = value)
                            val iconPath = IconPath(value, Densities.DEFAULT)
                            iconPaths = listOf(iconPath)
                        }
                    }
                }
            }

            "manifest" -> {
                apkMeta = apkMeta.copy(packageName = attributes.getString("package"))
                apkMeta = apkMeta.copy(versionName = attributes.getString("versionName"))
                apkMeta = apkMeta.copy(revisionCode = attributes.getLong("revisionCode"))
                apkMeta = apkMeta.copy(sharedUserId = attributes.getString("sharedUserId"))
                apkMeta = apkMeta.copy(sharedUserLabel = attributes.getString("sharedUserLabel"))
                if (apkMeta.split == null) apkMeta = apkMeta.copy(split = attributes.getString("split"))
                if (apkMeta.configForSplit == null) apkMeta = apkMeta.copy(configForSplit = attributes.getString("configForSplit"))
                if (!apkMeta.isFeatureSplit) apkMeta = apkMeta.copy(isFeatureSplit =
                    attributes.getBoolean(
                        "isFeatureSplit",
                        false
                    )
                )
                if (!apkMeta.isSplitRequired) apkMeta = apkMeta.copy(isSplitRequired =
                    attributes.getBoolean(
                        "isSplitRequired",
                        false
                    )
                )
                if (!apkMeta.isolatedSplits) apkMeta = apkMeta.copy(isolatedSplits =
                    attributes.getBoolean(
                        "isolatedSplits",
                        false
                    )
                )
                val majorVersionCode = attributes.getLong("versionCodeMajor")
                var versionCode = attributes.getLong("versionCode")
                if (majorVersionCode != null) {
                    if (versionCode == null) {
                        versionCode = 0L
                    }
                    versionCode = majorVersionCode shl 32 or (versionCode and 0xFFFFFFFFL)
                }
                if (versionCode != null) apkMeta = apkMeta.copy(versionCode = versionCode)
                val installLocation = attributes.getString("installLocation")
                if (installLocation != null) {
                    apkMeta = apkMeta.copy(installLocation = installLocation)
                }
                apkMeta = apkMeta.copy(compileSdkVersion = attributes.getString("compileSdkVersion"))
                apkMeta = apkMeta.copy(compileSdkVersionCodename = attributes.getString("compileSdkVersionCodename"))
                apkMeta = apkMeta.copy(platformBuildVersionCode = attributes.getString("platformBuildVersionCode"))
                apkMeta = apkMeta.copy(platformBuildVersionName = attributes.getString("platformBuildVersionName"))
            }

            "uses-sdk" -> {
                val minSdkVersion = attributes.getString("minSdkVersion")
                if (minSdkVersion != null) {
                    apkMeta = apkMeta.copy(minSdkVersion = minSdkVersion)
                }
                val targetSdkVersion = attributes.getString("targetSdkVersion")
                if (targetSdkVersion != null) {
                    apkMeta = apkMeta.copy(targetSdkVersion = targetSdkVersion)
                }
                val maxSdkVersion = attributes.getString("maxSdkVersion")
                if (maxSdkVersion != null) {
                    apkMeta = apkMeta.copy(maxSdkVersion = maxSdkVersion)
                }
            }

            "supports-screens" -> {
                apkMeta = apkMeta.copy(isAnyDensity = attributes.getBoolean("anyDensity", false))
                apkMeta = apkMeta.copy(isSmallScreens = attributes.getBoolean("smallScreens", false))
                apkMeta = apkMeta.copy(isNormalScreens = attributes.getBoolean("normalScreens", false))
                apkMeta = apkMeta.copy(isLargeScreens = attributes.getBoolean("largeScreens", false))
            }

            "uses-feature" -> {
                val name = attributes.getString("name")
                val required = attributes.getBoolean("required", false)
                if (name != null) {
                    val useFeature = UseFeature(name, required)
                    apkMeta.usesFeatures.add(useFeature)
                } else {
                    val gl = attributes.getInt("glEsVersion")
                    if (gl != null) {
                        val v: Int = gl
                        val glEsVersion = GlEsVersion(v shr 16, v and 0xffff, required)
                        apkMeta = apkMeta.copy(glEsVersion = glEsVersion)
                    }
                }
            }

            "uses-permission" -> apkMeta.usesPermissions.add(attributes.getString("name"))
            "permission" -> {
                val permission = Permission(
                    attributes.getString("name"),
                    attributes.getString("label"),
                    attributes.getString("icon"),
                    attributes.getString("description"),
                    attributes.getString("group"),
                    attributes.getString("android:protectionLevel")
                )
                apkMeta.permissions.add(permission)
            }
        }
        tagStack[depth++] = xmlNodeStartTag.name
    }

    override fun onEndTag(xmlNodeEndTag: XmlNodeEndTag) {
        depth--
    }

    override fun onCData(xmlCData: XmlCData) {}
    override fun onNamespaceStart(tag: XmlNamespaceStartTag) {}
    override fun onNamespaceEnd(tag: XmlNamespaceEndTag) {}

    private fun matchTagPath(vararg tags: String): Boolean {
        // the root should always be "manifest"
        if (depth != tags.size + 1) {
            return false
        }
        for (i in 1 until depth) {
            if (tagStack[i] != tags[i - 1]) {
                return false
            }
        }
        return true
    }

    private fun matchLastTag(tag: String): Boolean {
        // the root should always be "manifest"
        return tagStack[depth - 1]!!.endsWith(tag)
    }
}
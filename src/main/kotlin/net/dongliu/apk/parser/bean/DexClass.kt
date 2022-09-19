package net.dongliu.apk.parser.bean

import net.dongliu.apk.parser.struct.dex.DexClassStruct

/**
 * @author dongliu
 */
class DexClass(
    /**
     * the class name
     */
    val classType: String?, val superClass: String?, private val accessFlags: Int
) {
    val packageName: String
        get() {
            var packageName = classType
            if (packageName!!.length > 0) {
                if (packageName[0] == 'L') {
                    packageName = packageName.substring(1)
                }
            }
            if (packageName.length > 0) {
                val idx = classType!!.lastIndexOf('/')
                if (idx > 0) {
                    packageName = packageName.substring(0, classType.lastIndexOf('/') - 1)
                } else if (packageName[packageName.length - 1] == ';') {
                    packageName = packageName.substring(0, packageName.length - 1)
                }
            }
            return packageName.replace('/', '.')
        }
    val isInterface: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_INTERFACE != 0
    val isEnum: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_ENUM != 0
    val isAnnotation: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_ANNOTATION != 0
    val isPublic: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_PUBLIC != 0
    val isProtected: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_PROTECTED != 0
    val isStatic: Boolean
        get() = accessFlags and DexClassStruct.Companion.ACC_STATIC != 0

    override fun toString(): String {
        return "" + classType
    }
}
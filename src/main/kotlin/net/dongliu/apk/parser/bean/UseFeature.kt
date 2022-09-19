package net.dongliu.apk.parser.bean

/**
 * the permission used by apk
 *
 * @author dongliu
 */
class UseFeature(val name: String, val isRequired: Boolean) {
    override fun toString(): String {
        return name
    }
}
package net.dongliu.apk.parser.utils

/**
 * @author Liu Dong &lt;dongliu@live.cn&gt;
 */
class Pair<K, V> {
    private var left: K? = null
    private var right: V? = null

    constructor() {}
    constructor(left: K, right: V) {
        this.left = left
        this.right = right
    }

    fun getLeft(): K {
        return left
    }

    fun setLeft(left: K) {
        this.left = left
    }

    fun getRight(): V {
        return right
    }

    fun setRight(right: V) {
        this.right = right
    }
}
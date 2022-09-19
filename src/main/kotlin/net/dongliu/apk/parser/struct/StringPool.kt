package net.dongliu.apk.parser.struct

/**
 * String pool.
 *
 * @author dongliu
 */
class StringPool(poolSize: Int) {
    private val pool: Array<String?>

    init {
        pool = arrayOfNulls(poolSize)
    }

    operator fun get(idx: Int): String? {
        return pool[idx]
    }

    operator fun set(idx: Int, value: String?) {
        pool[idx] = value
    }
}
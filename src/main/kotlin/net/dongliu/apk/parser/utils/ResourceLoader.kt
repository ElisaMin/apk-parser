package net.dongliu.apk.parser.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * methods for load resources.
 *
 * @author dongliu
 */
object ResourceLoader {
    /**
     * load system attr ids for parse binary xml.
     */
    fun loadSystemAttrIds(): Map<Int, String> {
        try {
            toReader("/r_values.ini").use { reader ->
                val map: MutableMap<Int, String> = HashMap()
                var line: String
                while (reader?.readLine().also { line = it?:"" } != null) {
                    val items =
                        line.trim { it <= ' ' }.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (items.size != 2) {
                        continue
                    }
                    val name = items[0].trim { it <= ' ' }
                    val id = Integer.valueOf(items[1].trim { it <= ' ' })
                    map[id] = name
                }
                return map
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun loadSystemStyles(): Map<Int, String> {
        val map: MutableMap<Int, String> = HashMap()
        try {
            toReader("/r_styles.ini").use { reader ->
                var line: String
                while (reader?.readLine().also { line = it?:"" } != null) {
                    line = line.trim { it <= ' ' }
                    val items = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (items.size != 2) {
                        continue
                    }
                    val id = Integer.valueOf(items[1].trim { it <= ' ' })
                    val name = items[0].trim { it <= ' ' }
                    map[id] = name
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return map
    }

    private fun toReader(path: String): BufferedReader? = ResourceLoader::class
        .java
        .getResourceAsStream(path)
        ?.bufferedReader()
}
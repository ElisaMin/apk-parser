@file:JvmName("run")
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.window.singleWindowApplication
import me.heizi.apk.parser.ktx.Image
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
import kotlin.system.exitProcess

var time = System.currentTimeMillis()
fun updateTime(msg:String="") = System.currentTimeMillis().also { println("Time: ${it - time}ms $msg") }.also { time = it }

fun main(args: Array<String>) {
    updateTime("init")
    // init
    args.getOrNull(0)?.let {
        updateTime("parse")
        ApkFile(it)
            .icons
            .takeIf { it.isNotEmpty() }
    } ?.let {icons->
        updateTime("compose")
        singleWindowApplication {
            Row {
                val icon = icons.filter { it.density>=0 }.minBy { it.density }
                println(icon::class.simpleName)
                icons.forEach {
                    println(it::class)
                    println(it.density)
                    Image(it)
                }
                if (icon is ApkIcon.Adaptive) {
                    println(icon.background::class.simpleName)
//                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
//                } else {
//                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
                }
                updateTime("draw")
            }
        }

    } ?: exitProcess(-1)

}
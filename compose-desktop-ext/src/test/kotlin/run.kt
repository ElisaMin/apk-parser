@file:JvmName("run")
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import me.heizi.apk.parser.ktx.Image
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    args.getOrNull(0)?.let {
        ApkFile(it)
            .icons
            .takeIf { it.isNotEmpty() }
    } ?.let {icons->
        singleWindowApplication {
            Row {
                val icon = icons.minBy { it.density }
                if (icon is ApkIcon.Adaptive) {
                    println(icon.background::class.simpleName)
                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
                } else {
                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
                }
            }
        }

    } ?: exitProcess(-1)

}
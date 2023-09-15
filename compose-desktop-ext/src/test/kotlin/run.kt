@file:JvmName("run")
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.heizi.apk.parser.ktx.Image
import me.heizi.apk.parser.ktx.toImageVector
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
import kotlin.system.exitProcess

var time = System.currentTimeMillis()
fun updateTime(msg:String="") = System.currentTimeMillis().also { println("Time: ${it - time}ms $msg") }.also { time = it }

suspend fun main(args: Array<String>) {
    updateTime("init")
    // init
    coroutineScope {
        launch {
            updateTime("init by coroutine lunched")
        }
    }
    args.getOrNull(0)?.let {
        updateTime("parse")
        ApkFile(it)
            .icons
//            .map {
//                if (it is ApkIcon.Vector) {
//
//                    println(it.data)
//                }
//                it
//            }
            .takeIf { it.isNotEmpty() }
    } ?.let {icons->
        updateTime("starting new single windows application")
        singleWindowApplication {
            Row {
//                val icon = icons.filter { it.density>=0 }.minBy { it.density }
                val icon = icons.find { it is ApkIcon.Adaptive }?:icons.first()
                println(icon::class.simpleName)

                println(icon.data::class)
                if (icon is ApkIcon.Adaptive) {
                    if (icon.data.background is ApkIcon.Raster) {
                        val background:ApkIcon.Raster = icon.data.background as ApkIcon.Raster
                        println(background.data.size)
                        println(background.path)
                        exitProcess(0)
                    }
                    println(icon.data.background::class.simpleName)
//                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
//                } else {
//                    Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
                }
//                Image(icon, modifier = Modifier.sizeIn(minWidth = 128.dp, minHeight = 128.dp).fillMaxSize())
                icons.forEach {
                    println(it)
                    println(it.density)
                    var image: ApkIcon<out Any>? = icon
                    val density = LocalDensity.current
                    LaunchedEffect(it) {
                        println("inside l")
                        if (image is ApkIcon.Adaptive ) {
                            image = (image as ApkIcon.Adaptive).data.foreground
                        }
                        if (image is ApkIcon.Vector) {
                            println("inside")
                            image = image.runCatching {
                                (this as ApkIcon.Vector).toImageVector(density)
                                image
                            }.onFailure {
                                it.printStackTrace()
                            }.getOrNull()
                        }
                    }
                    println("outside")
                    if (image!==null) {
                        Image(image!!)
                    }

                }
                updateTime("draw")
            }
            LaunchedEffect("launch") {
                updateTime("launch")
            }
        }

    } ?: exitProcess(-1)

}
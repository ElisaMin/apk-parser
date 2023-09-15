@file:JvmName("run")
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.heizi.apk.parser.ktx.Image
import me.heizi.apk.parser.ktx.toImageVector
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.IconTypes
import net.dongliu.apk.parser.bean.IconResource
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
                val icon = icons.find { it is IconTypes.Adaptive }?:icons.first()
                println(icon::class.simpleName)

                println(icon.data.javaClass)
                if (icon is IconTypes.Adaptive) {
                    if (icon.data.background is IconTypes.Raster) {
                        val background:IconTypes.Raster = icon.data.background as IconTypes.Raster
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
                    var image: IconResource? = icon
                    val density = LocalDensity.current
                    LaunchedEffect(it) {
                        println("inside l")
                        if (image is IconTypes.Adaptive ) {
                            image = (image as IconTypes.Adaptive).data.foreground
                        }
                        if (image is IconTypes.Vector) {
                            println("inside")
                            image = image.runCatching {
                                (this as IconTypes.Vector).toImageVector(density)
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
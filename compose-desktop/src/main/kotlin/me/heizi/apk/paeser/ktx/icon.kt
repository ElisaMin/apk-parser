package me.heizi.apk.paeser.ktx

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.singleWindowApplication
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.AndroidIcons
import net.dongliu.apk.parser.bean.toImage
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream


@Composable
fun rememberVectorXmlResource(
    xml:String,
): Painter {
    val density = LocalDensity.current
    val image = remember(xml,density) {
       ByteArrayInputStream(xml.toByteArray()).use {
           loadXmlImageVector(InputSource(it),density)
       }
    }
    return rememberVectorPainter(image)
}

fun AndroidIcons.Vector.toImageVector(density:Density): ImageVector =
    ByteArrayInputStream(data.replace("olor=\"","olor=\"#").toByteArray()).use {
        println(data)
        loadXmlImageVector(InputSource(it),density)
    }
val emptyBitmapPainter = BitmapPainter(ImageBitmap(0,0))
@Composable
fun AndroidIcons<*>?.Paint(density: Density = LocalDensity.current):Painter = when(this) {
    is AndroidIcons.Adaptive -> foreground.Paint(density)
    is AndroidIcons.Raster -> toImage().toPainter()
    is AndroidIcons.Vector -> rememberVectorPainter(toImageVector(density))
    else-> emptyBitmapPainter
}
@Composable
fun rememberIconList():List<Painter> {
    val list = remember { mutableStateListOf<Painter>() }
    return list
}

fun main(args: Array<String>) = singleWindowApplication {

    AndroidIcons.Empty("")

    val icons = remember {
        ApkFile(args[0]).icons
    }
    val density = LocalDensity.current

    Row {
        for (i in icons) Image(i.Paint(density),"")
    }


//    Box(Modifier.height(128.dp).width(128.dp)) {
////        Icon(b,"background", modifier = Modifier.fillMaxSize())
//        Image(rememberVectorPainter(b),"b", modifier = Modifier.fillMaxSize())
////        Icon(f,"foreground", tint = Color.White, modifier = Modifier.fillMaxSize())
//        Image(rememberVectorPainter(f),"f", modifier = Modifier.fillMaxSize())
//    }

}
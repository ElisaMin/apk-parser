package me.heizi.apk.paeser.ktx

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
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

fun ApkIcon.Vector.toImageVector(density:Density): ImageVector =
    ByteArrayInputStream(data.toByteArray()).use {
//        println(data)
        loadXmlImageVector(InputSource(it),density)
    }
val emptyBitmapPainter = BitmapPainter(ImageBitmap(0,0))
@Composable
fun ApkIcon<*>?.Paint(density: Density = LocalDensity.current):Painter = when(this) {
    is ApkIcon.Raster -> remember(path,data.toString()) { toImage().toPainter() }
    is ApkIcon.Vector -> rememberVectorPainter(toImageVector(density))
    is ApkIcon.Color -> remember("ApkIconColorPainter",data) {
         ColorPainter(Color(data.removePrefix("#").toLong(16) or 0x00000000FF000000))
    }
    is ApkIcon.Adaptive-> emptyBitmapPainter
    else-> emptyBitmapPainter
}
@Suppress("NAME_SHADOWING")
@Composable
fun Image(
    icons: ApkIcon<*>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val icons = remember { icons }
    if (icons is ApkIcon.Adaptive) {
        val background = icons.background.Paint(density)
        val foreground = icons.foreground.Paint(density)
        Box(modifier) {
            Image(
                background,
                contentDescription,
                modifier,
                alignment,
                contentScale,
                alpha,
                colorFilter
            )
            Image(
                foreground,
                contentDescription,
                modifier,
                alignment,
                contentScale,
                alpha,
                colorFilter
            )
        }
    }else {
        val icon = icons.Paint(density)
        Image(icon, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
    }
}

@Composable
fun rememberIconList():List<Painter> {
    val list = remember { mutableStateListOf<Painter>() }
    return list
}

fun main(args: Array<String>) = singleWindowApplication {


    val icons = remember {
        ApkFile(args[0]).icons
    }

    Row(modifier = Modifier.background(Color.LightGray).fillMaxSize()) {
        for (i in icons) {
            if (i is ApkIcon.Adaptive) Image(i,"")
        }
    }


}
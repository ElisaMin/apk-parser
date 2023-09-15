package me.heizi.apk.parser.ktx

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import net.dongliu.apk.parser.bean.IconResource
import net.dongliu.apk.parser.bean.IconTypes
import net.dongliu.apk.parser.bean.toImage
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream

@Composable
fun Image(
    icon: IconTypes.Adaptive,
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    alignment: Alignment = Alignment.Center,
    contentDescription: String? = icon::class.simpleName + " " + icon.path,
    backgroundModifier: Modifier = Modifier,
    foregroundModifier: Modifier = Modifier,
    backgroundContentScale: ContentScale = ContentScale.Fit,
    foregroundContentScale: ContentScale = ContentScale.Fit,
    backgroundColorFilter: ColorFilter? = null,
    foregroundColorFilter: ColorFilter? = null,
    backgroundAlpha: Float = DefaultAlpha,
    foregroundAlpha: Float = DefaultAlpha,
    backgroundAlignment: Alignment = Alignment.Center,
    foregroundAlignment: Alignment = Alignment.Center,
) {
    val foreground = rememberApkIconPainter(icon.data.foreground,density)
    val background = rememberApkIconPainter(icon.data.background,density)
    Box(modifier,alignment,) {
        Image(background,contentDescription,backgroundModifier,backgroundAlignment,backgroundContentScale,backgroundAlpha,backgroundColorFilter)
        Image(foreground,contentDescription,foregroundModifier,foregroundAlignment,foregroundContentScale,foregroundAlpha,foregroundColorFilter)
    }
}


@Suppress("NAME_SHADOWING")
@Composable
fun Image(
    icon: IconResource,
    contentDescription: String? = icon::class.simpleName + " " + icon.path,
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val icon = remember { icon }
    if (icon is IconTypes.Adaptive) {
        Image(icon,modifier,density,alignment,contentDescription,
            modifier,modifier,contentScale,contentScale,colorFilter,
            colorFilter,alpha,alpha,alignment,alignment
        )
    } else {
        val painter = rememberApkIconPainter(icon, density)
        Image(painter, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
    }
}

class ParseVectorIconException(message:String?=null,cause:Throwable?=null):Exception(message,cause)

fun IconTypes.Vector.toImageVector(density:Density): ImageVector = data.replace(Regex("""android:([^=]*)="([^"]*)"""") ) { replacement ->
    val attr = replacement.groupValues[1]
    var value = replacement.groupValues[2]
//    println()
//    print(attr to value)
    when {
        attr.contains("Color") -> {
            if (!value.startsWith("#")) {
                value = value.toIntOrNull(16)?.let {
                    "#$it"
                } ?: "#FF000000"
            }
        }
        attr == "fillType" -> {
            if (value !="evenOdd" && value !="nonZero") {
                value = if (value == "0") "evenOdd" else "nonZero"
            }
        }
    }
//    print(" - after - ")
//    println(attr to value)
    """android:$attr="$value""""
}.toByteArray().let(::ByteArrayInputStream).use {
    runCatching {
        loadXmlImageVector(InputSource(it),density)
    }.onFailure {
        throw ParseVectorIconException(cause = it,message = it.message)
    }.getOrThrow()
}

private val emptyBitmapPainter = BitmapPainter(ImageBitmap(0,0))

//fun ApkIcon.Raster.toPainter(): Painter = toImage()!!.toPainter()
//fun ApkIcon.Vector.composeVector(density: Density) = toImageVector(density)
//fun ApkIcon.Color.toPainter() = ColorPainter(color)
//fun ApkIcon.Adaptive.toPainter() = emptyBitmapPainter


@Composable
fun rememberApkIconPainter(
    icon: IconResource?,
    density: Density = LocalDensity.current
):Painter = when(icon) {
    is IconTypes.Raster -> remember(icon.path,icon.data.toString()) { icon.toImage()!!.toPainter() }
    is IconTypes.Vector -> rememberVectorPainter(icon.toImageVector(density))
    is IconTypes.Color -> remember("ApkIconColorPainter",icon.data) {
         ColorPainter(icon.color)
    }
    is IconTypes.Adaptive-> emptyBitmapPainter
    else-> emptyBitmapPainter
}

val IconTypes.Color.color get() = Color(value)

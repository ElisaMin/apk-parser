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
import net.dongliu.apk.parser.bean.ApkIcon
import net.dongliu.apk.parser.bean.toImage
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream

@Composable
fun Image(
    icon: ApkIcon.Adaptive,
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
    val foreground = rememberApkIconPainter(icon.foreground,density)
    val background = rememberApkIconPainter(icon.background,density)
    Box(modifier,alignment,) {
        Image(background,contentDescription,backgroundModifier,backgroundAlignment,backgroundContentScale,backgroundAlpha,backgroundColorFilter)
        Image(foreground,contentDescription,foregroundModifier,foregroundAlignment,foregroundContentScale,foregroundAlpha,foregroundColorFilter)
    }
}


@Suppress("NAME_SHADOWING")
@Composable
fun Image(
    icon: ApkIcon<*>,
    contentDescription: String? = icon::class.simpleName + " " + icon.path,
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val icon = remember { icon }
    if (icon is ApkIcon.Adaptive) {
        Image(icon,modifier,density,alignment,contentDescription,
            modifier,modifier,contentScale,contentScale,colorFilter,
            colorFilter,alpha,alpha,alignment,alignment
        )
    } else {
        val painter = rememberApkIconPainter(icon, density)
        Image(painter, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
    }
}

private fun ApkIcon.Vector.toImageVector(density:Density): ImageVector =
    ByteArrayInputStream(data.toByteArray()).use {
        loadXmlImageVector(InputSource(it),density)
    }


val emptyBitmapPainter = BitmapPainter(ImageBitmap(0,0))


@Composable
private fun rememberApkIconPainter(
    icon: ApkIcon<*>?,
    density: Density = LocalDensity.current
):Painter = when(icon) {
    is ApkIcon.Raster -> remember(icon.path,icon.data.toString()) { icon.toImage()!!.toPainter() }
    is ApkIcon.Vector -> rememberVectorPainter(icon.toImageVector(density))
    is ApkIcon.Color -> remember("ApkIconColorPainter",icon.data) {
         ColorPainter(Color(icon.data.removePrefix("#").toLong(16) or 0x00000000FF000000))
    }
    is ApkIcon.Adaptive-> emptyBitmapPainter
    else-> emptyBitmapPainter
}

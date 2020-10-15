@file:JvmName("ImageUtils")
package ru.biomedis.biomedismair3.utils

import javafx.embed.swing.SwingFXUtils
import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO


fun toHtmlBase64(img: String) = "data:image/png;base64,$img"

fun fromBase64ToInputStream(encodedString: String): InputStream = ByteArrayInputStream(Base64.getDecoder().decode(encodedString))

fun inputStreamToImage(inputStream: InputStream) = Image(inputStream)

fun imageFromBase64(src: String)= inputStreamToImage(fromBase64ToInputStream(src))

fun imageViewToBase64(imageView: ImageView, isPng: Boolean) = writableImageToBase64(writableImageFromImageView(imageView, isPng), isPng)

fun writableImageFromImageView(imageView: ImageView, isPng: Boolean): WritableImage {
    val parameters = SnapshotParameters()
    if (isPng) parameters.fill = Color.TRANSPARENT else parameters.fill = Color.WHITE
    val wi = WritableImage(imageView.image.width.toInt(), imageView.image.height.toInt())
    imageView.snapshot(parameters, wi)
    return wi
}

fun writableImageToBase64(writableImage: WritableImage, isPng: Boolean): String = ByteArrayOutputStream().let {
    writableImageToStream(writableImage, isPng, it)
    Base64.getEncoder().encodeToString(it.toByteArray())
}


fun writableImageToStream(writableImage: WritableImage, isPng: Boolean, stream: OutputStream) {

    if (isPng) {
        ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", stream)

    } else {

        // save image (without alpha). Из-за глюка что в jpg записыается альфа
        // --------------------------------
        val bufImageARGB = SwingFXUtils.fromFXImage(writableImage, null)
        val bufImageRGB = BufferedImage(bufImageARGB.width, bufImageARGB.height, BufferedImage.OPAQUE)
        val graphics = bufImageRGB.createGraphics()
        graphics.drawImage(bufImageARGB, 0, 0, null)
        try {
            ImageIO.write(bufImageRGB, "jpg", stream)
        } catch (e: IOException) {
            throw e
        } finally {
            graphics.dispose()
        }
    }

}

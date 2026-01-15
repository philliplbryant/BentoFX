/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui.docking

import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import software.coley.bentofx.dockable.Dockable
import software.coley.bentofx.dockable.DockableIconFactory
import java.io.IOException
import java.nio.file.Path
import kotlin.Double.Companion.MAX_VALUE
import kotlin.io.path.absolute
import kotlin.math.max

/**
 * [Dockable] implementation that uses images, for demonstration purposes,
 * that represent actual application components.
 */
class JreDockable(
    jreDockableInfo: JreDockableInfo,
) : Dockable(JreBento, jreDockableInfo.id) {

    companion object {
        val LOGGER = LoggerFactory.getLogger(JreDockable::class.java.name)
    }

    init {

        title = jreDockableInfo.text

        if (jreDockableInfo.dockHandleImagePath != null) {
            iconFactory = DockableIconFactory {
                createImageView(
                    jreDockableInfo.dockHandleImagePath,
                    16.0,
                    16.0
                )
            }
        }

        if (jreDockableInfo.contentImagePath != null) {
            node = createImageView(
                jreDockableInfo.contentImagePath,
                jreDockableInfo.contentWidth,
                jreDockableInfo.contentHeight,
                true
            )
        }
    }

    /**
     * @return an [ImageView] created from an image file at the specified
     * [contentImagePath] with the specified [contentWidth] and [contentHeight].
     * @param contentImagePath the [Path], relative to the application classpath,
     * to an image file to be used as the content for the [ImageView].
     * @param contentWidth the width of the bounding box within which the
     * source image is resized as necessary to fit the [ImageView].
     * @param contentHeight the height of the bounding box within which the
     * source image is resized as necessary to fit the [ImageView].
     */
    fun createImageView(
        contentImagePath: Path,
        contentWidth: Double,
        contentHeight: Double,
        isResizable: Boolean = false,
    ): Node? {

        try {
            val imageResource: Resource =
                ClassPathResource(contentImagePath.toString())
            val image = Image(imageResource.inputStream)
            val imageView = ImageView(image)

            return if (isResizable) {
                imageView.apply {
                    isPreserveRatio = true
                    isSmooth = true
                }

                val container = StackPane(imageView).apply {
                    setMinSize(contentWidth, contentHeight)
                    setPrefSize(contentWidth, contentHeight)
                    setMaxSize(MAX_VALUE, MAX_VALUE)
                }

                imageView.fitWidthProperty().bind(container.widthProperty())
                imageView.fitHeightProperty().bind(container.heightProperty())

                container.layoutBoundsProperty().addListener { _, _, bounds ->

                    val img = imageView.image ?: return@addListener

                    if (bounds.width <= 0.0 || bounds.height <= 0.0) return@addListener

                    val scale = max(
                        bounds.width / img.width,
                        bounds.height / img.height
                    )

                    val viewportWidth = bounds.width / scale
                    val viewportHeight = bounds.height / scale

                    val x = (img.width - viewportWidth) / 2.0
                    val y = (img.height - viewportHeight) / 2.0

                    imageView.viewport = Rectangle2D(
                        x,
                        y,
                        viewportWidth,
                        viewportHeight
                    )
                }
                container
            } else {

                imageView.fitWidth = contentWidth
                imageView.fitHeight = contentHeight
                imageView
            }
        } catch (e: IOException) {

            LOGGER.warn(
                "Could not create image from ${contentImagePath.absolute()}",
                e
            )
            return null
        }
    }
}

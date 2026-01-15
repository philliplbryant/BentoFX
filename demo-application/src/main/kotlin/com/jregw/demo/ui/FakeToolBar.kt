/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui

import com.jregw.demo.ApplicationConstants.FAKE_TOOLBAR_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.MOCK_CONTENT_DIRECTORY
import com.jregw.demo.ui.docking.JreDockableInfo
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.nio.file.Paths

/**
 * An [ImageView] representing a 'ToolBar' (used in lieu of creating an actual
 * `Toolbar`).
 */
object FakeToolBar : ImageView() {

    /**
     * Because this is a "fake" toolbar, there's no point in displaying it if it
     * isn't initialized (which entails loading an image representing a toolbar).
     */
    var isInitialized = false
        private set

    init {

        val fakeToolBarInfo = JreDockableInfo(
            id = FAKE_TOOLBAR_DOCKABLE_ID,
            text = "Fake ToolBar",
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/fake-tool-bar.png"),
            contentWidth = 450.0,
            contentHeight = 25.0,
        )

        try {
            if (fakeToolBarInfo.contentImagePath != null) {
                val imageResource: Resource =
                    ClassPathResource(fakeToolBarInfo.contentImagePath.toString())
                this.image = Image(imageResource.inputStream)
                fitWidth = fakeToolBarInfo.contentWidth
                fitHeight = fakeToolBarInfo.contentHeight
                isInitialized = true
            }
        } catch (e: Exception) {
            val logger = LoggerFactory.getLogger(FakeToolBar.javaClass.name)
            logger.warn(
                "Could not initialize ${FakeToolBar.javaClass.name} class",
                e
            )
        }
    }
}

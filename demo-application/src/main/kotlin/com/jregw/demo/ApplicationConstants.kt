/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo

/**
 * Constants used by multiple application entities.
 */
object ApplicationConstants {

    private const val IMAGES_DIRECTORY = "images"

    internal const val APPLICATION_TITLE = "Joint Range Extension Gateway (BentoFX)"

    internal const val CSS_FILE_NAME = "dark-theme.css"

    const val ICON_FILE_DIRECTORY = "$IMAGES_DIRECTORY/icons"
    const val MOCK_CONTENT_DIRECTORY = "$IMAGES_DIRECTORY/mock-tab-content"

    const val STAGE_IMAGE_PATH = "$ICON_FILE_DIRECTORY/jre-48x48.png"

    const val DEFAULT_STAGE_WIDTH = 1420.0
    const val DEFAULT_STAGE_HEIGHT = 900.0

    internal const val APPLICATION_PANE_ID = "applicationPane"
    const val STATUS_PANE_ID = "statusPane"
    const val STATUS_TOP_PANE_ID = "statusTopPane"
    const val STATUS_BOTTOM_PANE_ID = "statusBottomPane"

    internal const val LEFT_PANE_ID = "leftPane"
    internal const val LEFT_TOP_PANE_ID = "leftTopPane"
    internal const val LEFT_BOTTOM_PANE_ID = "leftBottomPane"

    internal const val RIGHT_PANE_ID = "rightPane"
    internal const val RIGHT_TOP_PANE_ID = "rightTopPane"

    internal const val RIGHT_BOTTOM_PANE_ID = "rightBottomPane"
    internal const val RIGHT_BOTTOM_LEFT_PANE_ID = "rightBottomLeftPane"
    internal const val RIGHT_BOTTOM_RIGHT_PANE_ID = "rightBottomRightPane"

    const val FAKE_TOOLBAR_DOCKABLE_ID = "fakeToolBarDockable"
    const val STATUS_DOCKABLE_ID = "statusDockable"
    const val HOOKED_TRACKS_DOCKABLE_ID = "hookedTracksDockable"
    const val LINKS_DOCKABLE_ID = "linksDockable"
    const val MAP_DOCKABLE_ID = "mapDockable"
    const val MAP_OVERVIEW_DOCKABLE_ID = "mapOverviewDockable"
    const val OTHER_SA_DOCKABLE_ID = "otherSaDockable"
    const val SYSTEM_TRACK_COUNTS_DOCKABLE_ID = "systemTrackCountsDockable"
    const val TRACKS_DOCKABLE_ID = "tracksDockable"

    const val FILE_EXTENSION_BEAN_NAME = "fileExtension"

    @JvmField
    val DEFAULT_BENTO_DIRECTORY = "${System.getProperty("user.home")}/.bentofx"
    const val DEFAULT_BENTO_FILE_NAME = "recent-bento"
    const val JSON_FILE_EXTENSION = "json"
    const val XML_FILE_EXTENSION = "xml"
}

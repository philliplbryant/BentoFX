/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui.docking

import com.jregw.demo.ApplicationConstants.HOOKED_TRACKS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.ICON_FILE_DIRECTORY
import com.jregw.demo.ApplicationConstants.LINKS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.MAP_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.MAP_OVERVIEW_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.MOCK_CONTENT_DIRECTORY
import com.jregw.demo.ApplicationConstants.OTHER_SA_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.STATUS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.SYSTEM_TRACK_COUNTS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.TRACKS_DOCKABLE_ID
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Data class encapsulating attributes of a dockable user interface component.
 *
 * @param id an identifier that is unique to the application.
 * @param text text, that is suitable to display to the user, for identifying
 * the component.
 * @param dockHandleImagePath the path to the image to use for the docking
 * handle (e.g. the image displayed in a `Tab` in a `TabPane`).
 * @param contentImagePath the path to the image to use for the content of a
 * `JreDockable`
 * @param contentWidth the width of the content in a `JreDockable`.
 * @param contentHeight the height of the content in a `JreDockable`.
 */
data class JreDockableInfo(
    val id: String,
    val text: String,
    val dockHandleImagePath: Path? = null,
    val contentImagePath: Path? = null,
    val contentWidth: Double,
    val contentHeight: Double,
) {
    companion object {

        val STATUS_DOCKABLE_INFO = JreDockableInfo(
            id = STATUS_DOCKABLE_ID,
            text = "Status",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/status-55x55.png"),
            contentWidth = 338.0,
            contentHeight = 513.0,
        )

        val HOOKED_TRACKS_DOCKABLE_INFO = JreDockableInfo(
            id = HOOKED_TRACKS_DOCKABLE_ID,
            text = "Hooked Tracks",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/hooked-tracks-55x55.png"),
            contentWidth = 150.0,
            contentHeight = 340.0,
        )

        val LINKS_DOCKABLE_INFO = JreDockableInfo(
            id = LINKS_DOCKABLE_ID,
            text = "Links",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/links-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-links-tab.png"),
            contentWidth = 100.0,
            contentHeight = 100.0,
        )

        val MAP_DOCKABLE_INFO = JreDockableInfo(
            id = MAP_DOCKABLE_ID,
            text = "Map",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/map-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-map-tab.png"),
            contentWidth = 400.0,
            contentHeight = 180.0,
        )

        val MAP_OVERVIEW_DOCKABLE_INFO = JreDockableInfo(
            id = MAP_OVERVIEW_DOCKABLE_ID,
            text = "Map Overview",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/map-overview-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-map-overview-tab.png"),
            contentWidth = 170.0,
            contentHeight = 75.0,
        )

        val OTHER_SA__DOCKABLE_INFO = JreDockableInfo(
            id = OTHER_SA_DOCKABLE_ID,
            text = "Other SA",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/situational-awareness-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-other-sa-tab.png"),
            contentWidth = 500.0,
            contentHeight = 50.0,
        )

        val SYSTEM_TRACK_COUNTS = JreDockableInfo(
            id = SYSTEM_TRACK_COUNTS_DOCKABLE_ID,
            text = "System Track Counts",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/system-track-counts-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-system-track-counts-tab.png"),
            contentWidth = 210.0,
            contentHeight = 225.0,
        )

        val TRACKS_DOCKABLE_INFO = JreDockableInfo(
            id = TRACKS_DOCKABLE_ID,
            text = "Tracks",
            dockHandleImagePath = Paths.get("$ICON_FILE_DIRECTORY/tracks-55x55.png"),
            contentImagePath = Paths.get("$MOCK_CONTENT_DIRECTORY/mock-tracks-tab.png"),
            contentWidth = 810.0,
            contentHeight = 210.0,
        )
    }
}

/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui.docking

import com.jregw.demo.ApplicationConstants.STAGE_IMAGE_PATH
import com.jregw.demo.ApplicationConstants.STATUS_BOTTOM_PANE_ID
import com.jregw.demo.ApplicationConstants.STATUS_PANE_ID
import com.jregw.demo.ApplicationConstants.STATUS_TOP_PANE_ID
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.HOOKED_TRACKS_DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.LINKS_DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.MAP_DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.MAP_OVERVIEW_DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.OTHER_SA__DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.STATUS_DOCKABLE_INFO
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.SYSTEM_TRACK_COUNTS
import com.jregw.demo.ui.docking.JreDockableInfo.Companion.TRACKS_DOCKABLE_INFO
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.image.Image
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import software.coley.bentofx.building.DockBuilding
import software.coley.bentofx.dockable.Dockable
import software.coley.bentofx.layout.DockContainer
import software.coley.bentofx.persistence.api.DockableResolver
import java.io.IOException
import java.util.Optional

/**
 * [software.coley.bentofx.persistence.api.DockableResolver] implementation for getting or creating [Dockable] instances
 * and other user interface components.
 * @param builder [DockBuilding] to use for building [DockContainer] and [Dockable] instances.
 */
class JreDockableResolver(private val builder: DockBuilding) : DockableResolver {

    private companion object {

        private val LOGGER = LoggerFactory.getLogger(JreDockableResolver::class.java.name)

        private val stageImage = try {
            val imageResource: Resource =
                ClassPathResource(STAGE_IMAGE_PATH)
            Image(imageResource.inputStream)
        } catch (e: IOException) {
            LOGGER.warn(
                "Failed to load stage image $STAGE_IMAGE_PATH",
                e
            )
            null
        }
    }

    init {
        // Initialization of these values must be performed on the JavaFX
        // Application Thread because they create JavaFX components.
        Platform.runLater {
            mapOverviewDockable = JreDockable(MAP_OVERVIEW_DOCKABLE_INFO)
            systemTrackCountsDockable = JreDockable(SYSTEM_TRACK_COUNTS)

            jreDockableInstanceMap = mapOf(
                HOOKED_TRACKS_DOCKABLE_INFO.id to JreDockable(HOOKED_TRACKS_DOCKABLE_INFO),
                LINKS_DOCKABLE_INFO.id to JreDockable(LINKS_DOCKABLE_INFO),
                TRACKS_DOCKABLE_INFO.id to JreDockable(TRACKS_DOCKABLE_INFO),
                MAP_DOCKABLE_INFO.id to JreDockable(MAP_DOCKABLE_INFO),
                OTHER_SA__DOCKABLE_INFO.id to JreDockable(OTHER_SA__DOCKABLE_INFO),
                MAP_OVERVIEW_DOCKABLE_INFO.id to mapOverviewDockable,
                SYSTEM_TRACK_COUNTS.id to systemTrackCountsDockable,
                STATUS_DOCKABLE_INFO.id to createStatusDockable(),
            )
        }
    }

    private lateinit var mapOverviewDockable: Dockable
    private lateinit var systemTrackCountsDockable: Dockable
    private lateinit var jreDockableInstanceMap: Map<String, Dockable>

    override fun resolveDockable(id: String?): Optional<Dockable> {
        return Optional.ofNullable(jreDockableInstanceMap[id])
    }

    override fun getDefaultDragDropStageIcon(): Optional<Image> = Optional.ofNullable(stageImage)

    /**
     * The status dockable is a Singleton split pane containing nested dockable
     * items that can be docked/undocked as a single unit.
     */
    private fun createStatusDockable(): JreDockable {

        val statusDockable = JreDockable(STATUS_DOCKABLE_INFO)

        // SplitPane containers hold other containers as (optionally) resizable
        // children. Create a split on the left of the frame that holds other
        // split panes.
        val statusTopPane = builder.leaf(STATUS_TOP_PANE_ID)
        statusTopPane.addDockables(mapOverviewDockable)

        val statusBottomPane = builder.leaf(STATUS_BOTTOM_PANE_ID)
        statusBottomPane.addDockables(systemTrackCountsDockable)

        // Leaves can be set to NOT auto-expand when they are intended to be a
        // set size.
        // DockContainerBranch.setResizableWithParent(statusTopPane, false)
        // DockContainerBranch.setResizableWithParent(statusBottomPane, false)

        // Create a nested dockable container branch to hold dockable leaves.
        val statusBranchContainer = builder.branch(STATUS_PANE_ID)
        statusBranchContainer.addContainers(statusTopPane, statusBottomPane)
        statusBranchContainer.orientation = Orientation.VERTICAL
        statusBranchContainer.setDividerPositions(0.30)

        // Create a dockable to wrap the Status container so it can be added
        // to a leaf
        statusDockable.node = statusBranchContainer

        return statusDockable
    }
}

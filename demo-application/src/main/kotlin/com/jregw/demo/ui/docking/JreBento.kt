/*******************************************************************************
 This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui.docking

import javafx.scene.control.Label
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.coley.bentofx.Bento
import software.coley.bentofx.dockable.DockablePlaceholderFactory
import software.coley.bentofx.event.DockEvent

/**
 * Top level controller for docking operations.
 */
object JreBento : Bento() {

    val LOGGER: Logger = LoggerFactory.getLogger(javaClass.name)

    init {
        // Placeholder factory for dockables with no content to show.
        placeholderBuilding().dockablePlaceholderFactory =
            DockablePlaceholderFactory {
                Label("Empty Dockable")
            }

        // Placeholder factory for containers with no content to show.
        placeholderBuilding()
            .setContainerPlaceholderFactory {
                Label("Empty Container")
            }

        // Add a generic event handler to the Bento event bus
        events().addEventListener { dockEvent: DockEvent ->

            LOGGER.trace(dockEvent.toString())
        }
    }
}

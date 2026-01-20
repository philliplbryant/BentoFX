/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo

import com.jregw.demo.ApplicationConstants.APPLICATION_PANE_ID
import com.jregw.demo.ApplicationConstants.APPLICATION_TITLE
import com.jregw.demo.ApplicationConstants.DEFAULT_STAGE_HEIGHT
import com.jregw.demo.ApplicationConstants.DEFAULT_STAGE_WIDTH
import com.jregw.demo.ApplicationConstants.HOOKED_TRACKS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.LEFT_BOTTOM_PANE_ID
import com.jregw.demo.ApplicationConstants.LEFT_PANE_ID
import com.jregw.demo.ApplicationConstants.LEFT_TOP_PANE_ID
import com.jregw.demo.ApplicationConstants.LINKS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.MAP_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.OTHER_SA_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.RIGHT_BOTTOM_LEFT_PANE_ID
import com.jregw.demo.ApplicationConstants.RIGHT_BOTTOM_PANE_ID
import com.jregw.demo.ApplicationConstants.RIGHT_BOTTOM_RIGHT_PANE_ID
import com.jregw.demo.ApplicationConstants.RIGHT_PANE_ID
import com.jregw.demo.ApplicationConstants.RIGHT_TOP_PANE_ID
import com.jregw.demo.ApplicationConstants.STATUS_DOCKABLE_ID
import com.jregw.demo.ApplicationConstants.TRACKS_DOCKABLE_ID
import com.jregw.demo.configuration.BentoFxDemoApplicationConfiguration
import com.jregw.demo.configuration.FileLayoutStorageConfiguration
import com.jregw.demo.configuration.XmlLayoutCodecConfiguration
import com.jregw.demo.ui.FakeToolBar
import com.jregw.demo.ui.Menu
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.geometry.Side.BOTTOM
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.WebApplicationType.NONE
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import software.coley.bentofx.building.DockBuilding
import software.coley.bentofx.layout.container.DockContainerRootBranch
import software.coley.bentofx.persistence.api.DockableResolver
import software.coley.bentofx.persistence.api.LayoutRestorer
import software.coley.bentofx.persistence.api.LayoutSaver
import software.coley.bentofx.persistence.api.codec.BentoStateException
import software.coley.bentofx.persistence.api.storage.LayoutStorage
import software.coley.bentofx.persistence.impl.codec.common.FxStageUtils.*
import java.util.logging.LogManager
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess

/**
 * Spring enabled JavaFX application that demonstrates using the BentoFX
 * docking framework.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(
    BentoFxDemoApplicationConfiguration::class,
    // TODO BENTO-13: Choose ONLY one LayoutStorage configuration and
    //DatabaseLayoutStorageConfiguration::class,
    FileLayoutStorageConfiguration::class,
    // TODO BENTO-13: Choose ONLY one LayoutCodec configuration and
    //JsonLayoutCodecConfiguration::class,
    XmlLayoutCodecConfiguration::class,
)
class BentoFxDemoApplication : Application() {

    companion object {

        /**
         * Main entry point that launches the JavaFX [Application]
         * implementation.
         */
        @JvmStatic
        fun main(args: Array<String>) {

            // Initialize java.util.logging
            try {
                BentoFxDemoApplication::class.java.getResourceAsStream(
                    "/logging.properties"
                )?.use { inputStream ->
                    LogManager.getLogManager().readConfiguration(inputStream)
                } ?: run {
                    // logging.properties is not found
                    System.err.println(
                        "logging.properties not found. Using default Java " +
                                "Utility Logging configuration."
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace(System.err)
            }

            launch(BentoFxDemoApplication::class.java, *args)
        }

        private val LOGGER =
            LoggerFactory.getLogger(BentoFxDemoApplication::class.java)
    }

    // Variables injected into the main application class instance.
    private lateinit var applicationContext: ConfigurableApplicationContext
    private lateinit var builder: DockBuilding
    private lateinit var dockableResolver: DockableResolver
    private lateinit var layoutStorage: LayoutStorage
    private lateinit var layoutSaver: LayoutSaver
    private lateinit var layoutRestorer: LayoutRestorer

    /**
     * Configure SpringBoot before starting the JavaFX application.
     */
    override fun init() {

        // Disable Spring logging (to avoid Spring overriding SLF4J logging)
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none")

        // Initialize Spring context in a background thread
        applicationContext = SpringApplicationBuilder(
            BentoFxDemoApplication::class.java
        )
            // Instantiates AWT because Spring configurations create UI components
            .headless(false)
            // Turn off the Spring ASCII art banner
            .bannerMode(OFF)
            // Do not start an embedded web server.
            .web(NONE)
            // Inject dependencies and start the application
            .run()
    }

    /**
     * Configures, creates, and displays the user interface (called on the
     * JavaFX Application Thread).
     */
    override fun start(primaryStage: Stage) {

        // Initialize (inject) values from the Spring ApplicationContext
        builder = applicationContext.getBean<DockBuilding>()
        layoutStorage = applicationContext.getBean<LayoutStorage>()
        dockableResolver = applicationContext.getBean<DockableResolver>()
        layoutSaver = applicationContext.getBean<LayoutSaver>()
        layoutRestorer = applicationContext.getBean<LayoutRestorer>()

        // The primary pane will hold the application pane (subdivided into
        // split panes, tabbed panes, and tabs), menu, and toolbar.
        val primaryPane = BorderPane()

        // Configure the primary pane to combine the application pane, menu, and
        // toolbar (if initialized)
        primaryPane.top = if (FakeToolBar.isInitialized) {
            val actionsPane = BorderPane()
            actionsPane.top = Menu
            actionsPane.bottom = FakeToolBar
            actionsPane
        } else {
            Menu
        }

        // Add the primaryPane to the Scene before restoring the layout to avoid
        // NPE that occurs when accessing the Scene from a JavaFX Node before it
        // has been attached and placed within a Stage.
        primaryStage.scene = Scene(
            primaryPane,
            DEFAULT_STAGE_WIDTH,
            DEFAULT_STAGE_HEIGHT,
        )

        // If a prior layout hase been saved, restore the UI from its previous
        // state. Otherwise, show it with the default layout.
        if (layoutStorage.exists()) {
            try {
                primaryPane.center =
                    layoutRestorer.restoreLayout(
                        primaryStage
                    )

            } catch (e: BentoStateException) {

                LOGGER.warn(
                    "Could not restore the saved layout; using the " +
                            "default layout instead.",
                    e
                )

                primaryPane.center =
                    constructDefaultDockContainerRootBranch()
            }
        } else {

            primaryPane.center =
                constructDefaultDockContainerRootBranch()
        }

        configureAndShowPrimaryStage(primaryStage)
    }

    /**
     * Releases all resources, including cached Spring beans.
     */
    override fun stop() {
        applicationContext.close()
    }

    /**
     * Creates a [DockContainerRootBranch] that is split horizontally into left
     * and right panes.
     * <p>
     * The left pane is further split vertically, where the left top pane
     * contains the `Dockable` with id [STATUS_DOCKABLE_ID] and the left bottom
     * pane contains the `Dockable` with id [HOOKED_TRACKS_DOCKABLE_ID]. The
     * [STATUS_DOCKABLE_ID] is a nested `Dockable` that is further subdivided in
     * such a way as to allow undocking the nested components as a whole.
     * <p>
     * The right pane is also split vertically, where the right top pane
     * contains the `Dockable` with id [MAP_DOCKABLE_ID] and the right bottom
     * pane is further split horizontally. The right bottom left pane contains
     * the `Dockable`s with ids [LINKS_DOCKABLE_ID] and [TRACKS_DOCKABLE_ID] and
     * the right bottom right pane contains the `Dockable` with id
     * [OTHER_SA_DOCKABLE_ID].
     * ```
     * +----------------------------------+----------------------------------------------------------------------------+
     * | Left pane                        | Right pane                                                                 |
     * | +------------------------------+ | +------------------------------------------------------------------------+ |
     * | | Left top pane                | | | Right top pane                                                         | |
     * | | +--------------------------+ | | | +--------------------------------------------------------------------+ | |
     * | | | STATUS DOCKABLE (nested) | | | | | MAP DOCKABLE                                                       | | |
     * | | +--------------------------+ | | | +--------------------------------------------------------------------+ | |
     * | +------------------------------+ | +----------------------------------------------------------------------+ | |
     * | | Left bottom pane             | | | Right bottom pane                                                    | | |
     * | | +--------------------------+ | | | +--------------------------------------+ +-------------------------+ | | |
     * | | | HOOKED TRACKS DOCKABLE   | | | | | Right bottom left pane               | | Right bottom right pane | | | |
     * | | |                          | | | | | +----------------+-----------------+ | | +---------------------+ | | | |
     * | | |                          | | | | | | LINKS DOCKABLE | TRACKS DOCKABLE | | | | OTHER SA DOCKABLE   | | | | |
     * | | |                          | | | | | +----------------+-----------------+ | | +---------------------+ | | | |
     * | | +--------------------------+ | | | +--------------------------------------+ +-------------------------+ | | |
     * | +------------------------------+ | +------------------------------------------------------------------------+ |
     * +----------------------------------+----------------------------------------------------------------------------+
     * ```
     * @return a [DockContainerRootBranch] containing the UI components.
     */
    private fun constructDefaultDockContainerRootBranch():
            DockContainerRootBranch {

        // Add the status item to the top left pane
        val leftTopPane = builder.leaf(LEFT_TOP_PANE_ID)
        dockableResolver.resolveDockable(STATUS_DOCKABLE_ID)
            .ifPresent { dockable ->
                leftTopPane.addDockables(dockable)
            }

        val leftBottomPane = builder.leaf(LEFT_BOTTOM_PANE_ID)
        dockableResolver.resolveDockable(HOOKED_TRACKS_DOCKABLE_ID)
            .ifPresent { dockable ->
                leftBottomPane.addDockables(dockable)
            }

        // The left pane is intended to take up the entirety of the vertical
        // space, on the left side of the stage (excluding menu/toolbar).
        val leftPane = builder.branch(LEFT_PANE_ID)
        leftPane.addContainers(leftTopPane, leftBottomPane)
        leftPane.orientation = VERTICAL
        leftPane.setDividerPositions(0.61674)

        // Create a split for the bottom, divided into left/right. It will
        // occupy the lower portion of the right side.
        val rightBottomLeftPane = builder.leaf(RIGHT_BOTTOM_LEFT_PANE_ID)
        dockableResolver.resolveDockable(LINKS_DOCKABLE_ID)
            .ifPresent { dockable ->
                rightBottomLeftPane.addDockables(dockable)
            }
        dockableResolver.resolveDockable(TRACKS_DOCKABLE_ID)
            .ifPresent { dockable ->
                rightBottomLeftPane.addDockables(dockable)
            }
        rightBottomLeftPane.side = BOTTOM

        val rightBottomRightPane = builder.leaf(RIGHT_BOTTOM_RIGHT_PANE_ID)
        dockableResolver.resolveDockable(OTHER_SA_DOCKABLE_ID)
            .ifPresent { dockable ->
                rightBottomRightPane.addDockables(dockable)
            }
        rightBottomRightPane.side = BOTTOM

        val rightBottomPane = builder.branch(RIGHT_BOTTOM_PANE_ID)
        rightBottomPane.addContainers(rightBottomLeftPane, rightBottomRightPane)
        rightBottomPane.orientation = HORIZONTAL
        rightBottomPane.setDividerPositions(0.54835)

        // You can set containers to NOT allow splitting (mirroring intellij
        // behavior)
        // leftPane.isCanSplit = false
        // rightBottomPane.isCanSplit = false

        // Create a central pane that holds the map. It will occupy the upper
        // portion of the right side.
        val rightTopPane = builder.leaf(RIGHT_TOP_PANE_ID)
        dockableResolver.resolveDockable(MAP_DOCKABLE_ID).ifPresent { dockable ->
            rightTopPane.addDockables(dockable)
        }
        // Primary space should not prune when empty
        rightTopPane.setPruneWhenEmpty(false)

        // Set the mapPane and bottomPane on the right of the split so that
        // combination and the left pane will take up the entirety of the
        // vertical space (excluding menu/toolbar).
        val rightPane = builder.branch(RIGHT_PANE_ID)
        rightPane.addContainers(rightTopPane, rightBottomPane)
        rightPane.orientation = VERTICAL
        rightPane.setDividerPositions(0.75943)

        // SplitPane containers hold other containers as resizable children. The
        // application pane holds the split panes on the left/right.
        val applicationPane = builder.root(APPLICATION_PANE_ID)
        applicationPane.orientation = HORIZONTAL
        applicationPane.addContainers(leftPane, rightPane)
        applicationPane.setDividerPositions(0.20310)

        return applicationPane
    }

    /**
     * Configures the primary [Stage] by setting the title, image, event
     * handling, etc., and shows it.
     */
    private fun configureAndShowPrimaryStage(primaryStage: Stage) {

        primaryStage.properties[STAGE_ID_PROPERTY_KEY_NAME] = PRIMARY_STAGE_ID
        primaryStage.properties[IS_PRIMARY_STAGE_PROPERTY_KEY_NAME] = true

        primaryStage.title = APPLICATION_TITLE

        val stageImage: Image? =
            dockableResolver.getDefaultDragDropStageIcon().getOrNull()

        if (stageImage != null) {
            primaryStage.icons.add(stageImage)
        }

        primaryStage.setOnHidden {
            exitProcess(0)
        }

        primaryStage.onCloseRequest = EventHandler {
            layoutSaver.saveLayout()
        }

        primaryStage.show()
    }
}

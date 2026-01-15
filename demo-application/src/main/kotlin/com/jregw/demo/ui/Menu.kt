/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.ui

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar

/**
 * [MenuBar] that merely for show (its [Menu]s don't actually do anything).
 */
object Menu: MenuBar() {

    init {
        var fileMenu = Menu("_File")
        fileMenu.mnemonicParsingProperty().set(true)

        var setupMenu = Menu("Set_up")
        setupMenu.mnemonicParsingProperty().set(true)

        var controlMenu = Menu("_Control")
        controlMenu.mnemonicParsingProperty().set(true)

        var statusMenu = Menu("_Status")
        statusMenu.mnemonicParsingProperty().set(true)

        var toolsMenu = Menu("_Tools")
        toolsMenu.mnemonicParsingProperty().set(true)

        var nonC2Menu = Menu("Non-_C2")
        nonC2Menu.mnemonicParsingProperty().set(true)

        var tracksMenu = Menu("T_racks")
        tracksMenu.mnemonicParsingProperty().set(true)

        var symbolsMenu = Menu("S_ymbols")
        symbolsMenu.mnemonicParsingProperty().set(true)

        var mapMenu = Menu("_Map")
        mapMenu.mnemonicParsingProperty().set(true)

        var windowMenu = Menu("_Window")
        windowMenu.mnemonicParsingProperty().set(true)

        var helpMenu = Menu("_Help")
        helpMenu.mnemonicParsingProperty().set(true)

        menus.addAll(
            fileMenu,
            setupMenu,
            controlMenu,
            statusMenu,
            toolsMenu,
            nonC2Menu,
            tracksMenu,
            symbolsMenu,
            mapMenu,
            windowMenu,
            helpMenu,
        )
    }
}

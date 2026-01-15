/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/
package com.jregw.demo.configuration

import com.jregw.demo.ui.docking.JreBento
import com.jregw.demo.ui.docking.JreDockableResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.coley.bentofx.building.DockBuilding
import software.coley.bentofx.persistence.api.DockableResolver
import software.coley.bentofx.persistence.api.codec.LayoutCodec
import software.coley.bentofx.persistence.api.storage.LayoutStorage
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutRestorer
import software.coley.bentofx.persistence.impl.codec.common.BentoLayoutSaver

/**
 * Spring configuration for `BentoFxDemoApplication`.
 */
@Configuration
internal class BentoFxDemoApplicationConfiguration {

    @Bean
    fun dockBuilding(): DockBuilding =
        JreBento.dockBuilding()

    @Bean
    fun dockableResolver(dockBuilding: DockBuilding): DockableResolver =
        JreDockableResolver(dockBuilding)

    @Bean
    fun layoutSaver(
        layoutStorage: LayoutStorage,
        layoutCodec: LayoutCodec,
    ) = BentoLayoutSaver(
        layoutStorage,
        layoutCodec
    )

    @Bean
    fun layoutRestorer(
        layoutStorage: LayoutStorage,
        layoutCodec: LayoutCodec,
        dockBuilding: DockBuilding,
        dockableResolver: DockableResolver,
    ) = BentoLayoutRestorer(
        layoutStorage,
        layoutCodec,
        dockBuilding,
        dockableResolver
    )
}

/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package com.jregw.demo.configuration

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.coley.bentofx.persistence.api.storage.LayoutStorage
import software.coley.bentofx.persistence.impl.storage.db.DatabaseLayoutStorage

@Configuration
internal class DatabaseLayoutStorageConfiguration {

    @Bean
    fun entityManagerFactory(): EntityManagerFactory =
        Persistence.createEntityManagerFactory("layoutPU")

    @Bean
    fun layoutStorage(
        entityManagerFactory: EntityManagerFactory,
    ): LayoutStorage =
        DatabaseLayoutStorage(
            entityManagerFactory,
            "default",
        )
}

/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package com.jregw.demo.configuration

import com.jregw.demo.ApplicationConstants.DEFAULT_BENTO_FILE_NAME
import com.jregw.demo.ApplicationConstants.DEFAULT_BENTO_DIRECTORY
import com.jregw.demo.ApplicationConstants.FILE_EXTENSION_BEAN_NAME
import jakarta.inject.Named
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.coley.bentofx.persistence.api.storage.LayoutStorage
import software.coley.bentofx.persistence.impl.storage.file.FileLayoutStorage
import java.io.File

@Configuration
internal class FileLayoutStorageConfiguration {

    @Bean
    fun layoutStorage(
        @Named(FILE_EXTENSION_BEAN_NAME)
        fileExtension: String,
    ): LayoutStorage {
        val layoutFile = File(
            DEFAULT_BENTO_DIRECTORY,
            "$DEFAULT_BENTO_FILE_NAME.$fileExtension"
        )
        return FileLayoutStorage(layoutFile)
    }
}

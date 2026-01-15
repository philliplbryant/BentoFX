/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package com.jregw.demo.configuration

import com.jregw.demo.ApplicationConstants.FILE_EXTENSION_BEAN_NAME
import com.jregw.demo.ApplicationConstants.JSON_FILE_EXTENSION
import jakarta.inject.Named
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.coley.bentofx.persistence.api.codec.LayoutCodec
import software.coley.bentofx.persistence.impl.codec.json.JsonLayoutCodec

@Configuration
internal class JsonLayoutCodecConfiguration {

    @Bean
    @Named(FILE_EXTENSION_BEAN_NAME)
    fun fileExtension(): String = JSON_FILE_EXTENSION

    @Bean
    fun layoutCodec(): LayoutCodec =
        JsonLayoutCodec()
}

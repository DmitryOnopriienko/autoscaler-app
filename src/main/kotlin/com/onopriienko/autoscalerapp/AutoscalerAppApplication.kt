package com.onopriienko.autoscalerapp

import com.onopriienko.autoscalerapp.config.GitlabConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(GitlabConfig::class)
class AutoscalerAppApplication

fun main(args: Array<String>) {
    runApplication<AutoscalerAppApplication>(*args)
}

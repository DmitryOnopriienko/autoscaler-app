package com.onopriienko.autoscalerapp.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gitlab")
class GitlabConfig(
    val provider: String,
    val projectId: String,
    val accessToken: String,
    val defaultBranch: String,
)

package com.onopriienko.autoscalerapp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrometheusAlert(
    val status: String?,
    val generatorURL: String?,
    val startsAt: Instant?,
)

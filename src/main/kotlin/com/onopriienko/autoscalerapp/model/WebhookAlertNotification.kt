package com.onopriienko.autoscalerapp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookAlertNotification(
    val commonLabels: CommonLabels?,
    val alerts: List<PrometheusAlert>?,
    val externalURL: String?,
) {

    data class CommonLabels(
        val alertname: String?,
        val severity: String?,
    )
}

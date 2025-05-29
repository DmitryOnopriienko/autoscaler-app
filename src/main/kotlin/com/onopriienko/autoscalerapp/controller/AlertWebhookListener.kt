package com.onopriienko.autoscalerapp.controller

import com.onopriienko.autoscalerapp.enums.AlertStatus
import com.onopriienko.autoscalerapp.enums.ScaleDirection
import com.onopriienko.autoscalerapp.model.WebhookAlertNotification
import com.onopriienko.autoscalerapp.service.GitlabApiAdapter
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/alert/prometheus")
class AlertWebhookListener(
    private val gitlabApiAdapter: GitlabApiAdapter,
) {

    @PostMapping("/high-cpu")
    fun handlePrometheusAlert(@RequestBody alert: WebhookAlertNotification) {
        LOG.info("Received alert: {}", alert)
        val alertStatus = runCatching {
            val prometheusAlert = alert.alerts?.first()
            enumValueOf<AlertStatus>(requireNotNull(prometheusAlert?.status).uppercase())
        }.getOrElse {
            LOG.error("Error while handling webhook alert: {}", alert, it)
            return
        }
        when (alertStatus) {
            AlertStatus.FIRING -> gitlabApiAdapter.triggerScalingCiPipeline(ScaleDirection.UP)
            AlertStatus.RESOLVED -> gitlabApiAdapter.triggerScalingCiPipeline(ScaleDirection.DOWN)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AlertWebhookListener::class.java)
    }
}

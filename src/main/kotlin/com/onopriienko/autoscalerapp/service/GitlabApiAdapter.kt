package com.onopriienko.autoscalerapp.service

import com.onopriienko.autoscalerapp.config.GitlabConfig
import com.onopriienko.autoscalerapp.enums.ScaleDirection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class GitlabApiAdapter(
    private val restTemplate: RestTemplate,
    private val gitlabConfig: GitlabConfig,
) {

    fun triggerScalingCiPipeline(scaleDirection: ScaleDirection) {
        val formattedUri = String.format(CI_JOB_URL_TEMPLATE, gitlabConfig.provider, gitlabConfig.projectId)
        val uri = UriComponentsBuilder.fromUriString(formattedUri)
            .build().toUri()

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add(TOKEN_FORM_NAME, gitlabConfig.accessToken)
        formData.add(REF_FORM_NAME, gitlabConfig.defaultBranch)
        formData.add(SCALE_DIRECTION_VARIABLE_FORM_NAME, scaleDirection.value)


        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val httpEntity = HttpEntity(formData, headers)
        LOG.info("Triggering scaling job (scale direction - {}): {}", scaleDirection.value, uri)

        val response = restTemplate.postForEntity(
            uri,
            httpEntity,
            String::class.java,
        )

        LOG.info("Result of job triggering: {}", response.body)
    }

    companion object {
        private const val CI_JOB_URL_TEMPLATE = "https://%s/api/v4/projects/%s/trigger/pipeline"
        private const val TOKEN_FORM_NAME = "token"
        private const val REF_FORM_NAME = "ref"
        private const val SCALE_DIRECTION_VARIABLE_FORM_NAME = "variables[SCALE_DIRECTION]"


        private val LOG = LoggerFactory.getLogger(GitlabApiAdapter::class.java)
    }
}

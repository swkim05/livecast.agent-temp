package livecast.agent.srs;

import livecast.agent.model.srs.SRSOperationResponse;
import livecast.agent.model.srs.SRSStreamsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class SRSHttpApiUtil {
    private RestTemplate restTemplate;
    private String apiUrl;

    public SRSHttpApiUtil(RestTemplate restTemplate, String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public SRSStreamsResponse getStreams() {
        SRSStreamsResponse streams = null;

        final String streamsUrl = String.format("%s/api/v1/streams", this.apiUrl);
        final HttpMethod method = HttpMethod.GET;
        final ParameterizedTypeReference<SRSStreamsResponse> responseType = new ParameterizedTypeReference<SRSStreamsResponse>() {};

        try {
            log.debug("SRS GetStreams : {}", streamsUrl);
            final ResponseEntity<SRSStreamsResponse> response = restTemplate.exchange(streamsUrl, method, HttpEntity.EMPTY, responseType);

            if((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                log.trace("SRS GetStreams success : {}", response);
                streams = response.getBody();
            } else {
                log.warn("SRS GetStreams response : {}", response.getStatusCode());
            }
        } catch(Exception e) {
            log.warn("SRS GetStreams error", e);
        }

        return streams;
    }

    public boolean requestClientKickoff(String cid) {
        boolean result = false;

        final String kickoffUrl = String.format("%s/api/v1/clients/%s", this.apiUrl, cid);
        final HttpMethod method = HttpMethod.DELETE;
        final ParameterizedTypeReference<SRSOperationResponse> responseType = new ParameterizedTypeReference<SRSOperationResponse>() {};

        try {
            log.debug("SRS RequestClientKickoff : {}", kickoffUrl);
            final ResponseEntity<SRSOperationResponse> response = restTemplate.exchange(kickoffUrl, method, HttpEntity.EMPTY, responseType);

            if((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                final SRSOperationResponse operationResponse = response.getBody();
                if((operationResponse != null) && (operationResponse.getCode() == 0)) {
                    log.debug("SRS RequestClientKickoff success : cid={}", cid);
                    result = true;
                } else if(operationResponse != null) {
                    log.warn("SRS RequestClientKickoff error : statusCode={}, responseCode={}", response.getStatusCode(), operationResponse.getCode());
                } else {
                    log.warn("SRS RequestClientKickoff error : statusCode={}, responseCode=Unknown", response.getStatusCode());
                }
            } else {
                log.warn("SRS RequestClientKickoff error : statusCode={}", response.getStatusCode());
            }
        } catch(Exception e) {
            log.warn("SRS RequestClientKickoff error", e);
        }

        return result;
    }
}

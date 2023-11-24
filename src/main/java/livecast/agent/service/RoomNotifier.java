package livecast.agent.service;

import livecast.agent.exception.ErrorCode;
import livecast.agent.exception.LCException;
import livecast.agent.model.LCRoomNotify;
import livecast.agent.model.support.LCForbiddenWord;
import livecast.agent.model.support.LCRecordingMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class RoomNotifier {
    private RestTemplate restTemplate;

    @Autowired
    public RoomNotifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<LCForbiddenWord> getForbiddenWords(String queryUrl, String roomId) {
        List<LCForbiddenWord> forbiddenWords = null;

        if((roomId != null) && (queryUrl != null)) {
            final String parameterizedQueryUrl = queryUrl.replaceFirst("\\{[a-zA-Z0-9]+\\}", roomId);
            log.debug("GetForbiddenWords : parameterized query url = {}", parameterizedQueryUrl);

            final HttpMethod method = HttpMethod.GET;
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
            final HttpEntity requestEntity = new HttpEntity(headers);
            final ParameterizedTypeReference<List<LCForbiddenWord>> responseType = new ParameterizedTypeReference<List<LCForbiddenWord>>() {};

            try {
                final ResponseEntity<List<LCForbiddenWord>> response = restTemplate.exchange(parameterizedQueryUrl, method, requestEntity, responseType);

                if((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                    log.info("GetForbiddenWords success to {} : roomId={}", parameterizedQueryUrl, roomId);

                    forbiddenWords = response.getBody();
                } else {
                    log.warn("GetForbiddenWords error to {}: roomId={}, ResponseStateCode={}", parameterizedQueryUrl, roomId, response.getStatusCode());
                }
            } catch(Exception e) {
                log.warn("GetForbiddenWords error to {} : roomId={}, exceptionMessage={}", parameterizedQueryUrl, roomId, e.getMessage(), e);
            }
        } else {
            log.debug("GetForbiddenWords skipped : roomId or queryUrl is empty, roomId={}, queryUrl={}", roomId, queryUrl);
        }

        return forbiddenWords;
    }

    public void requestStartRecord(String recordEndpoint, String roomId, int activationId, int recordId, LCRecordingMode mode) {
        if(recordEndpoint != null) {
            final String url = String.format("%s/api/v1/rooms/%s/activations/%d/records/%d/start?mode=%s", recordEndpoint, roomId, activationId, recordId, mode);
            final HttpMethod method = HttpMethod.POST;
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
            final HttpEntity requestEntity = new HttpEntity(headers);
            final ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<Void>() {};

            log.debug("StartRecording HTTP Requesting... {} {}", method, url);
            final ResponseEntity<Void> response = restTemplate.exchange(url, method, requestEntity, responseType);
            if((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                log.trace("StartRecording HTTP Request success");
                return;
            } else {
                throw new LCException(ErrorCode.CanNotStartRecord, "StartRecording HTTP Request error : Response code = " + response.getStatusCode());
            }
        } else {
            throw new LCException(ErrorCode.CanNotStartRecord, "StartRecording HTTP Request error : Empty record endpoint");
        }
    }

    public void sendRoomStateChanged(String notifyUrl, String roomId, int activationId, LCRoomNotify.State state, ZoneId timeZone, LocalDateTime time) {
        if((roomId != null) && (notifyUrl != null)) {
            final LCRoomNotify notify = LCRoomNotify.builder()
                    .roomId(roomId)
                    .activationId(activationId)
                    .state(state)
//                    .timeZone(timeZone)
                    .time(time)
                    .build();
            final HttpMethod method = HttpMethod.POST;
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final HttpEntity<LCRoomNotify> requestEntity = new HttpEntity<>(notify, headers);
            final ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<Void>() {
            };

            try {
                final ResponseEntity<Void> response = restTemplate.exchange(notifyUrl, method, requestEntity, responseType);

                if ((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                    log.info("Notified room state success to {} : roomId={}, state={}", notifyUrl, roomId, state);
                } else {
                    log.warn("Notify room state error to {} : roomId={}, state={}, ResponseStatusCode={}", notifyUrl, roomId, state, response.getStatusCode());
                }
            } catch (Exception e) {
                log.warn("Notify room state error to {} : roomId={}, state={}, exceptionMessage={}", notifyUrl, roomId, state, e.getMessage(), e);
            }
        } else {
            log.debug("Notify room state skipped : roomId or notifyUrl is empty, roomId={}, notifyUrl={}", roomId, notifyUrl);
        }
    }
}

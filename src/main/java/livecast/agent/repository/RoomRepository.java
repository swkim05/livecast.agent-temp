package livecast.agent.repository;

import livecast.agent.model.LCRoom;
import livecast.agent.model.LCRoomEndpoint;
import livecast.agent.model.LCRoomState;
import livecast.agent.model.support.LCRoomStatus;
import livecast.agent.model.transfer.LCRoomTransfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class RoomRepository {
//    private RoomMapper mapper;
    private RestTemplate restTemplate;

    @Autowired
    public RoomRepository(
//            RoomMapper mapper
            RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
//        this.mapper = mapper;
    }

    public LCRoomTransfer selectRoom(String liveCode) {
        String url = String.format("http://146.56.177.196/api/v1/casts/live/%s/info", liveCode);
        LCRoomTransfer result = null;

        final HttpMethod method = HttpMethod.GET;
        final ParameterizedTypeReference<LCRoomTransfer> responseType = new ParameterizedTypeReference<LCRoomTransfer>() {};

        try {
            log.debug("Room selectRoom : {}", url);
            final ResponseEntity<LCRoomTransfer> response = restTemplate.exchange(url, method, HttpEntity.EMPTY, responseType);

            if((response != null) && (response.getStatusCode() != null) && (response.getStatusCode().equals(HttpStatus.OK))) {
                log.trace("Room selectRoom success : {}", response);
                result = response.getBody();
            } else {
                log.warn("Room selectRoom response : {}", response.getStatusCode());
            }
        } catch(Exception e) {
            log.warn("Room selectRoom error", e);
        }

        return result;
    }

    public List<LCRoomEndpoint> selectRoomEndpoints(String roomId) {
//        return mapper.selectRoomEndpoints(roomId);
        return new ArrayList<>();
    }

    public int updateRoomStatus(String roomId, LCRoomStatus status) {
//        return mapper.updateRoomStatus(roomId, status);
        return 1;
    }
}

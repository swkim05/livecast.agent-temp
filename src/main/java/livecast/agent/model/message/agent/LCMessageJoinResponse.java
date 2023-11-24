package livecast.agent.model.message.agent;

import livecast.agent.model.LCLiveUser;
import livecast.agent.model.LCRoom;
import livecast.agent.model.LCRoomState;
import livecast.agent.model.message.LCErrorReason;
import livecast.agent.model.support.LCForbiddenWord;
import livecast.agent.model.support.LCViewType;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCMessageJoinResponse {
    private boolean accepted;
    private LCErrorReason reason;

    private LCRoom room;
    private String roomPin;
    private LCLiveUser liveUser;
    private List<LCLiveUser> liveUsers;
    private List<LCForbiddenWord> forbiddenWords;
    private LCRoomState roomState;
    private LCViewType viewType;
    private Object viewState;
}

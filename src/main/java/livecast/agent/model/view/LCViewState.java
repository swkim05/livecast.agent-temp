package livecast.agent.model.view;

import livecast.agent.model.LCLiveUser;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCViewState {
    private boolean mainPublished;
    private String mainLiveId;
    private String mainFeedId;
    private String mainName;

    private boolean mainMirror;

    private LCLiveUser presentationUser;
}

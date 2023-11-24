package livecast.agent.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCUserMediaState {
    private String publishId;
    private boolean published;

    private boolean micPermission;
    private boolean micOn;

    private boolean camPermission;
    private boolean camOn;
    private boolean camMirror;

    private boolean audioOn;
}

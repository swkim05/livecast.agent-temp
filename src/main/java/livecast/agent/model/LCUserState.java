package livecast.agent.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LCUserState {
    private boolean muted;
    private boolean handUp;
    private boolean canChat;
    private boolean blocked;
}

package livecast.agent.model.message;

public enum LCErrorReason {
    OK, Unknown, TimeOver, BadRequest,
    NoSuchUser, NoSuchRoom, NoTimeToEnter, NoOwnerInRoom, NobodyInRoom,
    CheckEntranceCode, YouCanNotJoinYet, YouAreAlreadyJoined, YouAreNotJoined, YouAreNotConnected, YouCanNot, YouAreKicked, YouAreBlocked, TooManyUsers,
    AlreadyActivated, AlreadyDeactivated, Deactivated, NotActivated, NotSupportedView, AlreadyStarted, AlreadyStopped,
    NotSupportedMessage, NotSupportedOperation,
}

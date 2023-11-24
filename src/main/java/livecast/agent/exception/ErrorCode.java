package livecast.agent.exception;

public enum ErrorCode {
    Unknown,

    CommunicationError,
    NoMainEndpoint,
    JanusError,

    CanNotActivateRoom,
    CanNotStartRecord,

    CanNotNotify,
    CanNotFoundUser,
    NotAcceptableId,
    AuthenticationFail,

    CanNotFoundRoom,
}

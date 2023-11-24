USE training;

##
## for Spring Session
##
CREATE TABLE SPRING_SESSION (
                                PRIMARY_ID CHAR(36) NOT NULL,
                                SESSION_ID CHAR(36) NOT NULL,
                                CREATION_TIME BIGINT NOT NULL,
                                LAST_ACCESS_TIME BIGINT NOT NULL,
                                MAX_INACTIVE_INTERVAL INT NOT NULL,
                                EXPIRY_TIME BIGINT NOT NULL,
                                PRINCIPAL_NAME VARCHAR(100),

                                CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);


CREATE TABLE SPRING_SESSION_ATTRIBUTES (
                                           SESSION_PRIMARY_ID CHAR(36) NOT NULL,
                                           ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
                                           ATTRIBUTE_BYTES BLOB NOT NULL,

                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
                                           CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;


##
## for Application
##

CREATE TABLE mng_users (
                           id                                  BIGINT                      NOT NULL AUTO_INCREMENT             COMMENT '사용자 ID',
                           client_id                           VARCHAR(64)                     NULL                            COMMENT '클라이언트 ID',
                           email                               VARCHAR(256)                NOT NULL                            COMMENT '사용자 email',
                           password                            VARCHAR(128)                NOT NULL                            COMMENT '사용자 Password',
                           name                                VARCHAR(128)                NOT NULL                            COMMENT '사용자 이름',
                           type                                VARCHAR(32)                 NOT NULL                            COMMENT '사용자 유형 [Admin]',
                           enabled                             CHAR(1)                     NOT NULL                            COMMENT '사용가능 여부 [Y|N]',
                           parent_user_id                      BIGINT                          NULL                            COMMENT '계정 부모 ID',
                           created_datetime                    DATETIME                    NOT NULL DEFAULT NOW()              COMMENT '데이터 생성일시',
                           updated_datetime                    DATETIME                    NOT NULL DEFAULT NOW()              COMMENT '데이터 변경일시',

                           CONSTRAINT PRIMARY KEY (id),
                           CONSTRAINT fk_mng_users_parent_user_id   FOREIGN KEY (parent_user_id) REFERENCES mng_clients(id) ON DELETE CASCADE
) ENGINE InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '관리자 정보';

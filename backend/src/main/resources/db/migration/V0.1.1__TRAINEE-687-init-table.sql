CREATE TABLE "user"
(
    id         UUID            PRIMARY KEY,
    username   VARCHAR(64)     NOT NULL ,
    email      VARCHAR(128)    NOT NULL unique,
    isAdmin    BOOL            NOT NULL DEFAULT false
);

CREATE TABLE apprentice_supervisor(
    fk_apprentice UUID NOT NULL,
    fk_supervisor UUID NOT NULL ,
    foreign key (fk_apprentice) references "user"(id),
    foreign key (fk_supervisor) references "user"(id)
);

CREATE TABLE semester(
    id              UUID        NOT NULL PRIMARY KEY ,
    semester_name   VARCHAR(20) NOT NULL,
    semester_start  DATE        NOT NULL,
    semester_end    DATE        NOT NULL
);

CREATE TABLE subject
(
    id          UUID            NOT NULL PRIMARY KEY,
    name        VARCHAR (128)   NOT NULL,
    description varchar (128)            DEFAULT null
);

CREATE TABLE mark
(
    id          UUID            NOT NULL PRIMARY KEY,
    user_id     UUID            NOT NULL,
    subject_id  UUID            NOT NULL,
    semester_id UUID            NOT NULL,
    name        VARCHAR (128)   NOT NULL,
    date        DATE            NOT NULL,
    weight      INTEGER         NOT NULL,
    value       INTEGER                  DEFAULT null,
    foreign key (user_id)       references "user"(id)       ON DELETE CASCADE ,
    foreign key (subject_id)    references subject(id)     ON DELETE CASCADE,
    foreign key (semester_id)   references semester(id)     ON DELETE CASCADE
);

CREATE TABLE deleted_mark
(
    id              UUID            NOT NULL    PRIMARY KEY,
    user_id         UUID            NOT NULL,
    subject_id      UUID            NOT NULL,
    semester_id     UUID            NOT NULL,
    name            VARCHAR (128)   NOT NULL,
    date            DATE            NOT NULL,
    weight          INTEGER         NOT NULL,
    value           INTEGER                     DEFAULT null,
    deleted_time    TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    foreign key (user_id)       references "user"(id)       ON DELETE CASCADE ,
    foreign key (subject_id)    references subject(id)     ON DELETE CASCADE,
    foreign key (semester_id)   references semester(id)     ON DELETE CASCADE
);
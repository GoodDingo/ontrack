-- Schema for the SVN indexation

-- DB versioning

CREATE TABLE EXT_SVN_VERSION (
  VALUE   INTEGER   NOT NULL,
  UPDATED TIMESTAMP NOT NULL
);

-- Repository configuration

CREATE TABLE EXT_SVN_REPOSITORY (
  ID   INTEGER     NOT NULL AUTO_INCREMENT,
  NAME VARCHAR(80) NOT NULL,
  CONSTRAINT EXT_SVN_REPOSITORY_PK PRIMARY KEY (ID),
  CONSTRAINT EXT_SVN_REPOSITORY_UQ_NAME UNIQUE (NAME)
);

-- Indexed revisions for a repository

CREATE TABLE EXT_SVN_REVISION (
  REPOSITORY INTEGER      NOT NULL,
  REVISION   INTEGER      NOT NULL,
  AUTHOR     VARCHAR(40)  NOT NULL,
  CREATION   VARCHAR(40)  NOT NULL,
  MESSAGE    VARCHAR(500) NOT NULL,
  BRANCH     VARCHAR(200) NULL,
  CONSTRAINT EXT_SVN_REVISION_PK PRIMARY KEY (REPOSITORY, REVISION),
  CONSTRAINT EXT_SVN_REVISION_FK_REPOSITORY FOREIGN KEY (REPOSITORY) REFERENCES EXT_SVN_REPOSITORY (ID)
    ON DELETE CASCADE
);

-- Merge relationship between the revisions

CREATE TABLE EXT_SVN_MERGE_REVISION (
  REPOSITORY INTEGER NOT NULL,
  REVISION   INTEGER NOT NULL,
  TARGET     INTEGER NOT NULL,
  CONSTRAINT EXT_SVN_MERGE_REVISION_PK PRIMARY KEY (REPOSITORY, REVISION, TARGET),
  CONSTRAINT EXT_SVN_MERGE_REVISION_FK_TARGET FOREIGN KEY (REPOSITORY, TARGET) REFERENCES EXT_SVN_REVISION (REPOSITORY, REVISION)
    ON DELETE CASCADE
);

CREATE INDEX EXT_SVN_MERGE_REVISION_IDX_REPOSITORY_REVISION ON EXT_SVN_MERGE_REVISION (REPOSITORY, REVISION);

-- Copy events

CREATE TABLE EXT_SVN_COPY (
  REPOSITORY       INTEGER      NOT NULL,
  REVISION         INTEGER      NOT NULL,
  COPYFROMPATH     VARCHAR(255) NOT NULL,
  COPYFROMREVISION INTEGER      NOT NULL,
  COPYTOPATH       VARCHAR(255) NOT NULL,
  CONSTRAINT EXT_SVN_COPY_PK PRIMARY KEY (REPOSITORY, REVISION, COPYTOPATH),
  CONSTRAINT EXT_SVN_COPY_FK_REVISION FOREIGN KEY (REPOSITORY, REVISION) REFERENCES EXT_SVN_REVISION (REPOSITORY, REVISION)
    ON DELETE CASCADE
);

CREATE INDEX EXT_SVN_COPY_IDX_COPYTOPATH ON EXT_SVN_COPY (REPOSITORY, COPYTOPATH);

-- Stop events

CREATE TABLE EXT_SVN_STOP (
  REPOSITORY INTEGER      NOT NULL,
  REVISION   INTEGER      NOT NULL,
  PATH       VARCHAR(255) NOT NULL,
  CONSTRAINT EXT_SVN_STOP_PK PRIMARY KEY (REPOSITORY, REVISION, PATH),
  CONSTRAINT EXT_SVN_STOP_FK_REVISION FOREIGN KEY (REPOSITORY, REVISION) REFERENCES EXT_SVN_REVISION (REPOSITORY, REVISION)
    ON DELETE CASCADE
);

-- Indexation of issues

CREATE TABLE EXT_SVN_REVISION_ISSUE (
  REPOSITORY INTEGER     NOT NULL,
  REVISION   INTEGER     NOT NULL,
  ISSUE      VARCHAR(20) NOT NULL,
  CONSTRAINT EXT_SVN_REVISION_ISSUE_PK PRIMARY KEY (REPOSITORY, REVISION, ISSUE),
  CONSTRAINT EXT_SVN_REVISION_ISSUE_FK_REVISION FOREIGN KEY (REPOSITORY, REVISION) REFERENCES EXT_SVN_REVISION (REPOSITORY, REVISION)
    ON DELETE CASCADE
);

CREATE INDEX EXT_SVN_REVISION_ISSUE_IDX_ISSUE ON EXT_SVN_REVISION_ISSUE (REPOSITORY, ISSUE);
CREATE INDEX EXT_SVN_REVISION_ISSUE_IDX_REVISION ON EXT_SVN_REVISION_ISSUE (REPOSITORY, REVISION);

package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.common.BaseException;

public class Neo4JExportDownloadException extends BaseException {

    public Neo4JExportDownloadException(String uuid, Exception ex) {
        super(ex, "Cannot download %s", uuid);
    }

}

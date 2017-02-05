package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.model.exceptions.InputException;

public class Neo4JExportWrongDownloadException extends InputException {

    public Neo4JExportWrongDownloadException(String uuid) {
        super("The requested download (%s) does not match the current available download.", uuid);
    }

}

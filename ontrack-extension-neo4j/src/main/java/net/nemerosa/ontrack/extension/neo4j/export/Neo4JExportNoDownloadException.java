package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.model.exceptions.NotFoundException;

public class Neo4JExportNoDownloadException extends NotFoundException {

    public Neo4JExportNoDownloadException() {
        super("No download is available for download");
    }

}

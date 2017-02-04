package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.io.File;

@Data
public class Neo4JExportContext {

    private final String uuid;
    private final File dir;

}

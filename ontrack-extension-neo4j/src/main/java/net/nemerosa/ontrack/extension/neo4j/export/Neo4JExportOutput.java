package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.util.List;

@Data
public class Neo4JExportOutput {

    private final String uuid;
    private final List<String> nodeFiles;
    private final List<String> relFiles;

}

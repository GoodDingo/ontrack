package net.nemerosa.ontrack.extension.neo4j.export;

import java.io.IOException;

public interface Neo4JExportService {

    Neo4JExportOutput export(Neo4JExportInput input) throws IOException;

}

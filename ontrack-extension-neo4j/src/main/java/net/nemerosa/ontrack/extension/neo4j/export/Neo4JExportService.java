package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.common.Document;

public interface Neo4JExportService {

    Neo4JExportOutput export(Neo4JExportInput input);

    Document download(String uuid);

}

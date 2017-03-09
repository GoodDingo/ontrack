package net.nemerosa.ontrack.extension.neo4j.export.model;

import java.util.List;

public interface Neo4JExportModule {

    List<Neo4JExportRecordExtractor<?>> getRecordExtractors();

}

package net.nemerosa.ontrack.extension.neo4j.export.model;

import lombok.Data;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
public class Neo4JExportRecordExtractor<T> {

    private final Supplier<Stream<T>> collectionSupplier;
    private final List<Neo4JExportRecordDef<T>> recordDefs;

}

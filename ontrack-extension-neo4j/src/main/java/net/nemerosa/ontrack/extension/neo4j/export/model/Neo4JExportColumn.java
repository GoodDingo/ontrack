package net.nemerosa.ontrack.extension.neo4j.export.model;

import lombok.Data;

import java.util.function.Function;

@Data(staticConstructor = "of")
public class Neo4JExportColumn<T> {

    private final String header;
    private final Function<T, Object> valueFn;

}

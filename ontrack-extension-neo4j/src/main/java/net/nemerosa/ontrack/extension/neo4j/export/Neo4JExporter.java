package net.nemerosa.ontrack.extension.neo4j.export;

@FunctionalInterface
public interface Neo4JExporter<T> {

    void export(Neo4JExportContext exportContext, T o);

}

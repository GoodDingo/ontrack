package net.nemerosa.ontrack.extension.neo4j.export;

public interface Neo4JExportChannel<T> {
    void write(Neo4JExportContext exportContext, T o);
}

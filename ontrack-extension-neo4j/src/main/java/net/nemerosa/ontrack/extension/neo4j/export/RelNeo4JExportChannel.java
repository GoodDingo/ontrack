package net.nemerosa.ontrack.extension.neo4j.export;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class RelNeo4JExportChannel<T> extends AbstractNeo4JExportChannel<T> {

    private final String name;
    private final List<Neo4JColumn<T>> columns;

    public RelNeo4JExportChannel(String name, IDSpec<T> startIdFn, IDSpec<T> endIdFn, List<Neo4JColumn<T>> columns) {
        this.name = name;
        // Actual columns
        List<Neo4JColumn<T>> actualColumns = new ArrayList<>(
                columns
        );
        // Adding the type to the list of columns
        actualColumns.add(0, Neo4JColumn.column(":TYPE", it -> name));
        // Start ID column
        actualColumns.add(0, Neo4JColumn.column(
                format(":START_ID(%s)", startIdFn.getName()),
                startIdFn.getMapping()));
        // End ID column
        actualColumns.add(0, Neo4JColumn.column(
                format(":END_ID(%s)", endIdFn.getName()),
                endIdFn.getMapping()));
        // OK
        this.columns = actualColumns;
    }

    @Override
    public void write(Neo4JExportContext exportContext, T o) {
        // File name
        String file = "rel/" + name + ".csv";
        // Gets the writer
        PrintWriter writer = exportContext.getWriter(file, w -> writeCsvLine(w, columns.stream().map(Neo4JColumn::getHeader)));
        // Output
        writeCsvLine(
                writer,
                columns.stream().map(c -> c.getMapping().apply(o))
        );
    }
}

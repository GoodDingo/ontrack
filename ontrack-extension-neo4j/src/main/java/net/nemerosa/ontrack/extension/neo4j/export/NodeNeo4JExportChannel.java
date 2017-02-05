package net.nemerosa.ontrack.extension.neo4j.export;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;

public class NodeNeo4JExportChannel<T> extends AbstractNeo4JExportChannel<T> {

    private final String label;
    private final Function<T, ?> idFn;
    private final List<Neo4JColumn<T>> columns;

    public NodeNeo4JExportChannel(String label, Function<T, ?> idFn, List<Neo4JColumn<T>> columns) {
        this.label = label;
        this.idFn = idFn;
        this.columns = columns;
    }

    @Override
    public void write(Neo4JExportContext exportContext, T o) {
        List<Neo4JColumn<T>> actualColumns = new ArrayList<>(
                columns
        );
        // Adding the label to the list of columns
        actualColumns.add(0, Neo4JColumn.column(":LABEL", it -> label));
        // ID column
        actualColumns.add(0, Neo4JColumn.column(
                format("%sId:ID(%s)", StringUtils.uncapitalize(label), label),
                idFn));
        // File name
        String file = "node/" + label + ".csv";
        // Gets the writer
        PrintWriter writer = exportContext.getWriter(file, w -> writeCsvLine(w, actualColumns.stream().map(Neo4JColumn::getHeader)));
        // Output
        writeCsvLine(
                writer,
                actualColumns.stream().map(c -> c.getMapping().apply(o))
        );
    }
}

package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.extension.neo4j.Neo4JConstants;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNeo4JExportChannel<T> implements Neo4JExportChannel<T> {

    protected void writeCsvLine(PrintWriter writer, Stream<?> content) {
        writer.println(
                content.map(this::formatCsvValue).collect(Collectors.joining(Neo4JConstants.CSV_DELIMITER))
        );
    }

    protected String formatCsvValue(Object o) {
        if (o instanceof String) {
            String text = (String) o;
            return StringEscapeUtils.escapeCsv(text);
        } else if (o == null) {
            return "";
        } else {
            return String.valueOf(o);
        }
    }
}

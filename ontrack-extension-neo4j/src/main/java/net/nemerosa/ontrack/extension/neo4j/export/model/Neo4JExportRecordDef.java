package net.nemerosa.ontrack.extension.neo4j.export.model;

import com.google.common.collect.ImmutableList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
public class Neo4JExportRecordDef<T> {

    private final Neo4JExportRecordType type;
    private final String name;
    private final List<Neo4JExportColumn<T>> columns;

    public static <T> Neo4JExportRecordDefBuilder<T> node(String name, Function<T, Object> idFn) {
        return new Neo4JExportRecordDefBuilder<T>(Neo4JExportRecordType.NODE, name)
                .with(
                        nodeIdLabel(name),
                        nodeIdValueFn(name, idFn)
                );
    }

    public static <T> Function<T, Object> nodeIdValueFn(String name, Function<T, Object> idFn) {
        return t -> String.format("%s//%s", name, idFn.apply(t));
    }

    public static String nodeIdLabel(String name) {
        return String.format("%s:ID", name);
    }

    @Data
    public static class Neo4JExportRecordDefBuilder<T> {
        private final Neo4JExportRecordType type;
        private final String name;
        private final List<Neo4JExportColumn<T>> columns = new ArrayList<>();

        public Neo4JExportRecordDefBuilder<T> with(String header, Function<T, Object> valueFn) {
            columns.add(Neo4JExportColumn.of(header, valueFn));
            return this;
        }

        public Neo4JExportRecordDef<T> build() {
            return new Neo4JExportRecordDef<T>(
                    type,
                    name,
                    ImmutableList.copyOf(columns)
            );
        }
    }

}

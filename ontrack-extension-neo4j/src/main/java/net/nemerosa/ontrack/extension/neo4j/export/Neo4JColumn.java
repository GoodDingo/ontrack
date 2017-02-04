package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.util.function.Function;

@Data
public class Neo4JColumn<T> {

    public static <T> Neo4JColumn<T> column(String header, Function<T, ?> mapping) {
        return new Neo4JColumn<>(header, mapping);
    }

    private final String header;
    private final Function<T, ?> mapping;

}

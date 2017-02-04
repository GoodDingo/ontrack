package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.util.function.Function;

@Data
public class IDSpec<T> {

    public static <T> IDSpec<T> idSpec(String name, Function<T, ?> mapping) {
        return new IDSpec<T>(name, mapping);
    }

    private final String name;
    private final Function<T, ?> mapping;

}

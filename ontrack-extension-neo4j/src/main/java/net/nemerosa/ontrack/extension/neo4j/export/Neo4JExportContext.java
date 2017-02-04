package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
public class Neo4JExportContext implements Closeable {

    private final String uuid;
    private final File dir;

    private final Map<String, PrintWriter> writers = new ConcurrentHashMap<>();

    public PrintWriter getWriter(String file, Consumer<PrintWriter> initFn) {
        return writers.computeIfAbsent(
                file,
                (f) -> createWriter(f, initFn)
        );
    }

    private PrintWriter createWriter(String file, Consumer<PrintWriter> initFn) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, file)), "UTF-8"));
            if (initFn != null) {
                initFn.accept(writer);
            }
            return writer;
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new RuntimeException("Cannot create file", e);
        }
    }

    @Override
    public void close() {
        writers.values().forEach(PrintWriter::close);
    }
}

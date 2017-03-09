package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;
import net.nemerosa.ontrack.extension.neo4j.Neo4JConstants;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportColumn;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordDef;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Neo4JExportContext implements Closeable {

    private final String uuid;
    private final File dir;

    private final Map<String, RecordFile> records = new ConcurrentHashMap<>();

    public void writeRow(String name, Stream<?> row) {
        writeCsvLine(
                getWriter(name),
                row
        );
    }

    private PrintWriter getWriter(String name) {
        RecordFile recordFile = records.get(name);
        if (recordFile != null) {
            return recordFile.getWriter();
        } else {
            throw new IllegalStateException("No CSV writer has been initialized for " + name);
        }
    }

    private void writeCsvLine(PrintWriter writer, Stream<?> row) {
        writer.println(
                row.map(this::formatCsvValue).collect(Collectors.joining(Neo4JConstants.CSV_DELIMITER))
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

    public void init(Neo4JExportRecordDef<?> recordDef) {
        String file = getFileName(recordDef);
        PrintWriter writer = createWriter(file, w -> writeCsvLine(w, recordDef.getColumns().stream().map(Neo4JExportColumn::getHeader)));
        records.put(recordDef.getName(), new RecordFile(recordDef, writer));
    }

    private String getFileName(Neo4JExportRecordDef<?> recordDef) {
        return String.format("%s/%s.csv", recordDef.getType().name().toLowerCase(), recordDef.getName());
    }

    private PrintWriter createWriter(String file, Consumer<PrintWriter> initFn) {
        try {
            File f = new File(dir, file);
            FileUtils.forceMkdirParent(f);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
            if (initFn != null) {
                initFn.accept(writer);
            }
            return writer;
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file", e);
        }
    }

    @Override
    public void close() {
        records.values().forEach(RecordFile::close);
    }

    public List<String> getPaths() {
        return records.values().stream()
                .map(r -> getFileName(r.getRecordDef()))
                .sorted()
                .collect(Collectors.toList());
    }

    public InputStream open(String file) throws FileNotFoundException {
        File f = new File(dir, file);
        return new BufferedInputStream(new FileInputStream(f));
    }

    @Data
    public static class RecordFile {
        private final Neo4JExportRecordDef<?> recordDef;
        private final PrintWriter writer;

        public void close() {
            writer.flush();
            writer.close();
        }
    }
}

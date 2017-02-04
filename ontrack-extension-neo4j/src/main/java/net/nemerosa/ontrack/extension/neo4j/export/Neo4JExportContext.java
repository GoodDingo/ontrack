package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

import java.io.*;

@Data
public class Neo4JExportContext {

    private final String uuid;
    private final File dir;

    public PrintWriter write(String file) throws FileNotFoundException, UnsupportedEncodingException {
        File f = new File(dir, file);
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
    }
}

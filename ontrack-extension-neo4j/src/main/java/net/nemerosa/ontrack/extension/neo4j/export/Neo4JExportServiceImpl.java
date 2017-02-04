package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.extension.neo4j.Neo4JConstants;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.Entity;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureService;
import net.nemerosa.ontrack.model.support.EnvService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static net.nemerosa.ontrack.extension.neo4j.export.Neo4JColumn.column;

@Service
public class Neo4JExportServiceImpl implements Neo4JExportService {

    private final Logger logger = LoggerFactory.getLogger(Neo4JExportService.class);

    private final SecurityService securityService;
    private final EnvService envService;
    private final StructureService structureService;
    private final Neo4JConfigProperties configProperties;

    /**
     * Download contexts
     */
    private final Map<String, Neo4JExportContext> exportContextMap = new ConcurrentHashMap<>();

    @Autowired
    public Neo4JExportServiceImpl(SecurityService securityService, EnvService envService, StructureService structureService, Neo4JConfigProperties configProperties) {
        this.securityService = securityService;
        this.envService = envService;
        this.structureService = structureService;
        this.configProperties = configProperties;
    }

    @Override
    public Neo4JExportOutput export(Neo4JExportInput input) throws IOException {

        // Checks authorizations
        securityService.checkGlobalFunction(ApplicationManagement.class);

        // Checks the maximum number of downloads
        synchronized (exportContextMap) {
            if (exportContextMap.size() >= configProperties.getExportDownloadMaximum()) {
                throw new IllegalStateException("Maximum number of concurrent exports has been reached.");
            }
        }

        // UUID for this export
        String uuid = UUID.randomUUID().toString();

        // Export context
        Neo4JExportContext exportContext = exportContextMap.computeIfAbsent(
                uuid,
                this::createExportContext
        );

        // Project nodes
        exportProjects(exportContext);

        // Branch nodes
        exportBranches(exportContext);

        // TODO Branch --> Project

        // OK
        return new Neo4JExportOutput(uuid);
    }

    private void exportBranches(Neo4JExportContext exportContext) {
        trace(exportContext, "Export of branches");
        // FIXME Method net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportServiceImpl.exportBranches

    }

    private void exportProjects(Neo4JExportContext exportContext) throws FileNotFoundException, UnsupportedEncodingException {
        trace(exportContext, "Export of projects");
        exportNodes(
                exportContext,
                "projects.csv",
                structureService.getProjectList(),
                asList(
                        column("projectId:ID", Entity::id),
                        column("name", Project::getName)
                )
        );
    }

    private <T> void exportNodes(
            Neo4JExportContext exportContext,
            String file,
            List<T> items,
            List<Neo4JColumn<T>> columns) throws FileNotFoundException, UnsupportedEncodingException {
        // Output
        try (PrintWriter writer = exportContext.write(file)) {
            // Headers
            writeCsvLine(writer, columns.stream().map(Neo4JColumn::getHeader));
            // Values
            items.forEach(o ->
                    writeCsvLine(
                            writer,
                            columns.stream().map(c -> c.getMapping().apply(o))
                    )
            );
        }
    }

    private void writeCsvLine(PrintWriter writer, Stream<?> content) {
        writer.println(
                content.map(this::formatCsvValue).collect(Collectors.joining(Neo4JConstants.CSV_DELIMITER))
        );
    }

    private String formatCsvValue(Object o) {
        if (o instanceof String) {
            String text = (String) o;
            return StringEscapeUtils.escapeCsv(text);
        } else if (o == null) {
            return "";
        } else {
            return String.valueOf(o);
        }
    }

    private void trace(Neo4JExportContext exportContext, String message, Object... parameters) {
        logger.debug("[neo4j][export][%s] %s", exportContext.getUuid(), String.format(message, parameters));
    }

    private Neo4JExportContext createExportContext(String id) {
        File dir = envService.getWorkingDir(configProperties.getExportDownloadPath(), id);
        return new Neo4JExportContext(id, dir);
    }
}

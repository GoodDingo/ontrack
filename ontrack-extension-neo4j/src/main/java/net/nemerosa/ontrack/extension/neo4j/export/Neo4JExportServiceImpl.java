package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.common.Document;
import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportModule;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordDef;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordExtractor;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.NameDescription;
import net.nemerosa.ontrack.model.support.ApplicationLogEntry;
import net.nemerosa.ontrack.model.support.ApplicationLogService;
import net.nemerosa.ontrack.model.support.EnvService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// FIXME Cleanup job

@Service
@Transactional(readOnly = true)
public class Neo4JExportServiceImpl implements Neo4JExportService {

    private final Logger logger = LoggerFactory.getLogger(Neo4JExportService.class);

    private final Collection<Neo4JExportModule> exportModules;
    private final SecurityService securityService;
    private final EnvService envService;
    private final Neo4JConfigProperties configProperties;
    private final ApplicationLogService applicationLogService;

    /**
     * Download context
     */
    private final AtomicReference<Neo4JExportContext> currentExportContext = new AtomicReference<>();

    @Autowired
    public Neo4JExportServiceImpl(Collection<Neo4JExportModule> exportModules,
                                  SecurityService securityService,
                                  EnvService envService,
                                  Neo4JConfigProperties configProperties,
                                  ApplicationLogService applicationLogService
    ) {
        this.exportModules = exportModules;
        this.securityService = securityService;
        this.envService = envService;
        this.configProperties = configProperties;
        this.applicationLogService = applicationLogService;
    }

    @Override
    public Neo4JExportOutput export(Neo4JExportInput input) {

        // Checks authorizations
        securityService.checkGlobalFunction(ApplicationManagement.class);

        // Cleanup of previous context
        Neo4JExportContext exportContext = currentExportContext.updateAndGet(ctx -> {
            closeContext(ctx);
            // New context
            String uuid = UUID.randomUUID().toString();
            return createExportContext(uuid);
        });

        // Initialisation of the context
        exportModules.stream()
                .flatMap(exportModule -> exportModule.getRecordExtractors().stream())
                .flatMap(recordExtractor -> recordExtractor.getRecordDefs().stream())
                .forEach(exportContext::init);

        // Collecting all record extractors
        List<Neo4JExportRecordExtractor<?>> recordDefs = securityService.asAdmin(() ->
                exportModules.stream()
                        .flatMap(exportModule -> exportModule.getRecordExtractors().stream())
                        .collect(Collectors.toList())
        );

        // Launching the export
        securityService.asAdmin(() -> export(exportContext, recordDefs));

        // Gets list of paths
        List<String> paths = exportContext.getPaths();

        // OK
        return new Neo4JExportOutput(
                exportContext.getUuid(),
                // Nodes
                paths.stream().filter(p -> StringUtils.startsWith(p, "node/")).collect(Collectors.toList()),
                // Relationships
                paths.stream().filter(p -> StringUtils.startsWith(p, "rel/")).collect(Collectors.toList())
        );
    }

    private void export(Neo4JExportContext exportContext, List<Neo4JExportRecordExtractor<?>> recordExtractors) {
        recordExtractors.forEach(recordExtractor -> export(exportContext, recordExtractor));
    }

    private <T> void export(Neo4JExportContext exportContext, Neo4JExportRecordExtractor<T> recordExtractor) {
        // Gets the list of items
        recordExtractor.getCollectionSupplier().get().forEach(o ->
                export(exportContext, recordExtractor, o)
        );
    }

    private <T> void export(Neo4JExportContext exportContext, Neo4JExportRecordExtractor<T> recordExtractor, T o) {
        recordExtractor.getRecordDefs().forEach(recordDef ->
                export(exportContext, recordDef, o)
        );
    }

    private <T> void export(Neo4JExportContext exportContext, Neo4JExportRecordDef<T> recordExtractor, T o) {
        exportContext.writeRow(
                recordExtractor.getName(),
                recordExtractor.getColumns().stream()
                        .map(c -> c.getValueFn().apply(o))
        );
    }

    private Neo4JExportContext closeContext(Neo4JExportContext ctx) {
        if (ctx != null) {
            ctx.close();
            File contextWorkingDir = getContextWorkingDir(ctx.getUuid());
            try {
                FileUtils.forceDelete(contextWorkingDir);
            } catch (IOException e) {
                applicationLogService.log(
                        ApplicationLogEntry.error(
                                e,
                                NameDescription.nd("Neo4J Export Cleanup", "Cannot delete Neo4J export working directory"),
                                ""
                        )
                                .withDetail("neo4j.export.uuid", ctx.getUuid())
                                .withDetail("neo4j.export.dir", contextWorkingDir.getAbsolutePath())
                );
            }
        }
        return null;
    }

    @Override
    public Document download(String uuid) {
        // Checks authorizations
        securityService.checkGlobalFunction(ApplicationManagement.class);
        // Gets the current download context
        Neo4JExportContext exportContext = currentExportContext.get();
        if (exportContext == null) {
            throw new Neo4JExportNoDownloadException();
        }
        if (!StringUtils.equals(exportContext.getUuid(), uuid)) {
            throw new Neo4JExportWrongDownloadException(uuid);
        }
        // Close everything
        exportContext.close();
        // Gets the list of paths
        List<String> paths = exportContext.getPaths();
        // Zips the directory
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(bout)) {
            for (String path : paths) {
                zout.putNextEntry(new ZipEntry(path));
                try (InputStream in = exportContext.open(path)) {
                    IOUtils.copy(in, zout);
                }
            }
        } catch (IOException ex) {
            throw new Neo4JExportDownloadException(uuid, ex);
        }
        // Cleanup
        currentExportContext.updateAndGet(this::closeContext);
        // Returns the ZIP
        return new Document(
                "application/zip",
                bout.toByteArray()
        );
    }

    private Neo4JExportContext createExportContext(String id) {
        File dir = getContextWorkingDir(id);
        return new Neo4JExportContext(id, dir);
    }

    private File getContextWorkingDir(String id) {
        return envService.getWorkingDir(configProperties.getExportDownloadPath(), id);
    }
}

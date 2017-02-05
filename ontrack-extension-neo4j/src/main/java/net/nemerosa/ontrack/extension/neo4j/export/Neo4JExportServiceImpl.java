package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.common.Document;
import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.nemerosa.ontrack.extension.neo4j.export.IDSpec.idSpec;
import static net.nemerosa.ontrack.extension.neo4j.export.Neo4JColumn.column;

// FIXME Cleanup job

@Service
@Transactional(readOnly = true)
public class Neo4JExportServiceImpl implements Neo4JExportService {

    private final Logger logger = LoggerFactory.getLogger(Neo4JExportService.class);

    private final SecurityService securityService;
    private final EnvService envService;
    private final StructureService structureService;
    private final Neo4JConfigProperties configProperties;
    private final ApplicationLogService applicationLogService;

    /**
     * Download context
     */
    private final AtomicReference<Neo4JExportContext> currentExportContext = new AtomicReference<>();

    @Autowired
    public Neo4JExportServiceImpl(SecurityService securityService, EnvService envService, StructureService structureService, Neo4JConfigProperties configProperties, ApplicationLogService applicationLogService) {
        this.securityService = securityService;
        this.envService = envService;
        this.structureService = structureService;
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

        // Project nodes
        exportProjects(exportContext);

        // Branch nodes
        exportBranches(exportContext);

        // OK
        return new Neo4JExportOutput(exportContext.getUuid());
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

    private void exportBranches(Neo4JExportContext exportContext) {
        trace(exportContext, "Export of branches");
        exportNodes(
                exportContext,
                structureService.getProjectList().stream()
                        .flatMap(p -> structureService.getBranchesForProject(p.getId()).stream())
                        .collect(Collectors.toList()),
                asList(
                        // Branch node
                        new NodeNeo4JExportChannel<>(
                                "Branch",
                                Entity::id,
                                asList(
                                        column("name", Branch::getName),
                                        column("description", Branch::getDescription),
                                        column("disabled:boolean", Branch::isDisabled),
                                        column("creator", this::getSignatureCreator),
                                        column("creation", this::getSignatureCreation)
                                        // TODO Branch type
                                        // TODO Branch link to template
                                )
                        ),
                        // Branch --> Project
                        new RelNeo4JExportChannel<>(
                                "BRANCH_OF",
                                idSpec("Branch", Entity::id), // Branch + ID
                                idSpec("Project", b -> b.getProject().id()), // Project + ID
                                Collections.emptyList() // No data
                        )
                )
        );
    }

    private void exportProjects(Neo4JExportContext exportContext) {
        trace(exportContext, "Export of projects");
        exportNodes(
                exportContext,
                structureService.getProjectList(),
                singletonList(
                        new NodeNeo4JExportChannel<>(
                                "Project",
                                Entity::id,
                                asList(
                                        column("name", Project::getName),
                                        column("description", Project::getDescription),
                                        column("disabled:boolean", Project::isDisabled),
                                        column("creator", this::getSignatureCreator),
                                        column("creation", this::getSignatureCreation)
                                )
                        )
                )
        );
    }

    private LocalDateTime getSignatureCreation(ProjectEntity entity) {
        Signature signature = entity.getSignature();
        return signature != null ? signature.getTime() : null;
    }

    private String getSignatureCreator(ProjectEntity entity) {
        Signature signature = entity.getSignature();
        return signature != null ? signature.getUser().getName() : "";
    }

    private <T> void exportNodes(
            Neo4JExportContext exportContext,
            List<T> items,
            List<Neo4JExportChannel<T>> channels) {
        items.forEach(o ->
                channels.forEach(channel ->
                        channel.write(exportContext, o)
                )
        );
    }

    private void trace(Neo4JExportContext exportContext, String message, Object... parameters) {
        logger.debug("[neo4j][export][{}] {}", exportContext.getUuid(), format(message, parameters));
    }

    private Neo4JExportContext createExportContext(String id) {
        File dir = getContextWorkingDir(id);
        return new Neo4JExportContext(id, dir);
    }

    private File getContextWorkingDir(String id) {
        return envService.getWorkingDir(configProperties.getExportDownloadPath(), id);
    }
}

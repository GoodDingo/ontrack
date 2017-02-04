package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.model.support.EnvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
        try (Neo4JExportContext exportContext = exportContextMap.computeIfAbsent(
                uuid,
                this::createExportContext
        )) {

            // Project nodes
            exportProjects(exportContext);

            // Branch nodes
            exportBranches(exportContext);

            // TODO Branch --> Project

            // OK
            return new Neo4JExportOutput(uuid);
        }
    }

    private void exportBranches(Neo4JExportContext exportContext) throws FileNotFoundException, UnsupportedEncodingException {
        trace(exportContext, "Export of branches");
        exportNodes(
                exportContext,
                structureService.getProjectList().stream()
                        .flatMap(p -> structureService.getBranchesForProject(p.getId()).stream())
                        .collect(Collectors.toList()),
                asList(
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
                        )
                )
        );
    }

    private void exportProjects(Neo4JExportContext exportContext) throws FileNotFoundException, UnsupportedEncodingException {
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
            List<Neo4JExportChannel> channels) throws FileNotFoundException, UnsupportedEncodingException {
        items.forEach(o ->
                channels.forEach(channel ->
                        channel.write(exportContext, o)
                )
        );
    }

    private void trace(Neo4JExportContext exportContext, String message, Object... parameters) {
        logger.debug("[neo4j][export][%s] %s", exportContext.getUuid(), format(message, parameters));
    }

    private Neo4JExportContext createExportContext(String id) {
        File dir = envService.getWorkingDir(configProperties.getExportDownloadPath(), id);
        return new Neo4JExportContext(id, dir);
    }
}

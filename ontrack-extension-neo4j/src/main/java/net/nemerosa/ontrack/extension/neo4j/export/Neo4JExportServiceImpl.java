package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.support.EnvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Neo4JExportServiceImpl implements Neo4JExportService {

    private final Logger logger = LoggerFactory.getLogger(Neo4JExportService.class);

    private final SecurityService securityService;
    private final EnvService envService;
    private final Neo4JConfigProperties configProperties;

    /**
     * Download contexts
     */
    private final Map<String, Neo4JExportContext> exportContextMap = new ConcurrentHashMap<>();

    @Autowired
    public Neo4JExportServiceImpl(SecurityService securityService, EnvService envService, Neo4JConfigProperties configProperties) {
        this.securityService = securityService;
        this.envService = envService;
        this.configProperties = configProperties;
    }

    @Override
    public Neo4JExportOutput export(Neo4JExportInput input) {

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

    private void exportProjects(Neo4JExportContext exportContext) {
        trace(exportContext, "Export of projects");
        // FIXME Method net.nemerosa.ontrack.extension.neo4j.export.Neo4JExportServiceImpl.exportProjects

    }

    private void trace(Neo4JExportContext exportContext, String message, Object... parameters) {
        logger.debug("[neo4j][export][%s] %s", exportContext.getUuid(), String.format(message, parameters));
    }

    private Neo4JExportContext createExportContext(String id) {
        File dir = envService.getWorkingDir(configProperties.getExportDownloadPath(), id);
        return new Neo4JExportContext(id, dir);
    }
}

package net.nemerosa.ontrack.extension.neo4j.export;

import net.nemerosa.ontrack.extension.neo4j.Neo4JConfigProperties;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Neo4JExportServiceImpl implements Neo4JExportService {

    private final SecurityService securityService;
    private final Neo4JConfigProperties configProperties;

    /**
     * Download contexts
     */
    private final Map<String, Neo4JExportContext> exportContextMap = new ConcurrentHashMap<>();

    @Autowired
    public Neo4JExportServiceImpl(SecurityService securityService, Neo4JConfigProperties configProperties) {
        this.securityService = securityService;
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
                Neo4JExportContext::new
        );

        // TODO Project nodes
        // TODO Branch nodes
        // TODO Branch --> Project

        // OK
        return new Neo4JExportOutput(uuid);
    }
}

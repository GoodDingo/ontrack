package net.nemerosa.ontrack.extension.neo4j;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ontrack.config.neo4j")
public class Neo4JConfigProperties {

    /**
     * Maximum number of pending downloads
     */
    private int exportDownloadMaximum = 5;

    /**
     * Retention period for a download (in minutes, -1 to keep them forever)
     */
    private int exportDownloadRetentionMinutes = 15;

    /**
     * Path for the export files, relative to the working directory of Ontrack
     */
    private String exportDownloadPath = "neo4j/export";

}

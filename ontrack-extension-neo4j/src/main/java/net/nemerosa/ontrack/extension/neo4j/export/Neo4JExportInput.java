package net.nemerosa.ontrack.extension.neo4j.export;

import lombok.Data;

@Data
public class Neo4JExportInput {

    /**
     * Exporting the ACL
     */
    private boolean exportACL = true;

    /**
     * Ignoring the property migration errors?
     */
    // private boolean ignorePropertyError = false;

    /**
     * Limit for the number of builds being imported (-1 for no limit)
     */
    // private int buildCount = -1;

    /**
     * Limit for the number of build links being imported (-1 for no limit)
     */
    // private int buildLinkCount = -1;

    /**
     * Limit for the number of promotion runs being imported (-1 for no limit)
     */
    // private int promotionRunCount = -1;

    /**
     * Limit for the number of validation runs being imported (-1 for no limit)
     */
    // private int validationRunCount = -1;

    /**
     * Limit for the number of properties being imported (-1 for no limit)
     */
    // private int propertyCount = -1;

    /**
     * Property filter on type
     */
    // private String propertyFilterType;

}

package net.nemerosa.ontrack.extension.neo4j.export.core;

import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportModule;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordDef;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordExtractor;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EntitiesNeo4JExportModule implements Neo4JExportModule {

    private final StructureService structureService;
    private final Neo4JExportRecordDef<Project> projectNode;

    @Autowired
    public EntitiesNeo4JExportModule(
            StructureService structureService,
            @Qualifier("Project")
                    Neo4JExportRecordDef<Project> projectNode
    ) {
        this.structureService = structureService;
        this.projectNode = projectNode;
    }

    @Override
    public List<Neo4JExportRecordExtractor<?>> getRecordExtractors() {
        return Arrays.asList(
                new Neo4JExportRecordExtractor<>(
                        () -> structureService.getProjectList().stream(),
                        Arrays.asList(
                                projectNode
                        )
                )
        );
    }
}

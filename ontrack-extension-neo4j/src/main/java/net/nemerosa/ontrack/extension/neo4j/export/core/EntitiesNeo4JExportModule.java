package net.nemerosa.ontrack.extension.neo4j.export.core;

import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportModule;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordDef;
import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordExtractor;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class EntitiesNeo4JExportModule implements Neo4JExportModule {

    private final StructureService structureService;
    private final Neo4JExportRecordDef<Project> projectNode;
    private final Neo4JExportRecordDef<Branch> branchNode;

    @Autowired
    public EntitiesNeo4JExportModule(
            StructureService structureService,
            @Qualifier("Project")
                    Neo4JExportRecordDef<Project> projectNode,
            @Qualifier("Branch")
                    Neo4JExportRecordDef<Branch> branchNode
    ) {
        this.structureService = structureService;
        this.projectNode = projectNode;
        this.branchNode = branchNode;
    }

    @Override
    public List<Neo4JExportRecordExtractor<?>> getRecordExtractors() {
        return Arrays.asList(
                // Projects
                new Neo4JExportRecordExtractor<>(
                        () -> structureService.getProjectList().stream(),
                        Collections.singletonList(
                                projectNode
                        )
                ),
                // Branches
                new Neo4JExportRecordExtractor<Branch>(
                        () -> structureService.getProjectList().stream()
                                .flatMap(p -> structureService.getBranchesForProject(p.getId()).stream()),
                        Arrays.asList(
                                branchNode
                        )
                )
        );
    }
}

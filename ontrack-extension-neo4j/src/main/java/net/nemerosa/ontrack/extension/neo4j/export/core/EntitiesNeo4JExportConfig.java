package net.nemerosa.ontrack.extension.neo4j.export.core;

import net.nemerosa.ontrack.extension.neo4j.export.model.Neo4JExportRecordDef;
import net.nemerosa.ontrack.model.structure.Entity;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.Signature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class EntitiesNeo4JExportConfig {

    @Bean
    @Qualifier("Project")
    public Neo4JExportRecordDef<Project> projectNode() {
        return Neo4JExportRecordDef.<Project>node("Project", Entity::id)
                .with("name", Project::getName)
                .with("description", Project::getDescription)
                .with("disabled:boolean", Project::isDisabled)
                .with("creator", EntitiesNeo4JExportConfig::getSignatureCreator)
                .with("creation", EntitiesNeo4JExportConfig::getSignatureCreation)
                .build()
                ;
    }

    private static LocalDateTime getSignatureCreation(ProjectEntity entity) {
        Signature signature = entity.getSignature();
        return signature != null ? signature.getTime() : null;
    }

    private static String getSignatureCreator(ProjectEntity entity) {
        Signature signature = entity.getSignature();
        return signature != null ? signature.getUser().getName() : "";
    }

}

package net.nemerosa.ontrack.extension.neo4j;

import net.nemerosa.ontrack.extension.api.UserMenuExtension;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.GlobalFunction;
import net.nemerosa.ontrack.model.support.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Neo4JExportUserMenuExtension extends AbstractExtension implements UserMenuExtension {

    @Autowired
    public Neo4JExportUserMenuExtension(Neo4JExtensionFeature extensionFeature) {
        super(extensionFeature);
    }

    @Override
    public Class<? extends GlobalFunction> getGlobalFunction() {
        return ApplicationManagement.class;
    }

    @Override
    public Action getAction() {
        return Action.of("neo4j-export", "Exporting to Neo4J", "export");
    }
}

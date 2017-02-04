package net.nemerosa.ontrack.extension.neo4j.export

import net.nemerosa.ontrack.it.AbstractServiceTestSupport
import net.nemerosa.ontrack.model.structure.NameDescription
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class Neo4JExportTest extends AbstractServiceTestSupport {

    @Autowired
    private Neo4JExportService exportService

    @Test
    void 'Export of projects and branches'() {
        // Project and two branches
        def project = doCreateProject()
        doCreateBranch(project, NameDescription.nd('B1', ''))
        doCreateBranch(project, NameDescription.nd('B2', ''))
        // Another project and branch
        def branch = doCreateBranch()
        // Exporting
        def output = asAdmin().call { exportService.export(new Neo4JExportInput()) }
        // TODO Downloading
    }

}

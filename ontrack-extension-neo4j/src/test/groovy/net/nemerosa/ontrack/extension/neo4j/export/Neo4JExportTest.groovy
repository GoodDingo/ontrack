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
        // Checks the paths
        assert output.nodeFiles as Set == [
                'node/Project.csv',
                'node/Branch.csv',
        ] as Set
        assert output.relFiles as Set == [
                'rel/BRANCH_OF.csv',
        ] as Set
        // Downloading
        def document = asAdmin().call { exportService.download(output.uuid) }
        assert document.type == 'application/zip'
        assert document.content.length > 0
        // TODO Extract the zip
    }

}

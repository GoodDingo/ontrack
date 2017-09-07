package net.nemerosa.ontrack.repository

import net.nemerosa.ontrack.model.structure.ID
import net.nemerosa.ontrack.model.structure.NameDescription
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.test.TestUtils.uid
import org.junit.Assert.*
import org.junit.Test

class ProjectJdbcRepositoryIT : AbstractRepositoryTestSupport() {

    @Test
    fun create_project_with_null_description() {
        val p = projectRepository.newProject(Project.of(NameDescription.nd(uid("P"), null)))
        assertNotNull("Project is defined", p)
        assertNull(p.description)
        assertTrue("Project ID is defined", ID.isDefined(p.id))
    }

    @Test
    fun save_project_disabled() {
        var p = do_create_project()
        p = p.withDisabled(true)
        projectRepository.saveProject(p)
        p = projectRepository.getProject(p.id)
        assertTrue("Project must be disabled", p.isDisabled)
    }

}

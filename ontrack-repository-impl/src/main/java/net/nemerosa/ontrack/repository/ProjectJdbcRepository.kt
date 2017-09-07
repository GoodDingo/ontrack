package net.nemerosa.ontrack.repository

import net.nemerosa.ontrack.model.Ack
import net.nemerosa.ontrack.model.exceptions.ProjectNameAlreadyDefinedException
import net.nemerosa.ontrack.model.exceptions.ProjectNotFoundException
import net.nemerosa.ontrack.model.structure.ID
import net.nemerosa.ontrack.model.structure.NameDescription
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.repository.support.AbstractJdbcRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

@Repository
class ProjectJdbcRepository
@Autowired
constructor(
        dataSource: DataSource
) : AbstractJdbcRepository(dataSource), ProjectRepository {

    override fun newProject(project: Project): Project {
        // Creation
        try {
            val id = dbCreate(
                    "INSERT INTO PROJECTS(NAME, DESCRIPTION, DISABLED, CREATION, CREATOR) VALUES (:name, :description, :disabled, :creation, :creator)",
                    params("name", project.name)
                            .addValue("description", project.description)
                            .addValue("disabled", project.isDisabled)
                            .addValue("creation", AbstractJdbcRepository.dateTimeForDB(project.signature.time))
                            .addValue("creator", project.signature.user.name)
            )
            // Returns with ID
            return project.withId(id(id))
        } catch (ex: DuplicateKeyException) {
            throw ProjectNameAlreadyDefinedException(project.name)
        }
    }

    override fun getProjectList(): List<Project> = jdbcTemplate.query(
            "SELECT * FROM PROJECTS ORDER BY NAME"
    ) { rs, _ -> toProject(rs) }

    override fun getProject(projectId: ID): Project = try {
        namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM PROJECTS WHERE ID = :id",
                params("id", projectId.value)
        ) { rs, _ -> toProject(rs) }
    } catch (ex: EmptyResultDataAccessException) {
        throw ProjectNotFoundException(projectId)
    }

    override fun getProjectByName(project: String): Project? =
            getFirstItem(
                    "SELECT * FROM PROJECTS WHERE NAME = :name",
                    params("name", project)
            ) { rs, _ -> toProject(rs) }

    override fun saveProject(project: Project) {
        namedParameterJdbcTemplate.update(
                "UPDATE PROJECTS SET NAME = :name, DESCRIPTION = :description, DISABLED = :disabled WHERE ID = :id",
                params("name", project.name)
                        .addValue("description", project.description)
                        .addValue("disabled", project.isDisabled)
                        .addValue("id", project.id.value)
        )
    }

    override fun deleteProject(projectId: ID): Ack = Ack.one(
            namedParameterJdbcTemplate.update(
                    "DELETE FROM PROJECTS WHERE ID = :id",
                    params("id", projectId.value)
            )
    )

    @Throws(SQLException::class)
    protected fun toProject(rs: ResultSet): Project = Project.of(NameDescription(
            rs.getString("name"),
            rs.getString("description")
    ))
            .withId(id(rs.getInt("id")))
            .withSignature(readSignature(rs))
            .withDisabled(rs.getBoolean("disabled"))
}
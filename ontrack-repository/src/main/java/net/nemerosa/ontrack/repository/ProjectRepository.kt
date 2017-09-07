package net.nemerosa.ontrack.repository

import net.nemerosa.ontrack.model.Ack
import net.nemerosa.ontrack.model.structure.ID
import net.nemerosa.ontrack.model.structure.Project

interface ProjectRepository {

    fun newProject(project: Project): Project

    fun getProjectList(): List<Project>

    fun getProject(projectId: ID): Project

    fun getProjectByName(project: String): Project?

    fun saveProject(project: Project)

    fun deleteProject(projectId: ID): Ack

}
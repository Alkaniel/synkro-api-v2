package fr.enzogiardinelli.synkro.repositories;

import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.participants pp WHERE p.owner = :user OR pp.user = :user")
    List<Project> findProjetsForUser(@Param("user") User user);

    List<Project> findByOwner(User owner);

    List<Project> findByParticipants_User_Id(UUID userId);
}

package fr.enzogiardinelli.synkro.repositories;

import fr.enzogiardinelli.synkro.entities.tasks.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssignees_Id(UUID userId);
}

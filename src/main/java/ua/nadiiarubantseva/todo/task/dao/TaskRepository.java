package ua.nadiiarubantseva.todo.task.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findAllByUserEmailOrderByCreatedAtAsc(String email);

    boolean existsByUserEmailAndId(String userEmail, Long taskId);
}

package ua.nadiiarubantseva.todo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.roles r WHERE u.email = :email")
    Optional<User> loadUserByEmail(@Param("email") String email);

    @Query(value = "SELECT u.id FROM User u WHERE u.email = :email")
    Long getIdByEmail(@Param("email") String email);
}

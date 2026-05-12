package pl.san.articlesubmission.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleNameOrderByFullNameAsc(RoleName roleName);

    List<User> findAllByOrderByCreatedAtDesc();
}

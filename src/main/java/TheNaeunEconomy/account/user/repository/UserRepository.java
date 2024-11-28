package TheNaeunEconomy.account.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import TheNaeunEconomy.account.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findByDeleteDateBeforeAndDeleteDateIsNotNull(LocalDateTime threeMonthsAgo);

    @Query("SELECT FUNCTION('DATE_FORMAT', u.createDate, '%Y-%m') AS month, COUNT(u) FROM User u GROUP BY FUNCTION('DATE_FORMAT', u.createDate, '%Y-%m')")
    List<Object[]> countUsersByMonth();
}
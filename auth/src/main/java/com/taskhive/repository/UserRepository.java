package com.taskhive.repository;

import com.taskhive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) AND u.deletedAt IS NULL AND u.emailVerified = true AND u.email != :excludeEmail")
    List<User> searchByEmailExcluding(@Param("email") String email, @Param("excludeEmail") String excludeEmail);
}

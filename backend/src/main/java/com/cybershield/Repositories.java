package com.cybershield;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
interface AccountRepository extends JpaRepository<UserAccount,Long>{Optional<UserAccount> findByEmail(String email);}
interface SessionRepository extends JpaRepository<SessionToken,Long>{Optional<SessionToken> findByTokenHash(String tokenHash);void deleteByTokenHash(String tokenHash);}
interface ProgressRepository extends JpaRepository<LearningProgress,Long>{}

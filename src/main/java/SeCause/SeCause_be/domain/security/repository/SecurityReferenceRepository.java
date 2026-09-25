package SeCause.SeCause_be.domain.security.repository;

import SeCause.SeCause_be.domain.security.entity.SecurityReference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityReferenceRepository extends JpaRepository<SecurityReference, Long> {
}

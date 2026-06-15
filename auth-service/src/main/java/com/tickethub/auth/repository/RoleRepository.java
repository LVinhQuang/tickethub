package com.dormhub.auth.repository;

import com.dormhub.auth.domain.Role;
import com.dormhub.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}

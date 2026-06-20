package com.tickethub.auth.repository;

import com.tickethub.auth.domain.Role;
import com.tickethub.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}

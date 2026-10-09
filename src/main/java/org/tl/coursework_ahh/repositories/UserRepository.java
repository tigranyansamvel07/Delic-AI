package org.tl.coursework_ahh.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tl.coursework_ahh.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
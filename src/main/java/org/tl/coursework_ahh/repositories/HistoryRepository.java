package org.tl.coursework_ahh.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tl.coursework_ahh.models.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
}

package org.tl.coursework_ahh.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tl.coursework_ahh.models.Track;

public interface TrackRepository extends JpaRepository<Track, Long> {
}

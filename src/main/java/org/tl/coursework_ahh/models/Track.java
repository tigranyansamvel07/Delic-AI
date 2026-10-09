package org.tl.coursework_ahh.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@Entity
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String trackName, artistName, albumCover;
    private String youtubeLINK, spotifyLINK, appleMusicLINK;

    public Track(String trackName, String artistName, String albumCover,
                 String youtubeLINK, String spotifyLINK, String appleMusicLINK) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumCover = albumCover;
        this.youtubeLINK = youtubeLINK;
        this.spotifyLINK = spotifyLINK;
        this.appleMusicLINK = appleMusicLINK;
    }

}

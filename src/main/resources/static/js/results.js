
function selectTrack(element) {
    console.log('🎵 Track selected');

    // Get track data from attributes
    const trackName = element.getAttribute('data-track-name');
    const artistName = element.getAttribute('data-artist-name');
    const albumCover = element.getAttribute('data-album-cover');
    const spotifyLink = element.getAttribute('data-spotify-link');
    const youtubeLink = element.getAttribute('data-youtube-link');
    const appleMusicLink = element.getAttribute('data-apple-music-link');

    console.log('Track data:', { trackName, artistName, albumCover });

    updatePlayer(trackName, artistName, albumCover, spotifyLink, youtubeLink, appleMusicLink);

    highlightSelectedTrack(element);

}


function updateStreamingLinks(spotifyLink, youtubeLink, appleMusicLink) {
    const linksContainer = document.getElementById('streamingLinks');
    linksContainer.innerHTML = '';

    if (spotifyLink && spotifyLink !== 'Unknown' && spotifyLink !== 'none' && spotifyLink !== 'null') {
        const spotifyLinkElement = createStreamLink(spotifyLink, 'fab fa-spotify', 'Spotify');
        linksContainer.appendChild(spotifyLinkElement);
    }

    if (youtubeLink && youtubeLink !== 'Unknown' && youtubeLink !== 'none' && youtubeLink !== 'null') {
        const youtubeLinkElement = createStreamLink(youtubeLink, 'fab fa-youtube', 'YouTube');
        linksContainer.appendChild(youtubeLinkElement);
    }

    if (appleMusicLink && appleMusicLink !== 'Unknown' && appleMusicLink !== 'none' && appleMusicLink !== 'null') {
        const appleLinkElement = createStreamLink(appleMusicLink, 'fab fa-apple', 'Apple Music');
        linksContainer.appendChild(appleLinkElement);
    }
}

function createStreamLink(url, iconClass, platform) {
    const link = document.createElement('a');
    link.href = url;
    link.target = '_blank';
    link.className = 'stream-link';
    link.innerHTML = `<i class="${iconClass}"></i>`;
    link.title = platform;
    return link;
}

function highlightSelectedTrack(selectedElement) {
    const allTracks = document.querySelectorAll('.track-item');
    allTracks.forEach(track => {
        track.classList.remove('selected');
    });

    selectedElement.classList.add('selected');
}


function playNext() {
    console.log('⏭️ Next clicked');
    const selectedTrack = document.querySelector('.track-item.selected');
    if (selectedTrack) {
        const nextTrack = selectedTrack.nextElementSibling;
        if (nextTrack && nextTrack.classList.contains('track-item')) {
            selectTrack(nextTrack);
        }
    }
}

function playPrevious() {
    console.log('⏮️ Previous clicked');
    const selectedTrack = document.querySelector('.track-item.selected');
    if (selectedTrack) {
        const prevTrack = selectedTrack.previousElementSibling;
        if (prevTrack && prevTrack.classList.contains('track-item')) {
            selectTrack(prevTrack);
        }
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('🎵 Page loaded');
    const tracks = document.querySelectorAll('.track-item');

    setTimeout(() => {
        if (tracks.length > 0) {
            console.log('🎵 Auto-selecting first track');
            selectTrack(tracks[0]);
        }
    }, 500);
});

function updatePlayer(trackName, artistName, albumCover, spotifyLink, youtubeLink, appleMusicLink) {

    document.getElementById('currentSongTitle').textContent = trackName || 'Unknown Track';
    document.getElementById('currentSongArtist').textContent = artistName || 'Unknown Artist';

    const albumCoverImage = document.getElementById('albumCoverImage');
    const albumPlaceholder = document.getElementById('albumPlaceholder');

    if (albumCover && albumCover !== 'Unknown' && albumCover !== 'none' && albumCover !== 'null') {
        const img = new Image();
        img.onload = function() {
            albumCoverImage.src = albumCover;
            albumCoverImage.style.display = 'block';
            albumPlaceholder.style.display = 'none';
            console.log('✅ Album cover loaded successfully:', albumCover);
        };
        img.onerror = function() {
            console.error('❌ Failed to load album cover:', albumCover);
            albumCoverImage.style.display = 'none';
            albumPlaceholder.style.display = 'flex';
        };
        img.src = albumCover;

        loadHighQualityImage(albumCover);
    } else {
        albumCoverImage.style.display = 'none';
        albumPlaceholder.style.display = 'flex';
        console.log('🖼️ No album cover available, using placeholder');
    }

    updateStreamingLinks(spotifyLink, youtubeLink, appleMusicLink);
}

function loadHighQualityImage(originalUrl) {
    if (originalUrl.includes('thumb') || originalUrl.includes('small')) {
        const highQualityUrl = originalUrl
            .replace('thumb', 'large')
            .replace('small', 'large')
            .replace('60x60', '300x300')
            .replace('100x100', '500x500')
            .replace('150x150', '600x600');

        console.log('🔍 Trying high quality version:', highQualityUrl);

        const hqImg = new Image();
        hqImg.onload = function() {
            console.log('✅ High quality image available');
            document.getElementById('albumCoverImage').src = highQualityUrl;
        };
        hqImg.src = highQualityUrl;
    }
}


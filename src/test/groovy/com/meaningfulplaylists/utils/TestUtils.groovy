package com.meaningfulplaylists.utils

import com.meaningfulplaylists.domain.models.Track

class TestUtils {
    static Track createTrack(String title) {
        return new Track("id-${title}",
                title,
                "artist-${title}",
                "uri-${title}"
        )
    }
}

package app.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StreamPickerTest {
    private fun v(h: Int, fps: Int, c: String = "avc1.640028", cont: String = "mp4") =
        VideoCandidate("u$h$fps", cont, c, h, fps)

    @Test fun picks1080p60WhenAvailable() {
        val r = StreamPicker.bestVideoForMp4(listOf(v(1080, 30), v(1080, 60), v(720, 60)))
        assertEquals(60, r?.fps); assertEquals(1080, r?.height)
    }
    @Test fun ignoresVp9ForMp4() {
        assertNull(StreamPicker.bestVideoForMp4(listOf(v(1080, 60, "vp09.00.40.08", "webm"))))
    }
    @Test fun capsAtRequestedHeight() {
        assertEquals(720, StreamPicker.bestVideoForMp4(listOf(v(1080, 60), v(720, 30)), maxHeight = 720)?.height)
    }
}

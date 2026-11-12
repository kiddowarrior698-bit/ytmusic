package app.core

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

object Mp4Muxer {
    /** Junta um MP4 só de vídeo com um m4a só de áudio, sem recodificar. Lança exceção se falhar. */
    fun mux(video: File, audio: File, out: File) {
        val vEx = MediaExtractor()
        val aEx = MediaExtractor()
        var muxer: MediaMuxer? = null
        var started = false
        try {
            vEx.setDataSource(video.absolutePath)
            aEx.setDataSource(audio.absolutePath)
            val vIn = selectTrack(vEx, "video/")
            val aIn = selectTrack(aEx, "audio/")
            val vFmt = vEx.getTrackFormat(vIn)
            val aFmt = aEx.getTrackFormat(aIn)

            muxer = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val vOut = muxer.addTrack(vFmt)
            val aOut = muxer.addTrack(aFmt)
            muxer.start()
            started = true

            val buf = ByteBuffer.allocate(maxOf(sampleCap(vFmt), sampleCap(aFmt)))
            val info = MediaCodec.BufferInfo()
            var vDone = false
            var aDone = false
            while (!vDone || !aDone) {
                // Intercala pelo menor timestamp para o arquivo ficar bem entrelaçado.
                val takeVideo = when {
                    vDone -> false
                    aDone -> true
                    else -> vEx.sampleTime <= aEx.sampleTime
                }
                val ex = if (takeVideo) vEx else aEx
                val track = if (takeVideo) vOut else aOut
                val size = ex.readSampleData(buf, 0)
                if (size < 0) {
                    if (takeVideo) vDone = true else aDone = true
                    continue
                }
                info.set(
                    0, size, ex.sampleTime,
                    if (ex.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0)
                        MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
                )
                muxer.writeSampleData(track, buf, info)
                ex.advance()
            }
        } catch (t: Throwable) {
            out.delete()
            throw t
        } finally {
            runCatching { if (started) muxer?.stop() }
            runCatching { muxer?.release() }
            runCatching { vEx.release() }
            runCatching { aEx.release() }
        }
    }

    private fun selectTrack(ex: MediaExtractor, prefix: String): Int {
        for (i in 0 until ex.trackCount) {
            val mime = ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith(prefix)) { ex.selectTrack(i); return i }
        }
        error("Nenhuma trilha $prefix encontrada")
    }

    private fun sampleCap(f: MediaFormat): Int =
        if (f.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) f.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        else 2 * 1024 * 1024
}

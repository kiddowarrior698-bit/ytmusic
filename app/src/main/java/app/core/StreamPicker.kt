package app.core

data class AudioCandidate(
    val url: String,
    val container: String,   // "m4a", "webm"
    val codec: String,       // "mp4a.40.2", "opus"
    val bitrateKbps: Int
)

data class VideoCandidate(
    val url: String,
    val container: String,   // "mp4", "webm"
    val codec: String,       // "avc1.64002a", "vp09..."
    val height: Int,
    val fps: Int
)

object StreamPicker {
    /** Para música: o melhor bitrate real disponível, em qualquer formato. */
    fun bestAudio(list: List<AudioCandidate>): AudioCandidate? =
        list.filter { it.bitrateKbps > 0 }.maxByOrNull { it.bitrateKbps }

    /** Para juntar em MP4: só AAC dentro de m4a. */
    fun bestAudioForMp4(list: List<AudioCandidate>): AudioCandidate? =
        list.filter { it.container == "m4a" && it.codec.startsWith("mp4a") && it.bitrateKbps > 0 }
            .maxByOrNull { it.bitrateKbps }

    /** Maior resolução, depois maior fps, dentro do teto pedido. H.264 em MP4 para o MediaMuxer aceitar. */
    fun bestVideoForMp4(list: List<VideoCandidate>, maxHeight: Int = 1080, maxFps: Int = 60): VideoCandidate? =
        list.filter { it.container == "mp4" && it.codec.startsWith("avc1") && it.height <= maxHeight && it.fps <= maxFps }
            .sortedWith(compareByDescending<VideoCandidate> { it.height }.thenByDescending { it.fps })
            .firstOrNull()

    /** Texto honesto para a UI: o que existe de verdade neste vídeo. */
    fun describe(video: List<VideoCandidate>, audio: List<AudioCandidate>): String {
        val v = video.maxWithOrNull(compareBy<VideoCandidate> { it.height }.thenBy { it.fps })
        val a = audio.maxByOrNull { it.bitrateKbps }
        return "Vídeo máx.: ${v?.let { "${it.height}p${it.fps}" } ?: "nenhum"} | " +
               "Áudio máx.: ${a?.let { "${it.bitrateKbps} kbps ${it.codec}" } ?: "nenhum"}"
    }
}

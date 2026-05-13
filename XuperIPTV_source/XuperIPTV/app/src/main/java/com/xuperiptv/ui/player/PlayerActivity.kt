package com.xuperiptv.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.bumptech.glide.Glide
import com.xuperiptv.R
import com.xuperiptv.databinding.ActivityPlayerBinding
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL  = "extra_url"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_LOGO = "extra_logo"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideControls() }
    private val HIDE_DELAY_MS = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url  = intent.getStringExtra(EXTRA_URL)  ?: return finish()
        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        val logo = intent.getStringExtra(EXTRA_LOGO)

        setupUI(name, logo)
        initPlayer(url)
    }

    private fun setupUI(name: String, logo: String?) {
        binding.tvChannelName.text = name

        if (!logo.isNullOrBlank()) {
            Glide.with(this).load(logo)
                .placeholder(R.drawable.ic_channel_placeholder)
                .into(binding.ivChannelLogo)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.playerView.setOnClickListener { toggleControls() }

        scheduleHide()
    }

    private fun initPlayer(url: String) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("XuperIPTV/1.0")
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo

            val mediaItem = MediaItem.fromUri(Uri.parse(url))

            // HLS automático si la URL lo es, de lo contrario usa el player genérico
            if (url.contains(".m3u8") || url.contains("hls")) {
                val source = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                exo.setMediaSource(source)
            } else {
                exo.setMediaItem(mediaItem)
            }

            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showBuffering(true)
                        Player.STATE_READY     -> {
                            showBuffering(false)
                            binding.tvError.visibility = View.GONE
                        }
                        Player.STATE_ENDED     -> finish()
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    showBuffering(false)
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "Error: ${error.message}"
                }
            })
        }
    }

    // ── Controles ──────────────────────────────────────────────────────────

    private fun showBuffering(show: Boolean) {
        binding.progressBuffering.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showControls() {
        binding.controlsOverlay.visibility = View.VISIBLE
        scheduleHide()
    }

    private fun hideControls() {
        binding.controlsOverlay.visibility = View.GONE
    }

    private fun toggleControls() {
        if (binding.controlsOverlay.visibility == View.VISIBLE) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun scheduleHide() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, HIDE_DELAY_MS)
    }

    // ── Control remoto ────────────────────────────────────────────────────

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        showControls()
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
                true
            }
            KeyEvent.KEYCODE_BACK -> { finish(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onPause()  { super.onPause();  player?.pause() }
    override fun onResume() { super.onResume(); player?.play() }

    override fun onDestroy() {
        hideHandler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
        super.onDestroy()
    }
}

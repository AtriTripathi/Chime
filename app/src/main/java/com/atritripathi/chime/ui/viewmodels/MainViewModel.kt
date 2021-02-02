package com.atritripathi.chime.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atritripathi.chime.data.entities.Song
import com.atritripathi.chime.exoplayer.MusicServiceConnection
import com.atritripathi.chime.exoplayer.isPlayEnabled
import com.atritripathi.chime.exoplayer.isPlaying
import com.atritripathi.chime.exoplayer.isPrepared
import com.atritripathi.chime.others.Constants.MEDIA_ROOT_ID
import com.atritripathi.chime.others.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: MutableLiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))   // Shows Loading state initially.
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                mediaItemsList: MutableList<MediaItem>
            ) {
                super.onChildrenLoaded(parentId, mediaItemsList)
                val songItemsList = mediaItemsList.map { mediaItem ->
                    Song(
                        mediaItem.mediaId!!,
                        mediaItem.description.title.toString(),
                        mediaItem.description.subtitle.toString(),
                        mediaItem.description.mediaUri.toString(),
                        mediaItem.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(songItemsList)) // Update the songs list
            }
        })
    }

    fun skipToNextSong() = musicServiceConnection.transportControls.skipToNext()

    fun skipToPreviousSong() = musicServiceConnection.transportControls.skipToPrevious()

    fun skipTo(pos: Long) = musicServiceConnection.transportControls.seekTo(pos)

    /**
     * To play a given song or toggle the play/pause state of the song currently playing
     */
    fun playOrToggle(mediaItem: Song, toggle: Boolean = false) {
        val isPlayerPrepared = playbackState.value?.isPrepared ?: false
        if (isPlayerPrepared && mediaItem.mediaId ==
            currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}
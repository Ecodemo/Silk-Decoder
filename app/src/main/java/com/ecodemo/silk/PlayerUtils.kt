package com.ecodemo.silk;

import java.io.File
import android.os.Handler
import android.os.Message
import android.app.AlertDialog
import java.io.FileInputStream
import android.widget.SeekBar
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.content.DialogInterface
import com.ecodemo.silk.databinding.MediaBinding
import android.widget.SeekBar.OnSeekBarChangeListener

/* 播放器 Dialog */
class PlayerUtils {

    private val mediaPlayer = MediaPlayer()
    private lateinit var binding: MediaBinding
    private var total_time: Int = 0
    @Suppress("UNUSED", "DEPRECATION")
    private var handler: Handler = Handler {
        binding.progress.setProgress(it.obj as Int)
        var a = timeFormat(it.obj as Int)
        var b = timeFormat(total_time)
        binding.time.text = "$a/$b"
        false
    }
    private var playing = true;
    private val thread: Thread = Thread {
        while (playing){
            var currentPosition = mediaPlayer.getCurrentPosition()
            var msg = Message()
            msg.what = 0
            msg.obj = currentPosition
            handler.sendMessage(msg)
            Thread.sleep(500)
            continue
        }
    }
    constructor(context: Context, file: File) {
        binding = MediaBinding.inflate(LayoutInflater.from(context))
        var dialog =  AlertDialog.Builder(context)
        binding.name.text = file.name
        dialog.setView(binding.root)
        dialog.setTitle("音乐播放器")
        dialog.setNegativeButton("播放/暂停", DialogInterface.OnClickListener() {dialog_, _ ->
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
                //thread.start()
            }
            var field = dialog_.javaClass.getSuperclass().getDeclaredField("mShowing")
            field.setAccessible(true)
            field.set(dialog_, false) /* 防止消失 */
            //dialog_.dismiss()
        })
        dialog.setPositiveButton("关闭", DialogInterface.OnClickListener() {_dialog, _ ->
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop()
            }
            var field = _dialog.javaClass.getSuperclass().getDeclaredField("mShowing")
            field.setAccessible(true)
            field.set(_dialog, true)/* 防止不消失 */
            _dialog.dismiss()
        })
        dialog.setOnCancelListener(DialogInterface.OnCancelListener(){_->
            playing = false
        })
        var d = dialog.create()
        d.setCanceledOnTouchOutside(false)
        d.show()
        
        binding.progress.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                mediaPlayer.seekTo(p0?.getProgress()!!)
                mediaPlayer.start()
            }
        })
        var fis = FileInputStream(file)
		mediaPlayer.setDataSource(fis.getFD())
		mediaPlayer.prepare()
		mediaPlayer.start()
		total_time = mediaPlayer.getDuration()
		binding.progress.setMax(mediaPlayer.getDuration())
		thread.start()
	}
	
	/* ms to hms*/
	fun timeFormat(timeMs: Int): String? {
        var totalSeconds = timeMs/1000
        var seconds = totalSeconds % 60
        var minutes = (totalSeconds/60)%60
        var hours = totalSeconds/3600
        return String.format("%02d:%02d:%02d",hours,minutes,seconds)
	}
}
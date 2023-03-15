package com.example.audiorecorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity()
{
    lateinit var startTV: TextView
    lateinit var stopTV: TextView
    lateinit var playTV: TextView
    lateinit var stopplayTV: TextView
    lateinit var statusTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    private var playingAudio = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Assign the layout content to variables
        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        stopTV = findViewById(R.id.btnSave)
        playTV = findViewById(R.id.btnPlay)
        stopplayTV = findViewById(R.id.btnStopPlay)

        //Assign Listeners to each button
        startTV.setOnClickListener {
            startRecording()
        }

        stopTV.setOnClickListener {
            pauseRecording()
        }

        playTV.setOnClickListener {
            playAudio()
        }

        stopplayTV.setOnClickListener {
            pausePlaying()
        }
    }

    private fun startRecording()
    {
        // Check permissions
        if (checkPermissions())
        {
            if (mRecorder == null)
            {
                playingAudio = false

                // Save file
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

                // If file exists then increment counter
                var n = 0
                while (mFileName!!.exists())
                {
                    n++
                    mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
                }

                // Initialize the class MediaRecorder
                mRecorder = MediaRecorder()

                // Set source to get audio
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

                // Set the format of the file
                mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

                // Set the audio encoder
                mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                // Set the save path
                mRecorder!!.setOutputFile(mFileName)
                try
                {
                    // Preparation of the audio file
                    mRecorder!!.prepare()
                } catch (e: IOException)
                {
                    Log.e("TAG", "prepare() failed")
                }
                // Start the audio recording
                mRecorder!!.start()
                statusTV.text = "Recording in progress"
                //Change the button logos
                startTV.setBackgroundResource(R.drawable.btn_rec_stop)
                stopplayTV.setBackgroundResource(R.drawable.btn_rec_stop_play)
            }
            else
            {
                //Stop Recording
                mRecorder!!.stop()

                // Release the class mRecorder
                mRecorder!!.release()
                mRecorder = null
                statusTV.text = "Recording interrupted"
                //Change the button logos
                startTV.setBackgroundResource(R.drawable.btn_rec_start)
            }
        } else
        {
            // Request permissions
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode)
        {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty())
            {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord)
                {
                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()
                }
                else
                {
                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean
    {
        // Check permissions
        return  ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions()
    {
        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    private fun playAudio()
    {
        // Use the MediaPlayer class to listen to recorded audio files
        mPlayer = MediaPlayer()
        try
        {
            if(!mPlayer!!.isPlaying && !playingAudio)
            {
                // Preleva la fonte del file audio
                mPlayer!!.setDataSource(mFileName.toString())

                // Fetch the source of the mPlayer
                mPlayer!!.prepare()

                // Start the mPlayer
                mPlayer!!.start()
                statusTV.text = "Listening recording"

                playingAudio = true
                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_pause)
                stopplayTV.setBackgroundResource(R.drawable.btn_rec_stop_play)
            }
            else
            {
                mPlayer!!.pause()
                statusTV.text = "Listening Paused"

                playingAudio = false
                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_play)
                stopplayTV.setBackgroundResource(R.drawable.btn_rec_stop_play)
            }


        } catch (e: IOException)
        {
            Log.e("TAG", "prepare() failed")
        }
    }

    private fun pauseRecording()
    {
        // Stop recording
        if (mFileName == null)
        {
            // Message
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_LONG).show()

        } else
        {
            if(mRecorder == null)
            {
                // Message to confirm save file
                val savedUri = Uri.fromFile(mFileName)
                val msg = "File saved: " + savedUri!!.lastPathSegment
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

                //Show save message
                statusTV.text = "Saved Recording"
            }
        }
    }

    private fun pausePlaying()
    {
        if(mPlayer != null)
        {
            if(mPlayer!!.isPlaying || playingAudio)
            {
                mPlayer!!.stop()
                // Stop playing the audio file
                statusTV.text = "Recording stopped"
                //Change the button logos
                playTV.setBackgroundResource(R.drawable.btn_rec_play)
                stopplayTV.setBackgroundResource(R.drawable.btn_rec_delete)

                playingAudio = false
            }
            else
            {
                //Release mPlayer Reproductor
                mPlayer!!.release()
                mPlayer = null
                statusTV.text = "Last Recording deleted from reproductor cache"
                //Change the button logos
                stopplayTV.setBackgroundResource(R.drawable.btn_rec_stop_play)
            }
        }
        else
        {
            // No current audio recorded
            statusTV.text = "Nothing Recorded"
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}
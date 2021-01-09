package com.example.camerax.ui.view

import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import com.example.camerax.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), Executor {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            txv.post{
                startCamera()
            }
        }else{
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            1234)
        }
    }

    private fun startCamera() {
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            setTargetAspectRatio(Rational(1, 1))
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        btnSave.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, object: ImageCapture.OnImageSavedListener{
                override fun onImageSaved(file: File) {
                    Toast.makeText(this@MainActivity, "Image saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    Log.d("IMG_SAVED", "Image saved to ${file.absolutePath}")
                }

                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    //Toast.makeText(this@MainActivity, "Error $message", Toast.LENGTH_LONG).show()
                }

            })
        }

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(1080, 1080))
            setTargetAspectRatio(Rational(1, 1))
            setLensFacing(CameraX.LensFacing.BACK)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val parent = txv.parent as ViewGroup
            parent.removeView(txv)
            parent.addView(txv, 0)
            updateTransform()
            txv.setSurfaceTexture(it.surfaceTexture)
        }

        CameraX.bindToLifecycle(this, preview, imageCapture)

    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = txv.width / 2f
        val centerY = txv.height / 2f

        val rotationDegrees = when(txv.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        txv.setTransform(matrix)
    }

    override fun execute(command: Runnable?) {
        command?.run()
    }
}
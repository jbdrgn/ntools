package com.example.ntools

import android.content.Context
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRScannerTool(private val context: Context) {
	private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
	private var cameraProvider: ProcessCameraProvider? = null
	private var camera: Camera? = null

	fun startScanning(
		previewView: PreviewView,
		lifecycleOwner: LifecycleOwner,
		onQrCodeScanned: (String) -> Unit
	) {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
		
		cameraProviderFuture.addListener({
			cameraProvider = cameraProviderFuture.get()
			
			val preview = Preview.Builder()
				.setTargetResolution(Size(default_camera_width, default_camera_height))
				.build()
				.also {
					it.surfaceProvider = previewView.surfaceProvider
				}

			val imageAnalyzer = ImageAnalysis.Builder()
				.setTargetResolution(Size(default_camera_width, default_camera_height))
				.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
				.build()
				.also {
					it.setAnalyzer(cameraExecutor) { imageProxy ->
						processImage(imageProxy, onQrCodeScanned)
					}
				}

			try {
				cameraProvider?.unbindAll()
				camera = cameraProvider?.bindToLifecycle(
					lifecycleOwner,
					CameraSelector.DEFAULT_BACK_CAMERA,
					preview,
					imageAnalyzer
				)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}, ContextCompat.getMainExecutor(context))
	}

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(
		imageProxy: ImageProxy,
		onQrCodeScanned: (String) -> Unit
	) {
		val mediaImage = imageProxy.image
		if (mediaImage != null) {
			val image = InputImage.fromMediaImage(
				mediaImage,
				imageProxy.imageInfo.rotationDegrees
			)
			
			val scanner = BarcodeScanning.getClient()
			scanner.process(image)
				.addOnSuccessListener { barcodes ->
					for (barcode in barcodes) {
						if (barcode.valueType == Barcode.TYPE_URL) {
							barcode.url?.url?.let { url ->
								onQrCodeScanned(url)
							}
						} else {
							barcode.rawValue?.let { value ->
								onQrCodeScanned(value)
							}
						}
					}
				}
				.addOnCompleteListener {
					imageProxy.close()
				}
		} else {
			imageProxy.close()
		}
	}

	fun stopScanning() {
		cameraProvider?.unbindAll()
		cameraExecutor.shutdown()
	}
}
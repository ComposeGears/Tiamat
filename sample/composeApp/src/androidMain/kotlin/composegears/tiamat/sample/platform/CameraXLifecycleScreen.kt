package composegears.tiamat.sample.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composegears.tiamat.compose.navDestination
import composegears.tiamat.sample.ui.AppButton
import composegears.tiamat.sample.ui.Screen
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

val CameraXLifecycleScreen by navDestination {
    val viewModel = viewModel<CameraPreviewViewModel>()
    val context = LocalContext.current

    var isPermissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isPermissionGranted = it }
    )
    LaunchedEffect(Unit) {
        when {
            context.isPermissionGranted(Manifest.permission.CAMERA) -> isPermissionGranted = true
            context.shouldShowRationale(Manifest.permission.CAMERA) -> isPermissionGranted = false
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    Screen("CameraX + Lifecycle") {
        if (isPermissionGranted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    CameraView(viewModel)
                }

                val lf = LocalLifecycleOwner.current
                Text("Lifecycle State: ${lf.lifecycle.currentStateAsState().value}")
            }
        } else {
            PermissionDeclined {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
private fun PermissionDeclined(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera permission not granted")
        AppButton(text = "Request permission", onClick = onRequest)
    }
}

@Composable
private fun CameraView(viewModel: CameraPreviewViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraSelector by remember { mutableStateOf(DEFAULT_BACK_CAMERA) }
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleOwner, cameraSelector) {
        viewModel.bindToCamera(
            appContext = context.applicationContext,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector
        )
    }
    surfaceRequest?.let {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraXViewfinder(surfaceRequest = it)

            Icon(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .size(64.dp)
                    .padding(1.dp)
                    .border(1.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        Toast.makeText(context, "Take photo", Toast.LENGTH_SHORT).show()
                    },
                imageVector = Icons.Sharp.Lens,
                contentDescription = null
            )
            Icon(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 36.dp, end = 24.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        cameraSelector = when (cameraSelector) {
                            DEFAULT_BACK_CAMERA -> DEFAULT_FRONT_CAMERA
                            else -> DEFAULT_BACK_CAMERA
                        }
                    },
                imageVector = Icons.Sharp.FlipCameraAndroid,
                contentDescription = null
            )
        }
    }
}

private fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

private fun Context.shouldShowRationale(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(
        this as Activity,
        permission
    )

internal class CameraPreviewViewModel : ViewModel() {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    suspend fun bindToCamera(
        appContext: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector
    ) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector,
            cameraPreviewUseCase
        )

        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }
}
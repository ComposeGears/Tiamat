package content.examples.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.composegears.tiamat.navController
import com.composegears.tiamat.navDestination
import content.examples.common.BackButton
import content.examples.common.NextButton
import content.examples.common.TextBody

val AndroidViewLifecycleScreen by navDestination<Unit> {
    val context = LocalContext.current
    val navColor = navController()

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

    if (isPermissionGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val lf = LocalLifecycleOwner.current
            TextBody("Lifecycle State: ${lf.lifecycle.currentState}")

            Box(
                modifier = Modifier.fillMaxSize(0.8f),
                contentAlignment = Alignment.Center
            ) {
                CameraView()
            }
            BackButton(onClick = navColor::back)
        }
    } else {
        PermissionDeclined {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
        TextBody(text = "Camera permission not granted")
        NextButton(text = "Request permission", onClick = onRequest)
    }
}

@Composable
private fun CameraView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(LENS_FACING_BACK) }

    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val cameraSelector = remember(lensFacing) {
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }
    LaunchedEffect(lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            factory = { previewView }
        )
        Icon(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
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
                .navigationBarsPadding()
                .padding(bottom = 36.dp, end = 24.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    lensFacing = when (lensFacing) {
                        LENS_FACING_BACK -> LENS_FACING_FRONT
                        else -> LENS_FACING_BACK
                    }
                },
            imageVector = Icons.Sharp.FlipCameraAndroid,
            contentDescription = null
        )
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
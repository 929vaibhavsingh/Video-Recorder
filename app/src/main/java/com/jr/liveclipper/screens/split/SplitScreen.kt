package com.jr.liveclipper.screens.split

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jr.liveclipper.LiveClipperApp
import com.jr.liveclipper.splitsManager.SplitsManager
import com.jr.liveclipper.splitsManager.SplitsManagerImpl
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
internal fun SplitScreen(
    filePath: String,
    navHostController: NavHostController,
    splitsManager: SplitsManagerImpl,
    customSplitViewModel: CustomSplitViewModel = viewModel(),
) {
    val splitState = splitsManager.state.collectAsStateWithLifecycle()
    val splitSlices = splitsManager.slices.observeAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = Unit, block = {
        val contentUri = FileProvider.getUriForFile(
            LiveClipperApp.getApplicationContext(),
            "com.jr.liveclipper.fileprovider",
            File(filePath)
        )
            splitsManager.importFile(contentUri)
//            val time: Long = (customSplitViewModel.selectedValue.value ?: 0f).toLong()
//            splitsManager.doCustomSplits(time = TimeUnit.SECONDS.toMillis(time))
    })

    when (splitState.value) {
        SplitsManager.State.READY_TO_SPLIT -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    scope.launch {
                        val time: Long = (customSplitViewModel.selectedValue.value ?: 0f).toLong()
                        splitsManager.doCustomSplits(time = TimeUnit.SECONDS.toMillis(time))
                    }
                }) {
                    Text(text = "Split")
                }
            }
        }

        SplitsManager.State.SPLITTING_ERROR -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Error Occurred")
            }
        }

        SplitsManager.State.SPLITTING_DONE -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    splitSlices.value?.let { slices ->
                        items(slices) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(text = it.outputFilePath)
                            }
                        }
                    }
                }
            }
        }

        SplitsManager.State.IDLE -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "IDLE")
            }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.cpu_usage.CpuUsageMeasurer
import com.example.cpu_usage.proc.ProcStatAccessor
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.HeavyWorkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val procStatAccessor = ProcStatAccessor()
    private val cpuUsageMeasurer = CpuUsageMeasurer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                val procStat = procStatAccessor.getProcStat()
                val clockSpeed = procStatAccessor.clockTickHz
                val processorCount = procStatAccessor.processorCount
                withContext(Dispatchers.Main) {
                    render(
                        "$procStat \n\n" +
                                "clockSpeed $clockSpeed\n\n" +
                                "processorCount $processorCount\n\n" +
                                "${cpuUsageMeasurer.getInfo()}"
                    )
                }
                delay(1000)
            }
        }
    }

    private fun render(toShow: String) {
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center)
                        ) {
                            Text(
                                toShow,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            var heavyWorkInProgress by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    if (!heavyWorkInProgress) {
                                        heavyWorkInProgress = true
                                        lifecycleScope.launch(Dispatchers.Default) {
                                            HeavyWorkUtil().doHeavyWork()
                                            heavyWorkInProgress = false
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    if (heavyWorkInProgress) "Working..." else "Do heavy work",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

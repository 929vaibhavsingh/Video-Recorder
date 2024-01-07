package com.jr.liveclipper.screens.split

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomSplitViewModel : ViewModel() {

    val minValue = MutableLiveData<Float>()
    val maxValue = MutableLiveData<Float>()
    val selectedValue = MutableLiveData<Float>()

    init {
        minValue.value = 0f
        maxValue.value = 1f
        selectedValue.value = 5f
    }
}
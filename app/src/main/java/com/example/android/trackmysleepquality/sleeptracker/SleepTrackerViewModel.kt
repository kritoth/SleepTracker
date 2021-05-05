/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    /*
     * Properly encapsulated var to hold the current night, must be mutable in order to
     * let it be used for writing into the database
     */
    private var _tonight = MutableLiveData<SleepNight>()
    val tonight: LiveData<SleepNight>
        get() = _tonight

    /*
     * Getting all the nights from the database
     */
    private val nights = database.getAllNights()

    /*
     * initialize the tonight variable via a private method
     */
    init {
        initializeTonight()
    }

    /*
     * The viewModelScope.launch{} is to start a coroutine in the ViewModelScope.
     * See: https://developer.android.com/topic/libraries/architecture/coroutines
     */
    private fun initializeTonight(){
        viewModelScope.launch { _tonight.value = getTonightFromDatabase() }
    }

    /*
     * Let the coroutine get tonight value from the database.
     * If the start and end times are not the same, it means the night has already been completed so return null.
     * Otherwise, return night.
     */
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()

        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    /*
     * Event handler for click listening. When Start button is clicked:
     * 1. creates a new Entity
     * 2. writes it into the database via coroutine
     * 3. sets the member val tonight's value to this Entity
     */
    fun onStartTracking(){
        viewModelScope.launch {
            // Create a new night, which captures the current time,
            // and insert it into the database.
            val newNight = SleepNight()

            insert(newNight)

            _tonight.value = getTonightFromDatabase()
        }
    }
    /* Effectively writes the @param newNight into the database */
    private suspend fun insert(newNight: SleepNight) {
        database.insert(newNight)
    }

    /*
     * Event handler for click listening. When Stop button is clicked:
     * 1. gets member val tonight's value
     * 2. sets its end time of the recording
     * 3. updates the record/data (entity) in the database via coroutine
     */
    fun onStopTracking(){
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val nightsEnd = _tonight.value ?: return@launch

            // Update the night in the database to add the end time.
            nightsEnd.endTimeMilli = System.currentTimeMillis()

            update(nightsEnd)
        }
    }
    /* Updates the database record with {@param SleepNight} */
    private suspend fun update(night: SleepNight){
        database.update(night)
    }

    /*
     * Event handler for click listening. When Clear button is clicked:
     * 1. clears the database via coroutine
     * 2. sets the member val tonight's value
     */
    fun onClear(){
        viewModelScope.launch {
            clear()
            // And clear tonight since it's no longer in the database
            _tonight.value = null
        }
    }
    /* Clears the database */
    private suspend fun clear(){
        database.clear()
    }

    /*
     * Transform a list of {@link SleepNight} entity into a nightsString using the formatNights() function
     * from {@link Util.kt}
     */
    val nightsString = Transformations.map(nights){ nights ->
        formatNights(nights, application.resources)
    }
}


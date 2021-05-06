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

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.launch

class SleepQualityViewModel(
        val databaseDao: SleepDatabaseDao,
        private val sleepNightKey: Long = 0L) : ViewModel(){

    /*
    * Properly encapsulated var to hold the navigation event, must be mutable in order to
    * let it be set when navigation is done
    */
    private var  _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker

    /*
    * Sets the event when navigation is done
    */
    fun navigationDone(){
        _navigateToSleepTracker.value = null
    }

    /*
     * Event handler for click listening. When any of the smilies are clicked:
     * 1. gets the SleepNight entity from database by using its ID received in constructor
     * 2. sets its quality received as @param
     * 3. updates the entity in the database with the newly set quality record
     * 4. sets the navigation event var to allow its observers to be notified
     */
    fun onSetSleepQuality(quality: Int){
        viewModelScope.launch {
            val tonight = databaseDao.get(sleepNightKey)
            tonight.sleepQuality = quality
            databaseDao.update(tonight)
            _navigateToSleepTracker.value = true
        }
    }
}
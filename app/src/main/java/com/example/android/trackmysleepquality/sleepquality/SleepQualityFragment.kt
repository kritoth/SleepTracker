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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment(
        private val sleepNightKey: Long = 0L) : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepQualityBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_quality, container, false)

        val application = requireNotNull(this.activity).application

        //Get the arguments sent through navigation
        val arguments = SleepQualityFragmentArgs.fromBundle(arguments!!)
        //Get the dataSource
        val dataSource  = SleepDatabase.getInstance(application).sleepDatabaseDao
        //create a factory
        val sleepQualityViewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)
        //Get a ViewModel reference
        val viewModel = ViewModelProvider(this, sleepQualityViewModelFactory).get(SleepQualityViewModel::class.java)
        //Add the ViewModel to the binding object
        binding.sleepQualityViewModel = viewModel

        // Observe if a navigation is needed, ie. when the event var's state changes which occurs when any of the smilies are clicked
        // Do the navigation and then change the state of the event var's state back indicating navigation is done
        viewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
            if(it == true){ // Observed state is true, so ready to navigate
                this.findNavController()
                        .navigate(SleepQualityFragmentDirections
                                .actionSleepQualityFragmentToSleepTrackerFragment())
                //change the state of the event var's state back indicating navigation is done
                viewModel.navigationDone()
            }
        })

        return binding.root
    }
}

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        // get a reference to the application context
        val application = requireNotNull(this.activity).application
        // get a reference to the DAO of the database
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        // create an instance of the viewModelFactory
        val sleepViewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        // get a reference to the SleepTrackerViewModel
        val sleepViewModel = ViewModelProvider(this, sleepViewModelFactory).get(SleepTrackerViewModel::class.java)

        // set the current activity as the lifecycle owner of the binding
        binding.lifecycleOwner = this
        // assign the viewModel-binding-variable to the relevant viewModel
        binding.sleepTrackerViewModel = sleepViewModel

        // Observe if a navigation is needed, ie. when the event var's state changes which occurs when the Stop button is clicked
        // Do the navigation and then change the state of the event var's state back indicating navigation is done
        sleepViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer {
            nightDone ->
            nightDone?.let {
                // We need to get the navController from this, because button is not ready, and it
                // just has to be a view. For some reason, this only matters if we hit stop again
                // after using the back button, not if we hit stop and choose a quality.
                // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                // popping the stack to get the correct behavior if we press stop multiple times
                // followed by back.
                // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections
                                .actionSleepTrackerFragmentToSleepQualityFragment(nightDone._nightId))
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                sleepViewModel.navigationDone()
            }
        })

        // Observe if presenting snack bar is needed, ie. when Clear button is clicked and notify the user they cleared the data
        sleepViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                sleepViewModel.doneShowingSnackbar()
        }
        })

        return binding.root
    }
}

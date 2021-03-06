package com.paulconsulting.pcgandroidinterview.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.paulconsulting.pcgandroidinterview.R;
import com.paulconsulting.pcgandroidinterview.adapters.FoundAuthorsRecyclerViewAdapter;
import com.paulconsulting.pcgandroidinterview.adapters.NetworkUtils;
import com.paulconsulting.pcgandroidinterview.background.GetAuthorsJSONDataWorker;
import com.paulconsulting.pcgandroidinterview.background.GetWorksJSONDataWorker;
import com.paulconsulting.pcgandroidinterview.data.Author;
import com.paulconsulting.pcgandroidinterview.data.AuthorViewModel;

import java.util.ArrayList;


/**
 * The Fragment that will be shown as the first destination when the user opens the app
 */
public class HomeFragment extends Fragment {

    /// Tags
    public static final String LOG_TAG = HomeFragment.class.getSimpleName();

    private static final String AUTHORS_FOUND_JSON_WORKER_TAG = "authorsFoundJsonWork";

    private static final String WORKS_FOUND_JSON_WORKER_TAG = "worksFoundJsonWork";

    /// Keys
    public static final String QUERY_PARAM_FIRST_NAME_KEY = "firstName";

    public static final String QUERY_PARAM_LAST_NAME_KEY = "lastName";

    public static final String QUERY_PARAM_SEARCH_KEY = "search";

    /// References

    /// TextViews
    private MaterialTextView authorTextView;

    // First Edit Text field for author first name
    private TextInputLayout authorFirstNameTextInputLayout;
    private TextInputEditText authorFirstNameTextInputEditText;

    // Second edit text field for author last name
    private TextInputLayout authorLastNameTextInputLayout;
    private TextInputEditText authorLastNameTextInputEditText;


    private MaterialTextView authorsFoundTextView;

    /// Button
    private MaterialButton searchMaterialButton;

    /// RecyclerView
    private RecyclerView recyclerView;
    private FoundAuthorsRecyclerViewAdapter adapter;

    /// Data: List of Authors
    private ArrayList<Author> data = new ArrayList<>();

    /// ViewModel
    public AuthorViewModel viewModel;





    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /// Initialize the data list with an empty list, so there is no null pointer exception when initializing the data
//        data = new ArrayList<>();


        /// Assign the Views

        authorTextView = getView().findViewById(R.id.author_text_view);


        authorFirstNameTextInputLayout = getView().findViewById(R.id.author_first_name_text_input_layout);

        authorFirstNameTextInputEditText = getView().findViewById(R.id.author_first_name_text_input_edit_text);


        authorLastNameTextInputLayout = getView().findViewById(R.id.author_last_name_text_input_layout);

        authorLastNameTextInputEditText = getView().findViewById(R.id.author_last_name_text_input_edit_text);


        searchMaterialButton = getView().findViewById(R.id.search_material_button);

        authorsFoundTextView = getView().findViewById(R.id.authors_found_text_view);

        recyclerView = getView().findViewById(R.id.authors_found_recycler_view);






        /// Setup the ViewModel

        // Use the Activity context to share the same ViewModel instance amongst multiple Fragments
        viewModel = ViewModelProviders.of(getActivity()).get(AuthorViewModel.class);


        // Get the data from the Viewmodel, which is an ArrayList of Authors
        data = viewModel.getFetchedData();

        Log.d(LOG_TAG, "Current ViewModel data: " + data);


        /// Setup the RecyclerView and Adapter

        adapter = new FoundAuthorsRecyclerViewAdapter(getContext(), data);

        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);



        searchMaterialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the first name entered

                String queryFirstName = authorFirstNameTextInputEditText.getText().toString();

                // Get the last name entered

                String queryLastName = authorLastNameTextInputEditText.getText().toString();







                /**
                 *
                 * Get the text entered in the edit text views
                 *
                 */

                if(queryFirstName.length() > 0 && queryLastName.length() > 0) {



                    Log.d(LOG_TAG, "First Name Entered: " + queryFirstName);



                    Log.d(LOG_TAG, "Last name entered: " + queryLastName);






                }

                else {

                    Toast.makeText(getContext(), "Please enter a First and Last Name", Toast.LENGTH_SHORT).show();

                    return;

                }




                /**
                 *
                 * Set the Authors Found Text View to visible
                 *
                 */



                authorsFoundTextView.setVisibility(View.VISIBLE);

                authorsFoundTextView.setText("Searching...");


                //// Create a Worker that will find the authors

                /// Create the Worker and give it parameters

                // Create a Data object that will hold the first name and last name entered by the user to be passed into the Worker to use to get the JSON String

                Data queryParams = new Data.Builder().putString(QUERY_PARAM_FIRST_NAME_KEY, queryFirstName).putString(QUERY_PARAM_LAST_NAME_KEY, queryLastName).build();

                // Create the authors Worker, setting the input data

                OneTimeWorkRequest foundAuthorsWorkRequest = new OneTimeWorkRequest.Builder(GetAuthorsJSONDataWorker.class).setInputData(queryParams).addTag(AUTHORS_FOUND_JSON_WORKER_TAG).build();



                WorkManager.getInstance(getContext()).enqueue(foundAuthorsWorkRequest);

                // Add an observer to the Worker to perform logic when the work is finished
                // TODO: Figure out exactly what this is doing and how LiveData is involved
                WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(foundAuthorsWorkRequest.getId()).observe(HomeFragment.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                            // Get the JSON String after the work has finished
                            String authorsJSONResponse = GetAuthorsJSONDataWorker.getAuthorsJSONResponse();



                            Log.d(LOG_TAG, "JSON author response received from Worker: " + authorsJSONResponse);

                            // Use that JSON String to pass it to the parseJSON method in NetworkUtils
                            // Will return an ArrayList of Authors



                            ArrayList<Author> foundAuthors = NetworkUtils.parseAuthorsJSON(authorsJSONResponse);

                            // Handle the case if there were no valid authors found (the ArrayList of Authors is empty)
                            if(foundAuthors.size() == 0) {

                                authorsFoundTextView.setText("No Author Found!");

                                Log.d(LOG_TAG, "No valid author found");

                                // Update the data in the adapter to clear the RecyclerView
                                // Update the data in the Adapter
                                data.clear();

                                data.addAll(foundAuthors);

                                adapter.notifyDataSetChanged();

                                // Exit the method
                                return;

                            }

                            // Set the textview to indicate an Author was found
                            authorsFoundTextView.setText("Found Author: ");

                            Log.d(LOG_TAG, "Found Authors ArrayList size: " + foundAuthors.size());

                            for(Author author: foundAuthors) {

                                Log.d(LOG_TAG, "Found Author: " + author.getAuthorfirst() + " " + author.getAuthorlast());

                            }

                            // TODO: Double check on this logic

                            // Update the data in the Adapter
                            data.clear();

                            data.addAll(foundAuthors);

                            adapter.notifyDataSetChanged();

                            // Update the data in the ViewModel
                            viewModel.setFetchedData(foundAuthors);


//                            // Make sure the data in the ViewModel was updated
                            Log.d(LOG_TAG, "Current data in Viewmodel: " + data.get(0).getAuthorfirst() + " " + data.get(0).getAuthorlast());

                            Log.d(LOG_TAG, "Current Author in ArrayList: " + data.get(0).getAuthorfirst() + " " + data.get(0).getAuthorlast() + " " + "Spotlight: " + data.get(0).getSpotlight() );


                            //// Create a Worker that will find works

                            /// Create a Data object that will hold the Author's first name and last name of the current Author in the ArrayList to be used as a search term

                            // Get the Author's first name of the Author in the current ArrayList
                            String currentAuthorFirst = data.get(0).getAuthorfirst();

                            Log.d(LOG_TAG, "Current Author First Name to be Used as Search Param: " + currentAuthorFirst);

                            // Get the Author's last name of the Author in the current ArrayList
                            String currentAuthorLast = data.get(0).getAuthorlast();

                            Log.d(LOG_TAG, "Current Author Last Name to be Used as Search Param: " + currentAuthorLast);

                            // Concatenate the names to create a full name with a space
                            String currentAuthorFull = currentAuthorFirst + " " + currentAuthorLast;

                            Log.d(LOG_TAG, "Current Author Full Name to be Used as Search Param: " + currentAuthorFirst + " " + currentAuthorLast);






//
                            // Add the last name to the Data
                            Data worksQueryParam = new Data.Builder().putString(QUERY_PARAM_SEARCH_KEY, currentAuthorFull).build();


                            /// Create the Worker

                            OneTimeWorkRequest findWorksWorkRequest = new OneTimeWorkRequest.Builder(GetWorksJSONDataWorker.class).setInputData(worksQueryParam).addTag(WORKS_FOUND_JSON_WORKER_TAG).build();

                            // Hand of the Worker to WorkManager
                            WorkManager.getInstance(getContext()).enqueue(findWorksWorkRequest);

                            // Add an Observer to perform logic after work is completed
                            WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(findWorksWorkRequest.getId()).observe(HomeFragment.this, new Observer<WorkInfo>() {
                                @Override
                                public void onChanged(WorkInfo workInfo) {

                                    if(workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                                        // Get the JSON String after the work has finished
                                        String worksJSONResponse = GetWorksJSONDataWorker.getWorksJSONResponse();

                                        Log.d(LOG_TAG, "JSON works response from Worker: " + worksJSONResponse);

                                        // Parse the JSON works response to get the titleweb
                                        ArrayList<String> foundWorks = NetworkUtils.parseWorksJSON(worksJSONResponse);

                                        Log.d(LOG_TAG, "Found Works ArrayList size: " + foundWorks.size());

                                        for (String work : foundWorks) {

                                            Log.d(LOG_TAG, "Found Work: " + work);


                                        }

                                        // Add the works to the foundAuthors ArrayList (to the Author object)

                                        for (Author author : foundAuthors) {

                                            author.setWorks(foundWorks);

                                            Log.d(LOG_TAG, "Author Works Added: " + author.getWorks() + " for author " + author.getAuthorfirst() + " " + author.getAuthorlast());

                                        }

                                        // Update the data in the Adapter and the ViewModel, so the Author objects include the work
                                        data.clear();

                                        data.addAll(foundAuthors);

                                        adapter.notifyDataSetChanged();

                                        viewModel.setFetchedData(foundAuthors);

                                        // Make sure all data is now in the ViewModel data
                                        Log.d(LOG_TAG, "ViewModel data: " + data.get(0).getAuthorfirst() + " " + data.get(0).getAuthorlast() + " " + "Spotlight: " + data.get(0).getSpotlight() + " " + "Works: " + data.get(0).getWorks());



                                    } // if workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED (works Worker)
//
//
                                } //onChanged works Worker
                            });



                        } // if workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED (authors Worker)
                    } // onChanged authors Worker
                });

            } // onClick
        });


    }

    public void resetData() {


        // Get the fetched data
        ArrayList<Author> fetchedData = viewModel.getFetchedData();

        if(fetchedData != null) {

            Log.d(LOG_TAG, "Fetched data: " + fetchedData);

            data.clear();

            data = fetchedData;


            // Add the fetched data to the data for the RecyclerView
            data.addAll(fetchedData);

            // Notify the adapter to be updated with new data
            adapter.notifyDataSetChanged();

        }

        else {

            Log.d(LOG_TAG, "Fetched Data is null!");

        }


    }

}

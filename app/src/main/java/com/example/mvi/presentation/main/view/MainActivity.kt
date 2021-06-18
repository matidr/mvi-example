package com.example.mvi.presentation.main.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvi.R
import com.example.mvi.data.api.ApiHelperImpl
import com.example.mvi.data.api.RetrofitBuilder
import com.example.mvi.data.model.User
import com.example.mvi.databinding.ActivityMainBinding
import com.example.mvi.presentation.main.adapter.MainAdapter
import com.example.mvi.presentation.main.intent.MainIntent
import com.example.mvi.presentation.main.viewmodel.MainViewModel
import com.example.mvi.presentation.main.viewstate.MainState
import com.example.mvi.util.BindActivity
import com.example.mvi.util.ViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private var adapter = MainAdapter(arrayListOf())
    private val binding: ActivityMainBinding by BindActivity(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        setupViewModel()
        observeViewModel()
        setupClicks()
    }

    private fun setupClicks() {
        binding.run {
            buttonFetchUser.setOnClickListener {
                lifecycleScope.launch {
                    mainViewModel.userIntent.send(MainIntent.FetchUser)
                }
            }
        }
    }

    private fun setupUI() {
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.run {
                addItemDecoration(
                    DividerItemDecoration(
                        recyclerView.context,
                        (recyclerView.layoutManager as LinearLayoutManager).orientation
                    )
                )
            }
            recyclerView.adapter = adapter
        }
    }

    private fun setupViewModel() {
        mainViewModel =
            ViewModelProviders.of(this, ViewModelFactory(ApiHelperImpl(RetrofitBuilder.apiService)))
                .get(MainViewModel::class.java)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            binding.run {
                mainViewModel.state.collect {
                    when (it) {
                        is MainState.Idle -> {
                        }

                        is MainState.Loading -> {
                            buttonFetchUser.visibility = View.GONE
                            progressBar.visibility = View.VISIBLE
                        }

                        is MainState.Users -> {
                            // show users list
                            progressBar.visibility = View.GONE
                            buttonFetchUser.visibility = View.GONE
                            renderList(it.users)
                        }

                        is MainState.Error -> {
                            progressBar.visibility = View.GONE
                            buttonFetchUser.visibility = View.VISIBLE
                            Toast.makeText(this@MainActivity, it.error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun renderList(users: List<User>) {
        binding.run {
            recyclerView.visibility = View.VISIBLE
            users.let { listOfUsers -> listOfUsers.let { adapter.addData(it) } }
            adapter.notifyDataSetChanged()
        }
    }
}
package com.example.facedetectionapp.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.facedetectionapp.presentation.adapter.ResultAdapter
import com.example.facedetectionapp.util.BaseFragment
import com.example.facedetectionapp.databinding.FragmentHomeBinding
import com.example.facedetectionapp.presentation.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    onInflate = FragmentHomeBinding ::inflate
) {
    private val homeViewModel by viewModels<HomeViewModel>()
    private val resultAdapter by lazy {
        ResultAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setResultAdapter()

        binding.btnStartCamera.setOnClickListener{
            permissionHandler()
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())

        }
    }

    private fun permissionHandler() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
        }
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun setResultAdapter() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireActivity())
        binding.recyclerView.adapter = resultAdapter
        homeViewModel.getResultData().observe(viewLifecycleOwner){
            resultAdapter.submitList(it)

    }

}



}
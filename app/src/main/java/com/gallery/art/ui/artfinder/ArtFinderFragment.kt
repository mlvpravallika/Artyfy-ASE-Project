package com.gallery.art.ui.artfinder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.gallery.art.R
import com.gallery.art.databinding.FragmentArtFinderUploadBinding
import com.gallery.art.preferences
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ArtFinderFragment : Fragment() {
    private var binding: FragmentArtFinderUploadBinding? = null
    private var loggedInUser = ""
    private var selectedImageUri : Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArtFinderUploadBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loggedInUser = preferences.getLoggedInUser(requireContext())

        binding?.imageView?.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding?.uploadArtButton?.setOnClickListener {
            showProgressBar(true)
            addArt(System.currentTimeMillis().toString())
        }
    }

    private fun addArt(id: String) {
        // Defining the child of storageReference
        val ref: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                "images/artFinder/$id"
            )
        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    Toast
                        .makeText(
                            requireContext(),
                            "Art has been uploaded ",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    showProgressBar(false)
                    navigateToFragment(ArtFinderViewListFragment.newInstance(it.toString()))
                }.addOnFailureListener {
                    showProgressBar(false)
                    Toast
                        .makeText(
                            requireContext(),
                            "Failed " + it.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    showProgressBar(false)
                }
            }.addOnFailureListener { e -> // Error, Image not uploaded
                showProgressBar(false)
                Toast
                    .makeText(
                        requireContext(),
                        "Failed " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
                showProgressBar(false)
            }

    }

    private fun showProgressBar(show: Boolean) {
        binding?.uploadArtButton?.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                selectedImageUri = data?.data!!
                // Use Uri object instead of File to avoid storage permissions
                binding?.imageView?.scaleType = ImageView.ScaleType.CENTER_CROP
                binding?.imageView?.setImageURI(selectedImageUri)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ArtFinderFragment()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Art finder"
    }

    private fun navigateToFragment(fragmentToNavigate: Fragment) {
        val fragmentTransaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragmentToNavigate)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.addToBackStack(fragmentToNavigate.toString())
        fragmentTransaction.commit()
    }
}

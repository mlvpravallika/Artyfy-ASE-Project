package com.gallery.art.ui.artfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import coil.load
import com.gallery.art.R
import com.gallery.art.databinding.FragmentArtFinderDetailBinding

class ArtFinderViewListFragment : Fragment() {
    private var binding: FragmentArtFinderDetailBinding? = null
    private var selectedImageUri : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedImageUri = it.getString(IMAGE_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentArtFinderDetailBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.imageView?.load(selectedImageUri){
            crossfade(true)
            placeholder(ContextCompat.getDrawable(requireContext(), R.drawable.img))
        }

        binding?.detail1?.text = "Detail 1"
        binding?.detail2?.text = "Detail 2"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        val IMAGE_URL = "imageUrl"

        @JvmStatic
        fun newInstance(imageUrl: String) = ArtFinderViewListFragment().apply {
            arguments = Bundle().apply {
                putString(IMAGE_URL, imageUrl)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Art Detail"
    }
}

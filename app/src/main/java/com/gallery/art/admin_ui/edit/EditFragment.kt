package com.gallery.art.admin_ui.edit

import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.gallery.art.GpsTracker
import com.gallery.art.admin_ui.manage.ManageFragment
import com.gallery.art.databinding.FragmentEditMuseumOrArtBinding
import com.gallery.art.models.Artist
import com.gallery.art.models.Arts
import com.gallery.art.preferences
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.gallery.art.models.Museums
import com.gallery.art.models.ViewMode
import com.gallery.art.ui.purchase.PurchaseActivity
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_edit_museum_or_art.*
import java.util.*


class EditFragment : Fragment() {
    private var editViewModel: EditViewModel? = null
    private var binding: FragmentEditMuseumOrArtBinding? = null
    private var loggedInUser = ""
    private var art : Arts? = null
    private var artId : String? = System.currentTimeMillis().toString()
    private var artist : Artist? = null
    private var editType = EditType.MUSEUM
    private val museumDatabaseReference = FirebaseDatabase.getInstance().reference.child("Museums")
    private val artistDatabaseReference = FirebaseDatabase.getInstance().reference.child("Artists")
    var selectedImageUri : Uri? = null
    private var viewMode = ViewMode.VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            art = it.getParcelable(ART_DETAIL)
            artist = it.getParcelable(ARTIST_DETAIL)
            editType = EditType.valueOf(it.getString(EDIT_TYPE)!!)
            viewMode = ViewMode.valueOf(it.getString(ManageFragment.VIEW_MODE)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        editViewModel = ViewModelProvider(this)[EditViewModel::class.java]
        binding = FragmentEditMuseumOrArtBinding.inflate(inflater, container, false)
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

        when (editType) {
            EditType.ART -> {
                //In case of art add or edit
                binding?.sellGroup?.visibility = View.VISIBLE
                binding?.priceEtLayout?.visibility = View.VISIBLE
                binding?.artistEtLayout?.visibility = View.VISIBLE
                binding?.descEtLayout?.visibility = View.GONE
                binding?.cityEtLayout?.visibility = View.GONE

                if (art != null) {
                    //Load data
                    binding?.imageView?.load(art?.artUrl) {
                        crossfade(true)
                    }
                    artId = art?.artId
                    binding?.museumNameEt?.setText(art?.artName)
                    binding?.sellSwitch?.isChecked = art?.sell!!
                    binding?.priceEt?.setText(art?.price)
                    binding?.artistEt?.setText(art?.artist)
                    selectedImageUri = Uri.parse(art?.artUrl)
                    binding?.deleteArt?.visibility = View.VISIBLE
                }

                if (viewMode == ViewMode.VIEW) makeEditingDisable()

                binding?.addArtButton?.setOnClickListener {
                    if (selectedImageUri == null){
                        Toast.makeText(requireContext(),"Please select an image",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (binding?.museumNameEt?.text?.isEmpty()!!) {
                        binding?.museumNameEtLayout?.error = "Please enter Art name"
                        return@setOnClickListener
                    }
                    if (binding?.artistEt?.text?.isEmpty()!!) {
                        binding?.artistEtLayout?.error = "Please enter Artist name"
                        return@setOnClickListener
                    }
                    if (binding?.priceEt?.text?.isEmpty()!!) {
                        binding?.priceEtLayout?.error = "Please enter Art Price"
                        return@setOnClickListener
                    }
                    showProgressBar(true)
                    if (art != null){
                        updateArtDetails(
                            art?.artId,
                            binding?.museumNameEt?.text!!.toString(),
                            binding?.priceEt?.text!!.toString(),
                            binding?.artistEt?.text!!.toString(),
                            binding?.sellSwitch?.isChecked,
                            art?.artUrl,
                            museumDatabaseReference
                        )
                    }else{
                        addArt(
                            artId!!,
                            binding?.museumNameEt?.text!!.toString(),
                            binding?.priceEt?.text!!.toString(),
                            binding?.artistEt?.text!!.toString(),
                            binding?.sellSwitch?.isChecked,
                            "museusm",
                            museumDatabaseReference
                        )
                    }
                }

                binding?.deleteArt?.setOnClickListener {
                    museumDatabaseReference.child(loggedInUser).child("arts").child(artId!!).removeValue()
                    requireActivity().supportFragmentManager.popBackStack()
                }

                binding?.buyArtButton?.setOnClickListener {
                    requireContext().startActivity(PurchaseActivity.buildIntent(requireContext(),art!!))
                }

            }
            EditType.ARTIST -> {
                // Add Artist
                binding?.sellGroup?.visibility = View.GONE
                binding?.priceEtLayout?.visibility = View.GONE
                binding?.artistEtLayout?.visibility = View.GONE
                binding?.descEtLayout?.visibility = View.GONE
                binding?.cityEtLayout?.visibility = View.VISIBLE

                binding?.museumNameEt?.doOnTextChanged { text, start, before, count ->
                    binding?.museumNameEtLayout?.error = null
                    binding?.museumNameEt?.error = null
                }
                binding?.cityEt?.doOnTextChanged { text, start, before, count ->
                    binding?.cityEtLayout?.error = null
                    binding?.cityEt?.error = null
                }

                binding?.addArtButton?.setOnClickListener {
                    if (selectedImageUri == null){
                        Toast.makeText(requireContext(),"Please select an image",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (binding?.museumNameEt?.text?.isEmpty()!!) {
                        binding?.museumNameEtLayout?.error = "Please enter Your name"
                        return@setOnClickListener
                    }
                    if (binding?.cityEt?.text?.isEmpty()!!) {
                        binding?.cityEtLayout?.error = "Please enter City"
                        return@setOnClickListener
                    }
                    showProgressBar(true)
                    addArtist(
                        binding?.museumNameEt?.text!!.toString(),
                        binding?.cityEt?.text!!.toString()
                    )
                }
            }
            EditType.ART_FOR_ARTIST -> {
                //In case of art add or edit
                binding?.sellGroup?.visibility = View.VISIBLE
                binding?.priceEtLayout?.visibility = View.VISIBLE
                binding?.artistEtLayout?.visibility = View.VISIBLE
                binding?.descEtLayout?.visibility = View.GONE
                binding?.cityEtLayout?.visibility = View.GONE
                binding?.artistEt?.setText(artist?.artistName)

                if (art != null) {
                    //Load data
                    binding?.imageView?.load(art?.artUrl) {
                        crossfade(true)
                    }
                    artId = art?.artId
                    binding?.museumNameEt?.setText(art?.artName)
                    binding?.sellSwitch?.isChecked = art?.sell!!
                    binding?.priceEt?.setText(art?.price)
                    binding?.artistEt?.setText(art?.artist)
                    selectedImageUri = Uri.parse(art?.artUrl)
                    binding?.deleteArt?.visibility = View.VISIBLE
                }

                if (viewMode == ViewMode.VIEW) makeEditingDisable()

                binding?.addArtButton?.setOnClickListener {
                    if (selectedImageUri == null){
                        Toast.makeText(requireContext(),"Please select an image",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (binding?.museumNameEt?.text?.isEmpty()!!) {
                        binding?.museumNameEtLayout?.error = "Please enter Art name"
                        return@setOnClickListener
                    }
                    if (binding?.artistEt?.text?.isEmpty()!!) {
                        binding?.artistEtLayout?.error = "Please enter Artist name"
                        return@setOnClickListener
                    }
                    if (binding?.priceEt?.text?.isEmpty()!!) {
                        binding?.priceEtLayout?.error = "Please enter Art Price"
                        return@setOnClickListener
                    }
                    showProgressBar(true)
                    if (art != null){
                        updateArtDetails(
                            art?.artId,
                            binding?.museumNameEt?.text!!.toString(),
                            binding?.priceEt?.text!!.toString(),
                            binding?.artistEt?.text!!.toString(),
                            binding?.sellSwitch?.isChecked,
                            art?.artUrl,
                            artistDatabaseReference
                        )
                    }else{
                        addArt(
                            artId!!,
                            binding?.museumNameEt?.text!!.toString(),
                            binding?.priceEt?.text!!.toString(),
                            binding?.artistEt?.text!!.toString(),
                            binding?.sellSwitch?.isChecked,
                            "artists",
                            artistDatabaseReference
                        )
                    }
                }

                binding?.deleteArt?.setOnClickListener {
                    artistDatabaseReference.child(loggedInUser).child("arts").child(artId!!).removeValue()
                    requireActivity().supportFragmentManager.popBackStack()
                }

                binding?.buyArtButton?.setOnClickListener {
                    requireContext().startActivity(PurchaseActivity.buildIntent(requireContext(),art!!))
                }

            }
            else -> {
                // Add Museum
                binding?.sellGroup?.visibility = View.GONE
                binding?.priceEtLayout?.visibility = View.GONE
                binding?.artistEtLayout?.visibility = View.GONE
                binding?.descEtLayout?.visibility = View.VISIBLE
                binding?.cityEtLayout?.visibility = View.VISIBLE

                binding?.museumNameEt?.doOnTextChanged { text, start, before, count ->
                    binding?.museumNameEtLayout?.error = null
                    binding?.museumNameEt?.error = null
                }
                binding?.descEt?.doOnTextChanged { text, start, before, count ->
                    binding?.descEt?.error = null
                    binding?.descEtLayout?.error = null
                }
                binding?.cityEt?.doOnTextChanged { text, start, before, count ->
                    binding?.cityEtLayout?.error = null
                    binding?.cityEt?.error = null
                }

                binding?.addArtButton?.setOnClickListener {
                    if (selectedImageUri == null){
                        Toast.makeText(requireContext(),"Please select an image",Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (binding?.museumNameEt?.text?.isEmpty()!!) {
                        binding?.museumNameEtLayout?.error = "Please enter museum name"
                        return@setOnClickListener
                    }
                    if (binding?.descEt?.text?.isEmpty()!!) {
                        binding?.descEtLayout?.error = "Please enter museum Description"
                        return@setOnClickListener
                    }

                    if (binding?.cityEt?.text?.isEmpty()!!) {
                        binding?.cityEtLayout?.error = "Please enter City"
                        return@setOnClickListener
                    }
                    showProgressBar(true)
                    addMuseum(
                        binding?.museumNameEt?.text!!.toString(),
                        binding?.descEt?.text!!.toString(),
                        binding?.cityEt?.text!!.toString()
                    )
                }
            }
        }

    }

    private fun makeEditingDisable() {
        binding?.museumNameEt?.isEnabled = false
        binding?.sellSwitch?.isEnabled = false
        binding?.priceEt?.isEnabled = false
        binding?.artistEt?.isEnabled = false
        binding?.progressCircular?.visibility = View.GONE
        binding?.deleteArt?.visibility = View.GONE
        binding?.addArtButton?.visibility = View.INVISIBLE
        if (art != null && art?.sell!!){
            binding?.buyArtButton?.visibility = View.VISIBLE
        }
    }

    private fun addArt(
        id: String,
        name: String,
        price: String,
        artistName: String,
        upForSale: Boolean?,
        path: String,
        museumDatabaseReference: DatabaseReference
    ) {
        // Defining the child of storageReference
        val ref: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                "images/$path/$loggedInUser/arts/$id"
            )
        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    updateArtDetails(id, name, price,artistName,upForSale,it.toString(),museumDatabaseReference)
                }.addOnFailureListener {
                    showProgressBar(false)
                    Toast
                        .makeText(
                            requireContext(),
                            "Failed " + it.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
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
            }

    }

    private fun updateArtDetails(
        id: String?,
        name: String,
        price: String,
        artistName: String,
        upForSale: Boolean?,
        artUrl: String?,
        databaseReference : DatabaseReference
    ) {
        val art = Arts(
            id!!,
            name,
            artUrl!!,
            price,
            artistName,
            upForSale!!
        )
        databaseReference.child(loggedInUser).child("arts").child(id).setValue(art)
        showProgressBar(false)
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun showProgressBar(show: Boolean) {
        binding?.addArtButton?.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun addMuseum(name: String, desc: String, city: String) {
        // Defining the child of storageReference
        val ref: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                "images/museums/$loggedInUser/$loggedInUser"
            )

        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    val museum = Museums(
                        name,
                        desc,
                        it.toString(),
                        HashMap<String, Arts>(),
                        city,
                        loggedInUser,
                        GpsTracker(requireActivity()).latitude,
                        GpsTracker(requireActivity()).longitude
                    )
                    museumDatabaseReference.child(loggedInUser).setValue(museum)
                    showProgressBar(false)
                    requireActivity().supportFragmentManager.popBackStack()
                }.addOnFailureListener {
                    showProgressBar(false)
                    Toast
                        .makeText(
                            requireContext(),
                            "Failed " + it.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
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
            }
    }

    private fun addArtist(name: String, city: String) {
        // Defining the child of storageReference
        val ref: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                "images/artists/$loggedInUser/$loggedInUser"
            )

        ref.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    val artist = Artist(
                        name,
                        it.toString(),
                        HashMap<String, Arts>(),
                        city,
                        loggedInUser
                    )
                    artistDatabaseReference.child(loggedInUser).setValue(artist)
                    showProgressBar(false)
                    requireActivity().supportFragmentManager.popBackStack()
                }.addOnFailureListener {
                    showProgressBar(false)
                    Toast
                        .makeText(
                            requireContext(),
                            "Failed " + it.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
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
            }
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
        const val ARTIST_DETAIL = "artist_details"
        const val ART_DETAIL = "art_details"
        const val MUSEUM_DETAIL = "museum_details"
        const val EDIT_TYPE = "edit_type"

        @JvmStatic
        fun addArtsToMuseum(editType: EditType, museums: Museums, viewMode: ViewMode) = EditFragment().apply {
            arguments = Bundle().apply {
                putString(EDIT_TYPE, editType.name)
                putParcelable(MUSEUM_DETAIL, museums)
                putString(ManageFragment.VIEW_MODE, viewMode.name)
            }
        }

        @JvmStatic
        fun addArtsToArtist(editType: EditType, artist: Artist, viewMode: ViewMode) = EditFragment().apply {
            arguments = Bundle().apply {
                putString(EDIT_TYPE, editType.name)
                putParcelable(ARTIST_DETAIL, artist)
                putString(ManageFragment.VIEW_MODE, viewMode.name)
            }
        }

        @JvmStatic
        fun newInstance(editType: EditType, viewMode: ViewMode) = EditFragment().apply {
            arguments = Bundle().apply {
                putString(EDIT_TYPE, editType.name)
                putString(ManageFragment.VIEW_MODE, viewMode.name)
            }
        }

        @JvmStatic
        fun loadInstance(art: Arts?, editType: EditType, viewMode: ViewMode) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ART_DETAIL, art)
                    putString(EDIT_TYPE, editType.name)
                    putString(ManageFragment.VIEW_MODE, viewMode.name)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = if(viewMode == ViewMode.VIEW){
            "View"
        }else "Edit"
    }
}
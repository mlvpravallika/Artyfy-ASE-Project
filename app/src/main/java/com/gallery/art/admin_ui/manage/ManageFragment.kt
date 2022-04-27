package com.gallery.art.admin_ui.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.gallery.art.R
import com.gallery.art.admin_ui.edit.EditFragment
import com.gallery.art.admin_ui.edit.EditType
import com.gallery.art.databinding.FragmentManageMuseumBinding
import com.gallery.art.models.Artist
import com.gallery.art.models.Arts
import com.gallery.art.models.Museums
import com.gallery.art.models.ViewMode
import com.gallery.art.preferences
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageFragment : Fragment(), ArtCollectionAdapter.ArtItemClickListener{
    private var manageViewModel: ManageViewModel? = null
    lateinit var binding: FragmentManageMuseumBinding
    private var loggedInUser = ""
    private var artCollectionAdapter : ArtCollectionAdapter? = null
    private val museumDatabaseReference = FirebaseDatabase.getInstance().reference.child("Museums")
    private var museums : Museums? = null
    private var viewMode = ViewMode.EDIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            museums = it.getParcelable(EditFragment.MUSEUM_DETAIL)
            viewMode = ViewMode.valueOf(it.getString(VIEW_MODE)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        manageViewModel = ViewModelProvider(this)[ManageViewModel::class.java]
        binding = FragmentManageMuseumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loggedInUser = preferences.getLoggedInUser(requireContext())
        if (museums != null && viewMode == ViewMode.VIEW){
            initMuseumView(museums!!)
        }else{
            initUI()
        }
    }

    private fun initMuseumView(museums: Museums) {
        userHasMuseum(museums)
    }

    private fun initUI() {
        museumDatabaseReference.child(loggedInUser)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue(Museums::class.java) != null){
                        museums = snapshot.getValue(Museums::class.java)
                        userHasMuseum(museums)
                    }else{
                        noMuseumFound()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    noMuseumFound()
                }
            })

        binding.registerMuseum.setOnClickListener {
            loadFragment(EditFragment.newInstance(EditType.MUSEUM, ViewMode.EDIT))
        }
    }

    private fun userHasMuseum(museums: Museums?) {
        binding.progressCircular.visibility = View.GONE
        binding.museumDetailLayout.visibility = View.VISIBLE
        binding.noMuseumGroup.visibility = View.GONE

        binding.imageView.load(museums?.museumImage) {
            crossfade(true)
        }
        binding.museumName.text = museums?.museumName
        binding.museumDesc.text = museums?.museumDesc
        binding.museumCity.text = museums?.city

        if (museums?.arts != null && museums.arts.isNotEmpty()){
            binding.artCollectionRv.visibility = View.VISIBLE
            binding.noArtTv.visibility = View.GONE

            val artList = museums.arts
            artCollectionAdapter = ArtCollectionAdapter(requireContext(),
                artList.values.toList() as MutableList<Arts>,
                this
            )
            binding.artCollectionRv.apply {
                layoutManager = GridLayoutManager(requireContext(),2)
                adapter = artCollectionAdapter
            }
        }else{
            binding.artCollectionRv.visibility = View.GONE
            binding.noArtTv.visibility = View.VISIBLE
        }

        if (viewMode == ViewMode.VIEW) {
            requireActivity().title = museums?.museumName
            binding.addArtButton.visibility = View.GONE
        }

        binding.addArtButton.setOnClickListener {
            loadFragment(EditFragment.addArtsToMuseum(EditType.ART, museums!!, ViewMode.EDIT))
        }
    }

    private fun noMuseumFound() {
        binding.progressCircular.visibility = View.GONE
        binding.museumDetailLayout.visibility = View.GONE
        binding.noMuseumGroup.visibility = View.VISIBLE
    }


    companion object {
        const val MUSEUM_DETAIL = "museum_details"
        const val VIEW_MODE = "view_mode"

        @JvmStatic
        fun newInstance(): ManageFragment {
            return ManageFragment()
        }

        @JvmStatic
        fun viewMuseumInstance(museums: Museums, viewMode: ViewMode) =
            ManageFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MUSEUM_DETAIL, museums)
                putString(VIEW_MODE, viewMode.name)
            }
        }

    }

    private fun loadFragment(fragment: Fragment?) {
        //switching fragment
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment!!)
            .addToBackStack(fragment.toString())
            .commit()
    }

    override fun onArtClick(arts: Arts) {
        loadFragment(EditFragment.loadInstance(arts,EditType.ART,viewMode))
    }

    override fun onResume() {
        super.onResume()
        if (viewMode != ViewMode.VIEW) {
            initUI()
            requireActivity().title = "Your Museum"
        }
    }
}
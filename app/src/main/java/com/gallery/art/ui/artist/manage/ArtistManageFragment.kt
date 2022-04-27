package com.gallery.art.ui.artist.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

class ArtistManageFragment : Fragment(), ArtistArtCollectionAdapter.ArtItemClickListener{
    lateinit var binding: FragmentManageMuseumBinding
    private var loggedInUser = ""
    private var artCollectionAdapter : ArtistArtCollectionAdapter? = null
    private val artistDatabaseReference = FirebaseDatabase.getInstance().reference.child("Artists")
    private var viewMode = ViewMode.EDIT
    private var artist : Artist? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            artist = it.getParcelable(ARTIST_DETAIL)
            viewMode = ViewMode.valueOf(it.getString(VIEW_MODE)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageMuseumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loggedInUser = preferences.getLoggedInUser(requireContext())
        if (artist != null && viewMode == ViewMode.VIEW){
            initArtistView(artist!!)
        }else{
            initUI()
        }
    }

    private fun initArtistView(artist: Artist) {
        userHasArtistAccount(artist)
    }

    private fun initUI() {
        artistDatabaseReference.child(loggedInUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue(Museums::class.java) != null){
                        artist = snapshot.getValue(Artist::class.java)
                        userHasArtistAccount(artist)
                    }else{
                        noArtistFound()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    noArtistFound()
                }
            })

        binding.registerMuseum.setOnClickListener {
            loadFragment(EditFragment.newInstance(EditType.ARTIST, ViewMode.EDIT))
        }
    }

    private fun userHasArtistAccount(artist: Artist?) {
        binding.progressCircular.visibility = View.GONE
        binding.museumDetailLayout.visibility = View.VISIBLE
        binding.noMuseumGroup.visibility = View.GONE
        binding.noArtTv.text = "Your Art collection is empty!"


        binding.imageView.load(artist?.artistImage) {
            crossfade(true)
        }
        binding.museumName.text = artist?.artistName
        binding.museumDesc.visibility = View.GONE
        binding.museumCity.text = artist?.city

        if (artist?.arts != null && artist.arts.isNotEmpty()){
            binding.artCollectionRv.visibility = View.VISIBLE
            binding.noArtTv.visibility = View.GONE

            val artList = artist.arts
            artCollectionAdapter = ArtistArtCollectionAdapter(requireContext(),
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
            requireActivity().title = artist?.artistName
            binding.addArtButton.visibility = View.GONE
        }

        binding.addArtButton.setOnClickListener {
            loadFragment(EditFragment.addArtsToArtist(EditType.ART_FOR_ARTIST, artist!!, ViewMode.EDIT))
        }
    }

    private fun noArtistFound() {
        binding.progressCircular.visibility = View.GONE
        binding.museumDetailLayout.visibility = View.GONE
        binding.desc.text = "Looks like your artist account isn't registered with us yet!"
        binding.noMuseumGroup.visibility = View.VISIBLE
    }


    companion object {
        const val ARTIST_DETAIL = "artist_details"
        const val VIEW_MODE = "view_mode"

        @JvmStatic
        fun newInstance(): ArtistManageFragment {
            return ArtistManageFragment()
        }

        @JvmStatic
        fun viewArtistInstance(artist: Artist, viewMode: ViewMode) =
            ArtistManageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARTIST_DETAIL, artist)
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
        loadFragment(EditFragment.loadInstance(arts,EditType.ART_FOR_ARTIST,viewMode))
    }

    override fun onResume() {
        super.onResume()
        if (viewMode != ViewMode.VIEW) {
            initUI()
            requireActivity().title = "Your Artist Account"
        }
    }
}
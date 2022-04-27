package com.gallery.art.ui.artist

import com.google.firebase.database.FirebaseDatabase
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.gallery.art.preferences
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.recyclerview.widget.LinearLayoutManager
import com.gallery.art.R
import com.gallery.art.databinding.GalleryRecyclerViewBinding
import com.gallery.art.models.Artist
import com.gallery.art.models.ViewMode
import com.gallery.art.ui.artist.manage.ArtistManageFragment.Companion.viewArtistInstance
import java.util.ArrayList

class ArtistListFragment : Fragment(), ArtistItemClickListener {
    private var binding: GalleryRecyclerViewBinding? = null
    private val myListData: MutableList<Artist?> = ArrayList()
    private val artistDb = FirebaseDatabase.getInstance().reference.child("Artists")
    private var loggedInUser = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = GalleryRecyclerViewBinding.inflate(
            inflater,
            container,
            false
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loggedInUser = preferences.getLoggedInUser(requireContext())
        myListData.clear()
        init()
    }

    private fun init() {
        artistDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    if (!ds.key.equals(loggedInUser, ignoreCase = true)) {
                        myListData.add(ds.getValue(Artist::class.java))
                    }
                }
                loadRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadRecyclerView() {
        val adapter = ArtistListAdapter(myListData, this)
        binding!!.root.layoutManager = LinearLayoutManager(requireContext())
        binding!!.root.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Artists"
    }

    private fun navigateToFragment(fragmentToNavigate: Fragment) {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragmentToNavigate)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.addToBackStack(fragmentToNavigate.toString())
        fragmentTransaction.commit()
    }

    override fun onArtistClick(artist: Artist) {
        navigateToFragment(viewArtistInstance(artist, ViewMode.VIEW))
    }
}
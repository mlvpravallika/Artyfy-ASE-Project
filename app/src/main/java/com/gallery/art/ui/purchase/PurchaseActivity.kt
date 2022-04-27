package com.gallery.art.ui.purchase

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.gallery.art.R
import com.gallery.art.databinding.ActivityPurchaseBinding
import com.gallery.art.models.Arts

class PurchaseActivity : AppCompatActivity() {
    private var binding: ActivityPurchaseBinding? = null
    private var arts: Arts? = null

    companion object{
        const val ART_FOR_REVIEW = "artForReview"

        fun buildIntent(context: Context, arts: Arts) : Intent {
            val intent = Intent(context, PurchaseActivity::class.java)
            intent.putExtra(ART_FOR_REVIEW,arts)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        this.title = "Review your purchase"

        if (intent.hasExtra(ART_FOR_REVIEW)){
            arts = intent.getParcelableExtra(ART_FOR_REVIEW)
        }

        if (arts != null){
            initReviewUI(arts!!)
        }

    }

    private fun initReviewUI(arts: Arts) {
        binding?.reviewLayout?.visibility = View.VISIBLE
        binding?.successPurchaseLayout?.visibility = View.GONE

        binding?.imageView?.load(arts.artUrl)
        binding?.artName?.text = arts.artName
        binding?.artistName?.text = arts.artist
        binding?.amountTv?.text = arts.price

        binding?.purchaseSuccessIv?.load(ContextCompat.getDrawable(this,R.drawable.success))

        binding?.confirmButton?.setOnClickListener {
            binding?.reviewLayout?.visibility = View.GONE
            binding?.successPurchaseLayout?.visibility = View.VISIBLE
            this.title = "Success"
        }

        binding?.backButton?.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }
}
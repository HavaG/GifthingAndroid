package hu.bme.aut.android.gifthing.ui.gift.reserved

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.android.gifthing.AppPreferences
import hu.bme.aut.android.gifthing.R
import hu.bme.aut.android.gifthing.database.models.entities.Gift
import hu.bme.aut.android.gifthing.database.models.entities.GiftWithOwner
import hu.bme.aut.android.gifthing.database.viewModels.GiftViewModel
import kotlinx.android.synthetic.main.activity_gift_details.*
import kotlinx.android.synthetic.main.gift_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@AndroidEntryPoint
class ReservedGiftDetailsActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val mGiftViewModel: GiftViewModel by viewModels()
    private lateinit var currentGift: Gift

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift_details)

        val giftId = intent.getLongExtra("GIFT_ID", 0)

        btnReserve.setOnClickListener {

            val dialogClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            onReserveOrFree()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure You want to free/reserve this gift?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show()
        }


        mGiftViewModel.getByIdWithOwner(giftId).observe(
            this,
            Observer<GiftWithOwner> { giftWithOwner ->
                try {
                    currentGift = giftWithOwner.gift
                    tvOwnerName.text =
                        if (giftWithOwner.owner.firstName != "" && giftWithOwner.owner.lastName != "") {
                            "${giftWithOwner.owner.firstName} ${giftWithOwner.owner.lastName}"
                        } else {
                            giftWithOwner.owner.username
                        }

                    tvGiftName.text = giftWithOwner.gift.name
                    giftWithOwner.gift.price?.let { tvGiftPrice.text = it.toString() } ?: run { priceLL.visibility = View.GONE }
                    giftWithOwner.gift.description?.let { tvGiftDescription.text = it } ?: run { descriptionLL.visibility = View.GONE }
                    giftWithOwner.gift.link?.let { tvGiftLink.text = it } ?: run { linkLL.visibility = View.GONE }

                    giftWithOwner.gift.reservedBy?.let {
                        if (it == AppPreferences.currentServerId!!) {
                            btnReserve.text = resources.getString(R.string.free)
                            tvReservedBy.text = getString(R.string.you)
                        } else {
                            tvReservedBy.text = ""
                        }
                    } ?: run {
                        tvReservedBy.text = resources.getString(R.string.currently_free)
                        btnReserve.text = resources.getString(R.string.reserve)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gift with owner not found.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun onReserveOrFree() {
        currentGift.reservedBy?.let {
            if (it == AppPreferences.currentServerId!!) {
                currentGift.reservedBy = null
                mGiftViewModel.release(currentGift).observe(
                    this,
                    Observer<Boolean> { success ->
                        if(success)
                            Toast.makeText(baseContext, "Freed successfully", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(baseContext, "Something went wrong, try again later", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } ?: run {
            currentGift.reservedBy = AppPreferences.currentServerId!!
            mGiftViewModel.reserve(currentGift).observe(
                this,
                Observer<Boolean> { success ->
                    if(success)
                        Toast.makeText(baseContext, "Reserved successfully", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(baseContext, "Something went wrong, try again later", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
package hu.bme.aut.android.gifthing.ui.gift

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import hu.bme.aut.android.gifthing.ErrorActivity
import hu.bme.aut.android.gifthing.Services.GiftService
import hu.bme.aut.android.gifthing.Services.ServiceBuilder
import hu.bme.aut.android.gifthing.models.Gift
import hu.bme.aut.android.gifthing.ui.gift.myGifts.MyGiftsActivity
import kotlinx.android.synthetic.main.activity_gift_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import hu.bme.aut.android.gifthing.R

class GiftDetailsActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift_details)

        val giftId = intent.getLongExtra("GIFT_ID", 0)

        launch {
            val currentGift = getGift(giftId)
            if (currentGift != null) {
                tvGiftName.text = currentGift.name
                tvGiftPrice.text = currentGift.price.toString()
                tvGiftLink.text = currentGift.link
                tvGiftDescription.text = currentGift.description
            } else {
                val intent = Intent(this@GiftDetailsActivity, ErrorActivity::class.java).apply {
                    putExtra("ERROR_MESSAGE", "Na itt valami komoly baj van (0 id gift-et akart elkérni)")
                }
                startActivity(intent)
            }
        }

        btnDelete.setOnClickListener{
            onDelete(giftId)
        }

        btnEdit.setOnClickListener{
            Toast.makeText(baseContext, "This method is not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDelete(giftId: Long) {
        launch {
            val success = deleteGift(giftId)
            if (success) {
                val intent = Intent(this@GiftDetailsActivity, MyGiftsActivity::class.java).apply {}
                Toast.makeText(this@GiftDetailsActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@GiftDetailsActivity, ErrorActivity::class.java).apply {
                    putExtra("ERROR_MESSAGE", "We could not delete this gift (wtf ??? )")
                }
                startActivity(intent)
            }
        }
    }

    private suspend fun getGift(id: Long) : Gift? {
        val giftService = ServiceBuilder.buildService(GiftService::class.java)
        return giftService.getById(id)
    }

    private suspend fun deleteGift(id: Long) : Boolean {
        val giftService = ServiceBuilder.buildService(GiftService::class.java)
        return giftService.deleteById(id)
    }
}
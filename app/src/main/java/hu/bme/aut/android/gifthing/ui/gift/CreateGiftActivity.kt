package hu.bme.aut.android.gifthing.ui.gift

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import hu.bme.aut.android.gifthing.services.GiftService
import hu.bme.aut.android.gifthing.services.ServiceBuilder
import hu.bme.aut.android.gifthing.models.Gift
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.android.gifthing.ErrorActivity
import hu.bme.aut.android.gifthing.services.AppPreferences
import hu.bme.aut.android.gifthing.ui.home.HomeActivity
import kotlinx.android.synthetic.main.dialog_create_gift.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception


class CreateGiftActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(hu.bme.aut.android.gifthing.R.layout.dialog_create_gift)
        setFinishOnTouchOutside(false)
        showSoftKeyboard(etGiftName)

        btnCreate.setOnClickListener {
            if (etGiftName.text.toString() == "") {
                val intent = Intent(this, ErrorActivity::class.java).apply {
                    putExtra("ERROR_MESSAGE", "Name is required")
                }
                startActivity(intent)
                return@setOnClickListener
            }
            var link: String? = null
            if (etGiftLink.text.toString() != "") {
                link = etGiftLink.text.toString()
                /*TODO: link is valid or not
                if (!URLUtil.isValidUrl(etGiftLink.text.toString())) {
                    val intent = Intent(this, ErrorActivity::class.java).apply {
                        putExtra("ERROR_MESSAGE", "Add a valid link (or leave it empty)")
                    }
                    startActivity(intent)
                    return@setOnClickListener
                } else {
                    link = etGiftLink.text.toString()
                }*/
            }

            val newGift = Gift()

            newGift.name = etGiftName.text.toString()
            if (etGiftPrice.text.toString() != "") {
                newGift.price = Integer.parseInt(etGiftPrice.text.toString())
            }

            if (etGiftDescription.text.toString() != "") {
                newGift.description = etGiftDescription.text.toString()
            }

            if (link != null) {
                newGift.link = link
            }

            launch {
                try {
                    val currentUserId = AppPreferences.currentId
                    if(currentUserId == 0L) {
                        throw Exception("User not logged in")
                    }
                    newGift.owner = currentUserId
                    val savedGift = createGift(newGift)

                    val result = Intent().apply {
                        putExtra("GIFT", savedGift)
                    }
                    setResult(Activity.RESULT_OK, result)
                } catch (e: HttpException) {
                    val intent = Intent(this@CreateGiftActivity, ErrorActivity::class.java).apply {
                        putExtra("ERROR_MESSAGE", "Something went wrong")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(this@CreateGiftActivity, ErrorActivity::class.java).apply {
                        putExtra("ERROR_MESSAGE", e.message)
                    }
                    startActivity(intent)
                }


                finish()
            }
        }

        btnCancel.setOnClickListener{
            setResult(Activity.RESULT_CANCELED) //TODO: ezt Ok-nak veszi át a mygiftfragment
            finish()
        }
    }

    private suspend fun createGift(newGift: Gift) : Gift {
        val giftService = ServiceBuilder.buildService(GiftService::class.java)
        return giftService.create(newGift)
    }

    private fun showSoftKeyboard(view: View){
        if(view.requestFocus()){
            val imm = getSystemService((Context.INPUT_METHOD_SERVICE)) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
        }
    }
}

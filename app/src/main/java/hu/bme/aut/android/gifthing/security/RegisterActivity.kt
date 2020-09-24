package hu.bme.aut.android.gifthing.security

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hu.bme.aut.android.gifthing.ErrorActivity
import hu.bme.aut.android.gifthing.ui.home.HomeActivity
import hu.bme.aut.android.gifthing.R
import hu.bme.aut.android.gifthing.services.ServiceBuilder
import hu.bme.aut.android.gifthing.services.UserService
import hu.bme.aut.android.gifthing.models.User
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.*
import retrofit2.HttpException

class RegisterActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.+[a-z]+"

        okBtn.setOnClickListener {
            //TODO: if not correct pwd or email is exists
             if( etEmail.text.toString() == "" ||
                 etPassword.text.toString()== "" ||
                 etPasswordAgain.text.toString()== "" ||
                 etFirstName.text.toString()== "" ||
                 etLastName.text.toString()== "") {
                val intent = Intent(this, ErrorActivity::class.java).apply {
                    putExtra( "ERROR_MESSAGE","Fill every required field")
                }
                startActivity(intent)
            } else if (etPassword.text.toString() != etPasswordAgain.text.toString()) {
            val intent = Intent(this, ErrorActivity::class.java).apply {
                putExtra( "ERROR_MESSAGE","The passwords are not matching")
            }
            startActivity(intent)
        } else if(!etEmail.text.toString().trim().matches(emailPattern.toRegex())) {
                 val intent = Intent(this, ErrorActivity::class.java).apply {
                 putExtra( "ERROR_MESSAGE","Enter a valid email address")
             }
                 startActivity(intent)
        } else {
                val newUser = User(etEmail.text.toString(), etFirstName.text.toString(), etLastName.text.toString())
                 if(etNickname.text.toString() == "") {
                     newUser.nickName = null
                 } else {
                     newUser.nickName = etNickname.text.toString()
                 }
                newUser.password = etPassword.text.toString()

                 //save
                 launch {
                     val createdUser: User?
                     try {
                         createdUser = saveUser(newUser)
                         val intent = Intent(this@RegisterActivity, HomeActivity::class.java).apply {
                             //TODO: flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                             putExtra("USER_ID", createdUser.id)
                         }
                         startActivity(intent)
                     } catch (e: HttpException) {
                         if(e.code() == 409) {
                             val intent = Intent(this@RegisterActivity, ErrorActivity::class.java).apply {
                                 putExtra( "ERROR_MESSAGE","Email is in use")
                             }
                             startActivity(intent)
                         } else {
                             val intent = Intent(this@RegisterActivity, ErrorActivity::class.java).apply {
                                 putExtra( "ERROR_MESSAGE","Something went wrong")
                             }
                             startActivity(intent)
                         }
                     }
                 }
            }
        }

        cancelBtn.setOnClickListener{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private suspend fun saveUser(user: User) : User {
        val userService = ServiceBuilder.buildService(UserService::class.java)
        return userService.create(user)
    }
}
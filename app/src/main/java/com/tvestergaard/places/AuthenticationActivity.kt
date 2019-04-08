package com.tvestergaard.places

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_authentication.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.toast

// https://developers.google.com/identity/sign-in/android/start-integrating
// https://developers.google.com/identity/sign-in/android/sign-in

const val AUTHENTICATION_REQUEST_CODE = 1

class AuthenticationActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        authenticateButton.onClick {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, AUTHENTICATION_REQUEST_CODE)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AUTHENTICATION_REQUEST_CODE -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    private fun handleSignInResult(authenticationAttempt: Task<GoogleSignInAccount>) {
        try {
            val account = authenticationAttempt.getResult(ApiException::class.java)
            val result = Intent()
            result.putExtra("account", account)
            setResult(2, result)
            finish()
        } catch (e: ApiException) {
            toast("You could not be authenticated.")
            debug(e)
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true) // Prevent that the user can go back without successful authentication
    }
}

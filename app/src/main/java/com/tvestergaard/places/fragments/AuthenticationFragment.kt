package com.tvestergaard.places.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.tvestergaard.places.MainActivity

import com.tvestergaard.places.R
import com.tvestergaard.places.runOnUiThread
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_authentication.*
import org.jetbrains.anko.doAsync
import java.io.Serializable

data class AuthenticatedUser(
    var id: Int,
    var googleId: String,
    var name: String,
    var email: String,
    var picture: String,
    var locale: String,
    var token: String
) : Serializable

class AuthenticationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_authentication, container, false)
    }

    override fun onStart() {
        super.onStart()

        doAsync {
            val lastSignIn = GoogleSignIn.getLastSignedInAccount(activity)

            if (lastSignIn != null) {
                val account = BackendCommunicator().authenticateWithBackend(lastSignIn.idToken)
                if (account != null) {
                    authenticationComplete(account)
                }
            }
        }

        authenticateButton.setOnClickListener {
            val signInIntent = (activity as MainActivity).googleAuthClient.signInIntent
            startActivityForResult(signInIntent, authenticationRequestCode)
        }
    }

    private fun authenticationComplete(user: AuthenticatedUser) {
        runOnUiThread {
            (activity as MainActivity).onAuthenticationSuccess(user)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            authenticationRequestCode -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    private fun handleSignInResult(authenticationAttempt: Task<GoogleSignInAccount>) {
        try {
            val account = authenticationAttempt.getResult(ApiException::class.java)
            doAsync {
                val backendAuthenticationUser = BackendCommunicator().authenticateWithBackend(account?.idToken)
                BackendCommunicator.authenticatedUser = backendAuthenticationUser
                authenticationComplete(backendAuthenticationUser!!)
            }
        } catch (e: ApiException) {
            error("Could not authenticate with error code ${e.statusCode}")
        }
    }

    companion object {
        const val authenticationRequestCode = 1
        @JvmStatic
        fun newInstance() =
            AuthenticationFragment().apply {
                arguments = Bundle()
            }
    }
}

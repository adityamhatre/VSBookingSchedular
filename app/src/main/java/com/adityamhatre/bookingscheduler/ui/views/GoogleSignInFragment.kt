package com.adityamhatre.bookingscheduler.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.calendar.CalendarScopes
import kotlin.random.Random


class GoogleSignInFragment : Fragment() {
    private val TAG = "GoogleSignInFragment"
    private val SIGN_IN_INTENT_RC: Int = Random.nextInt()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity!!, gso);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_sign_in_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (GoogleSignIn.getLastSignedInAccount(activity) == null) {
            view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
                startActivityForResult(googleSignInClient.signInIntent, SIGN_IN_INTENT_RC)
            }
        } else {
            view.findViewById<SignInButton>(R.id.sign_in_button).visibility = View.GONE
            onSuccessLogin(GoogleSignIn.getLastSignedInAccount(activity))
        }

    }

    private fun onSuccessLogin(account: GoogleSignInAccount?) {
        if (account == null) {
            Toast.makeText(activity, "Some error occurred", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Returning because account = null")
            return
        }
        Application.account = account.account!!
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_INTENT_RC) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            onSuccessLogin(account)
        } catch (e: ApiException) {
            Log.e(TAG, e.statusCode.toString())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = GoogleSignInFragment()
    }
}
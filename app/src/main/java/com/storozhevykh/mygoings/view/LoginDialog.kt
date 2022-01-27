package com.storozhevykh.mygoings.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.databinding.DialogLoginBinding
import com.storozhevykh.mygoings.firebase.SyncHelper
import com.storozhevykh.mygoings.model.StaticStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class LoginDialog : DialogFragment(), View.OnClickListener {

    @Inject
    lateinit var syncHelper: SyncHelper

    lateinit var auth: FirebaseAuth
    lateinit var launcher: ActivityResultLauncher<Intent>

    lateinit var binding: DialogLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        App.component.inject(this)
        binding = DialogLoginBinding.inflate(layoutInflater)
        with(binding) {
            signInButton.setOnClickListener(this@LoginDialog)
            signUpButton.setOnClickListener(this@LoginDialog)
            btnGoogle.setOnClickListener(this@LoginDialog)
            btnFacebook.setOnClickListener(this@LoginDialog)
            btnTwitter.setOnClickListener(this@LoginDialog)
            btnVk.setOnClickListener(this@LoginDialog)
            btnOk.setOnClickListener(this@LoginDialog)
        }

        auth = Firebase.auth

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        if (binding.loginEmailEdit.text.isNotEmpty() && binding.loginPasswordEdit.text.isNotEmpty()) {
            val email = binding.loginEmailEdit.text.toString()
            val password = binding.loginPasswordEdit.text.toString()
            when (v?.id) {
                R.id.signInButton -> {
                    auth.signInWithEmailAndPassword(
                        binding.loginEmailEdit.text.toString(),
                        binding.loginPasswordEdit.text.toString()
                    )
                        .addOnCompleteListener(requireActivity()) {
                            if (it.isSuccessful) success()
                            else errorHandler(it)
                        }
                }
                R.id.signUpButton -> {
                    if (password.length < 6)
                        binding.loginPasswordEdit.error =
                            "password must contain at least 6 characters"
                    else {
                        auth.createUserWithEmailAndPassword(
                            binding.loginEmailEdit.text.toString(),
                            binding.loginPasswordEdit.text.toString()
                        )
                            .addOnCompleteListener(requireActivity()) {
                                if (it.isSuccessful) success()
                                else errorHandler(it)
                            }
                    }
                }
            }
        }

        when (v?.id) {
            R.id.btn_google -> googleSignIn()
            R.id.btn_facebook -> binding.facebookLoginBtn.performClick()
            R.id.facebookLoginBtn -> facebookSignIn()
            R.id.btn_vk -> Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
            R.id.btn_ok -> Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
            R.id.btn_twitter -> Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun googleSignIn() {
        println("try to login")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        launcher.launch(googleSignInClient.signInIntent)
    }

    private fun authWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) success()
            else Toast.makeText(requireContext(), "Registration error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun facebookSignIn() {
        println("Facebook login")
        val callbackManager = CallbackManager.Factory.create()
        binding.facebookLoginBtn.setPermissions("public_profile", "email")
        // Initialize Facebook Login button
        binding.facebookLoginBtn.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) {
                if (it.isSuccessful) success()
                else Toast.makeText(requireContext(), "Registration error", Toast.LENGTH_SHORT).show()
            }
    }

    private fun success() {
        (requireActivity() as MainActivity).navHeaderUpdate()
        StaticStorage.userEmail = Firebase.auth.currentUser?.email!!.replace(".", "")
        CoroutineScope(Dispatchers.IO).launch {
            val goingsNeedSync = syncHelper.compareLocalWithCloud()
            /*if (goingsNeedSync > 0)
            SyncDialog().show(requireActivity().supportFragmentManager, "TAG")*/
            dismiss()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    private fun errorHandler(task: Task<AuthResult>) {
        val errorCode = (task.getException() as FirebaseAuthException).errorCode
        when (errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN" -> showToast("The custom token format is incorrect. Please check the documentation.")
            "ERROR_CUSTOM_TOKEN_MISMATCH" -> showToast("The custom token corresponds to a different audience.")
            "ERROR_INVALID_CREDENTIAL" -> showToast("The supplied auth credential is malformed or has expired.")
            "ERROR_INVALID_EMAIL" -> {
                showToast("The email address is badly formatted.")
                binding.loginEmailEdit.error = "The email address is badly formatted"
                binding.loginEmailEdit.requestFocus()
            }
            "ERROR_WRONG_PASSWORD" -> {
                showToast("The password is invalid or the user does not have a password.")
                binding.loginPasswordEdit.error = "password is incorrect"
                binding.loginPasswordEdit.requestFocus()
                binding.loginPasswordEdit.setText("")
            }
            "ERROR_USER_MISMATCH" -> showToast("The supplied credentials do not correspond to the previously signed in user.")
            "ERROR_REQUIRES_RECENT_LOGIN" -> showToast("This operation is sensitive and requires recent authentication. Log in again before retrying this request.")
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> showToast("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.")
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                showToast("The email address is already in use by another account.")
                binding.loginEmailEdit.error =
                    "The email address is already in use by another account"
                binding.loginEmailEdit.requestFocus()
            }
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> showToast("This credential is already associated with a different user account.")
            "ERROR_USER_DISABLED" -> showToast("The user account has been disabled by an administrator.")
            "ERROR_USER_TOKEN_EXPIRED" -> showToast("The user\\\\'s credential is no longer valid. The user must sign in again.")
            "ERROR_USER_NOT_FOUND" -> showToast("There is no user record corresponding to this identifier. The user may have been deleted.")
            "ERROR_INVALID_USER_TOKEN" -> showToast("The user\\\\'s credential is no longer valid. The user must sign in again.")
            "ERROR_OPERATION_NOT_ALLOWED" -> showToast("This operation is not allowed. You must enable this service in the console.")
            "ERROR_WEAK_PASSWORD" -> {
                showToast("The given password is invalid.")
                binding.loginPasswordEdit.error =
                    "The password is invalid it must 6 characters at least"
                binding.loginPasswordEdit.requestFocus();
            }
        }
    }
}
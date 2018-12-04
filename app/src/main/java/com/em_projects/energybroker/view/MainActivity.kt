package com.em_projects.energybroker.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.em_projects.energybroker.utils.AppUtils
import android.widget.TextView
import android.widget.Toast
import com.em_projects.energybroker.R
import com.em_projects.energybroker.viewmodel.SignInViewModel


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"

    // UI Components
    lateinit var loginUserName: EditText
    lateinit var loginPassword: EditText

    lateinit var loginSignInButton: Button
    lateinit var loginRegisterButton: Button

    // Helpers
    private lateinit var context: Context

    // Connection
    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        // Bind UI Component
        loginUserName = findViewById(R.id.loginUserName)
        loginPassword = findViewById(R.id.loginPassword)
        loginSignInButton = findViewById(R.id.loginSignInButton)
        loginRegisterButton = findViewById(R.id.loginRegisterButton)

        loginSignInButton.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View?) {
                var usr = loginUserName.text.toString()
                var pwd = loginPassword.text.toString()
                if (!usr.isEmpty() && pwd.isEmpty())  {
                    // TODO
                } else {
                    Toast.makeText(this@MainActivity,
                        R.string.login_missing_credentials, Toast.LENGTH_LONG).show()
                }
            }

        })

        // Handle ime option Send Action
        loginPassword.setOnEditorActionListener(object : TextView.OnEditorActionListener {

            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    loginSignInButton.performClick();
                    return true;
                }
                return false;
            }
        })

        // Version name and number
        val versionNumber = findViewById<TextView>(R.id.version_number)
        versionNumber.text = getString(
            R.string.app_version_format,
            AppUtils.getAppVersion(this), AppUtils.getAppVerionCode(this)
        )

        // Init the ViewModel for ext and int communication
        viewModel = ViewModelProviders.of(this).get(SignInViewModel::class.java)
    }


}

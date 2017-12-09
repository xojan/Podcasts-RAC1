package cat.xojan.random1.feature.splash

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cat.xojan.random1.feature.home.HomeActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
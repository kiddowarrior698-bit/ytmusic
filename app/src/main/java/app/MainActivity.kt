package app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = getString(R.string.user_name)
        setContentView(TextView(this).apply {
            text = getString(R.string.greeting, user) + "\n" + getString(R.string.skeleton_status)
            textSize = 20f
            setPadding(48, 96, 48, 48)
        })
    }
}

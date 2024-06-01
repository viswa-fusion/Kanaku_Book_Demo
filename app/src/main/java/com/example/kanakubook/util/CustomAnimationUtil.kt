package com.example.kanakubook.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

internal object CustomAnimationUtil {

    const val GROUP_SCREEN_FAB = 1
    const val FRIEND_SCREEN_FAB = 2

    fun rotateFab(mainFab: FloatingActionButton,blurFadeScreen: View, isFabRotated: Boolean): Boolean {
        val fromDegrees = if (isFabRotated) 45f else 0f
        val toDegrees = if (isFabRotated) 0f else 45f
        if (!isFabRotated) {
            blurFadeScreen.visibility = View.VISIBLE
            blurFadeScreen.animate().translationY(0f).alpha(1f).setDuration(300).start()
        } else {
            blurFadeScreen.visibility = View.GONE
            val translationY = blurFadeScreen.height.toFloat()
            blurFadeScreen.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        }
        val rotateAnimation = RotateAnimation(
            fromDegrees,
            toDegrees,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 200
        rotateAnimation.fillAfter = true
        mainFab.startAnimation(rotateAnimation)
        return !isFabRotated
    }

    fun showFABs(groupFab: ExtendedFloatingActionButton, friendFab: ExtendedFloatingActionButton, expenseFab: ExtendedFloatingActionButton, fabScreenId: Int) {
        expenseFab.visibility = View.VISIBLE
        when (fabScreenId) {
            GROUP_SCREEN_FAB -> {
                groupFab.visibility = View.VISIBLE
                groupFab.animate().translationY(0f).alpha(1f).setDuration(500).start()
                expenseFab.animate().translationY(0f).alpha(1f).setDuration(500).start()
//                friendFab.animate().translationY(friendFab.height.toFloat())
//                    .alpha(0f).setDuration(300).start()
            }

            FRIEND_SCREEN_FAB -> {
                friendFab.visibility = View.VISIBLE
                friendFab.animate().translationY(0f).alpha(1f).setDuration(500).start()
                expenseFab.animate().translationY(0f).alpha(1f).setDuration(500).start()
//                groupFab.animate().translationY(groupFab.height.toFloat()).alpha(0f)
//                    .setDuration(300).start()
            }
        }
    }

    fun hideFABs(groupFab: ExtendedFloatingActionButton, friendFab: ExtendedFloatingActionButton, expenseFab: ExtendedFloatingActionButton) {
        val translationY = groupFab.height.toFloat()

        groupFab.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        expenseFab.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        friendFab.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        expenseFab.visibility = View.GONE
        groupFab.visibility = View.GONE
        friendFab.visibility = View.GONE
    }

}
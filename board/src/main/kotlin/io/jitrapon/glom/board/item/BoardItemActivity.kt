package io.jitrapon.glom.board.item

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.WindowManager
import io.jitrapon.glom.base.ui.BaseActivity
import io.jitrapon.glom.board.Const

/* animation delay time in ms before content of this view appears */
const val SHOW_ANIM_DELAY = 300L

/* enter transition animation time */
const val SHARED_ELEMENT_ANIM_TIME = 220L

/**
 * Base parent activity to be used to show different board item when in detailed, expanded mode
 *
 * @author Jitrapon Tiachunpun
 */
abstract class BoardItemActivity : BaseActivity() {

    /*
     * Indicates whether or not this activity has been started yet
     */
    private var isActivityStarted: Boolean = false

    /**
     * Transition listener for properly animating objects at respect times to transition animations
     */
    private val transitionListener: android.transition.Transition.TransitionListener = object : android.transition.Transition.TransitionListener {

        override fun onTransitionStart(p0: android.transition.Transition?) {
            if (!isActivityStarted) onBeginTransitionAnimationStart()
            else {
                undimBackground()
                onFinishTransitionAnimationStart()
            }
        }

        override fun onTransitionEnd(p0: android.transition.Transition?) {
            if (!isActivityStarted) {
                dimBackground()
                onBeginTransitionAnimationEnd()
                isActivityStarted = true
            }
            else onFinishTransitionAnimationEnd()
        }

        override fun onTransitionResume(p0: android.transition.Transition?) {}

        override fun onTransitionPause(p0: android.transition.Transition?) {}

        override fun onTransitionCancel(p0: android.transition.Transition?) {}
    }

    //region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())

        window.apply {
            // must be called, otherwise layout won't appear
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

            // set transition element
            sharedElementEnterTransition = ChangeBounds().apply {
                duration = SHARED_ELEMENT_ANIM_TIME
            }
            sharedElementEnterTransition.addListener(transitionListener)

            sharedElementExitTransition = ChangeBounds().apply {
                duration = SHARED_ELEMENT_ANIM_TIME
            }
        }
    }

    /**
     * We want to make sure to save user's changes before finishing this activity
     */
    override fun onBackPressed() {
        onDismiss()
    }

    //endregion
    //region other callbacks

    /**
     * Returns the current board item representing this view
     */
    protected fun getBoardItemFromIntent() = intent?.getParcelableExtra<BoardItem?>(Const.EXTRA_BOARD_ITEM)

    /**
     * Returns whether or not this item is new
     */
    protected fun isNewItem(): Boolean = intent?.getBooleanExtra(Const.EXTRA_IS_BOARD_ITEM_NEW, false) ?: false

    /**
     * Invoked when the Activity is about to be dismissed and any checks to make sure
     * user's data is saved should be performed at this stage
     */
    abstract fun onDismiss()

    /**
     * Perform saving the state to Bundle and dismiss this Activity
     */
    internal fun dismissView(savedState: BoardItem.SavedState) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(Const.EXTRA_BOARD_ITEM, savedState.item)
            putExtra(Const.EXTRA_IS_BOARD_ITEM_MODIFIED, savedState.isItemModified)
            putExtra(Const.EXTRA_IS_BOARD_ITEM_NEW, savedState.isItemNew)
        })
        supportFinishAfterTransition()
    }

    /**
     * Returns the layout ID of this activity to be inflated
     */
    abstract fun getLayout(): Int

    /**
     * Called when activity is about to start and animation transition starts
     */
    open fun onBeginTransitionAnimationStart() {}

    /**
     * Called when activity is about to start and animation transition ends
     */
    open fun onBeginTransitionAnimationEnd() {}

    /**
     * Called when activity is about to finish and animation transition starts
     */
    open fun onFinishTransitionAnimationStart() {}

    /**
     * Called when activity is about to finish and animation transition ends
     */
    open fun onFinishTransitionAnimationEnd() {}

    /**
     * Dims background behind this activity
     */
    private fun dimBackground(dimAmount: Float = 0.6f) {
        window.apply {
            attributes.dimAmount = dimAmount
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    /**
     * Undims the background behind this activity
     */
    private fun undimBackground() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    //endregion
}

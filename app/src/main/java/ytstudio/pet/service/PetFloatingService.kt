package ytstudio.pet.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import ytstudio.pet.MainActivity
import ytstudio.pet.PetApplication
import ytstudio.pet.R
import ytstudio.pet.data.CharacterRepository
import ytstudio.pet.model.PetCharacter
import ytstudio.pet.model.SpecialAction
import kotlin.math.abs
import kotlin.random.Random

class PetFloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var ivPetSleep: ImageView? = null
    private var ivPetKiss: ImageView? = null
    private var tvSleepZzz: TextView? = null
    private var bubbleLayout: LinearLayout? = null
    private var tvBubbleText: TextView? = null
    private var petBodyContainer: FrameLayout? = null

    private var sleepBreatheAnimator: AnimatorSet? = null
    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentCharacter: PetCharacter? = null
    private var isKissing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForeground(PetApplication.NOTIFICATION_ID, createNotification())
        initFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        refreshCharacterData()
        return START_STICKY
    }

    private fun refreshCharacterData() {
        currentCharacter = CharacterRepository.getActiveCharacter(this)
        ivPetSleep?.setImageResource(currentCharacter?.avatarResId ?: R.drawable.avatar_sleep)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, PetFloatingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, PetApplication.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("桌宠“小傻子”正在桌面上呼呼大睡…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "收回桌宠", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.layout_floating_pet, null)

        ivPetSleep = floatingView?.findViewById(R.id.ivPetSleep)
        ivPetKiss = floatingView?.findViewById(R.id.ivPetKiss)
        tvSleepZzz = floatingView?.findViewById(R.id.tvSleepZzz)
        bubbleLayout = floatingView?.findViewById(R.id.bubbleLayout)
        tvBubbleText = floatingView?.findViewById(R.id.tvBubbleText)
        petBodyContainer = floatingView?.findViewById(R.id.petBodyContainer)

        refreshCharacterData()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        setupTouchListener()
        startSleepBreathingAnimation()

        windowManager?.addView(floatingView, layoutParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || layoutParams == null) return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams!!.x
                        initialY = layoutParams!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > 12 || abs(dy) > 12) {
                            isDragging = true
                            layoutParams!!.x = initialX + dx
                            layoutParams!!.y = initialY + dy
                            windowManager?.updateViewLayout(floatingView, layoutParams)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            onPetClicked()
                        } else {
                            snapToNearestEdge()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun snapToNearestEdge() {
        val params = layoutParams ?: return
        val displayMetrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val viewWidth = floatingView?.width ?: 200

        val targetX = if (params.x + viewWidth / 2 < screenWidth / 2) {
            20
        } else {
            screenWidth - viewWidth - 20
        }

        val startX = params.x
        val animator = ValueAnimator.ofInt(startX, targetX)
        animator.duration = 260
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            params.x = animation.animatedValue as Int
            windowManager?.updateViewLayout(floatingView, params)
        }
        animator.start()
    }

    private fun startSleepBreathingAnimation() {
        val target = petBodyContainer ?: return
        val scaleX = ObjectAnimator.ofFloat(target, "scaleX", 1.0f, 1.05f, 1.0f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 2400
        }
        val scaleY = ObjectAnimator.ofFloat(target, "scaleY", 1.0f, 1.06f, 1.0f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 2400
        }

        val zzz = tvSleepZzz ?: return
        val zzzAlpha = ObjectAnimator.ofFloat(zzz, "alpha", 0.4f, 1.0f, 0.4f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 2400
        }
        val zzzTransY = ObjectAnimator.ofFloat(zzz, "translationY", 0f, -8f, 0f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 2400
        }

        sleepBreatheAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, zzzAlpha, zzzTransY)
            start()
        }
    }

    private fun onPetClicked() {
        if (isKissing) return

        val char = currentCharacter ?: CharacterRepository.getActiveCharacter(this)
        val hasKissAction = char.specialActionType == SpecialAction.KISS_AUDIO

        // 6 potential actions: 5 dialogues + 1 kiss action
        val dialogues = char.dialogues
        val totalOptions = dialogues.size + if (hasKissAction) 1 else 0

        val choice = Random.nextInt(totalOptions)
        if (hasKissAction && choice == dialogues.size) {
            performKissAction()
        } else {
            val text = dialogues.getOrElse(choice) { "呼呼大睡中…" }
            showSpeechBubble(text)
        }
    }

    private fun showSpeechBubble(text: String) {
        mainHandler.removeCallbacksAndMessages(null)
        tvBubbleText?.text = text
        bubbleLayout?.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.7f
            scaleY = 0.7f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        mainHandler.postDelayed({
            bubbleLayout?.animate()
                ?.alpha(0f)
                ?.scaleX(0.7f)
                ?.scaleY(0.7f)
                ?.setDuration(200)
                ?.withEndAction {
                    bubbleLayout?.visibility = View.GONE
                }
                ?.start()
        }, 3200)
    }

    private fun performKissAction() {
        isKissing = true
        showSpeechBubble("啾咪~ ❤️")

        // Switch to kiss animation
        tvSleepZzz?.visibility = View.GONE
        ivPetSleep?.visibility = View.GONE
        ivPetKiss?.visibility = View.VISIBLE

        playKissAudio()

        // After 4.8 seconds (duration of video kiss scene), restore sleeping avatar
        mainHandler.postDelayed({
            ivPetKiss?.visibility = View.GONE
            ivPetSleep?.visibility = View.VISIBLE
            tvSleepZzz?.visibility = View.VISIBLE
            isKissing = false
        }, 4800)
    }

    private fun playKissAudio() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.kiss_audio)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        sleepBreatheAnimator?.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
        if (floatingView != null && windowManager != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
    }

    companion object {
        const val ACTION_STOP = "ytstudio.pet.ACTION_STOP"
        var isRunning = false
            private set
    }
}

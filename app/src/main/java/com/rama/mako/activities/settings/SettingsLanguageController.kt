package com.rama.mako.activities.settings

<<<<<<< HEAD
import android.util.TypedValue
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
=======
import android.widget.RadioGroup
>>>>>>> 1aacd6a (i18n but hell to maintain version)
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager

class SettingsLanguageController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        val group = activity.findViewById<RadioGroup>(R.id.language_group)
<<<<<<< HEAD
        val codes = activity.resources.getStringArray(R.array.supported_language_codes)
        val labels = activity.resources.getStringArray(R.array.supported_language_labels)
        require(codes.size == labels.size) {
            "supported_language_codes (${codes.size}) and supported_language_labels (${labels.size}) must have the same length"
        }
        val currentLanguage = prefs.getAppLanguage()

        val codeToId = mutableMapOf<String, Int>()

        codes.zip(labels).forEachIndexed { index, (code, label) ->
            val rb = RadioButton(activity).apply {
                id = View.generateViewId()
                text = label
                textSize = 16f
                setTextColor(ContextCompat.getColor(activity, R.color.foreground))
                val params = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                if (index < codes.size - 1) {
                    val marginBottomPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, 8f, resources.displayMetrics
                    ).toInt()
                    params.bottomMargin = marginBottomPx
                }
                layoutParams = params
            }
            codeToId[code] = rb.id
            group.addView(rb)
        }

        codeToId[currentLanguage]?.let { group.check(it) }

        group.setOnCheckedChangeListener { _, checkedId ->
            val language = codeToId.entries
                .firstOrNull { it.value == checkedId }?.key
                ?: PrefsManager.Language.SYSTEM
=======

        when (prefs.getAppLanguage()) {
            PrefsManager.AppLanguage.ENGLISH -> group.check(R.id.language_english)
            PrefsManager.AppLanguage.GERMAN -> group.check(R.id.language_german)
            else -> group.check(R.id.language_system)
        }

        group.setOnCheckedChangeListener { _, id ->
            val language = when (id) {
                R.id.language_english -> PrefsManager.AppLanguage.ENGLISH
                R.id.language_german -> PrefsManager.AppLanguage.GERMAN
                else -> PrefsManager.AppLanguage.SYSTEM
            }
>>>>>>> 1aacd6a (i18n but hell to maintain version)

            if (language == prefs.getAppLanguage()) {
                return@setOnCheckedChangeListener
            }

            prefs.setAppLanguage(language)
            activity.recreate()
        }
    }
}
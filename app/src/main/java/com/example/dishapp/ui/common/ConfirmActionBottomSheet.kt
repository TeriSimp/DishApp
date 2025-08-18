package com.example.dishapp.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.dishapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmActionBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val REQUEST_KEY = "confirm_request"
        const val BUNDLE_KEY_CONFIRM = "confirmed"
        const val EXTRA_ACTION = "extra_action"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CONFIRM_TEXT = "extra_confirm_text"
        const val EXTRA_CANCEL_TEXT = "extra_cancel_text"
        const val EXTRA_CONFIRM_COLOR = "extra_confirm_color"
        const val EXTRA_CANCEL_COLOR = "extra_cancel_color"
        const val EXTRA_SHOW_CHEVRON = "extra_show_chevron"

        fun newInstance(
            title: String? = null,
            message: String? = null,
            action: String? = null,
            confirmText: String? = null,
            cancelText: String? = null,
            confirmColorInt: Int? = null,
            cancelColorInt: Int? = null,
            showChevron: Boolean = true
        ): ConfirmActionBottomSheet {
            val bs = ConfirmActionBottomSheet()
            bs.arguments = bundleOf().apply {
                title?.let { putString(EXTRA_TITLE, it) }
                message?.let { putString(EXTRA_MESSAGE, it) }
                action?.let { putString(EXTRA_ACTION, it) }
                confirmText?.let { putString(EXTRA_CONFIRM_TEXT, it) }
                cancelText?.let { putString(EXTRA_CANCEL_TEXT, it) }
                confirmColorInt?.let { putInt(EXTRA_CONFIRM_COLOR, it) }
                cancelColorInt?.let { putInt(EXTRA_CANCEL_COLOR, it) }
                putBoolean(EXTRA_SHOW_CHEVRON, showChevron)
            }
            return bs
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.bs_confirm, container, false)

        val tvTitle = root.findViewById<TextView?>(R.id.tvTitle)
        val tvMessage = root.findViewById<TextView>(R.id.tvMessage)
        val btnCancel = root.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = root.findViewById<Button>(R.id.btnConfirm)
        val btnChevron =
            root.findViewById<ImageButton?>(R.id.btnChevron)

        val title = arguments?.getString(EXTRA_TITLE)
        val message = arguments?.getString(EXTRA_MESSAGE) ?: getString(R.string.logout_ques)
        val action = arguments?.getString(EXTRA_ACTION)
        val confirmText = arguments?.getString(EXTRA_CONFIRM_TEXT) ?: getString(android.R.string.ok)
        val cancelText =
            arguments?.getString(EXTRA_CANCEL_TEXT) ?: getString(android.R.string.cancel)
        val confirmColor = if (arguments?.containsKey(EXTRA_CONFIRM_COLOR) == true)
            arguments?.getInt(EXTRA_CONFIRM_COLOR) else null
        val cancelColor = if (arguments?.containsKey(EXTRA_CANCEL_COLOR) == true)
            arguments?.getInt(EXTRA_CANCEL_COLOR) else null
        val showChevron = arguments?.getBoolean(EXTRA_SHOW_CHEVRON, true) ?: true

        tvTitle?.let {
            if (!title.isNullOrEmpty()) {
                it.text = title
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }

        tvMessage.text = message

        btnConfirm.text = confirmText
        btnCancel.text = cancelText

        confirmColor?.let { btnConfirm.setTextColor(it) }
        cancelColor?.let { btnCancel.setTextColor(it) }

        btnChevron?.let { chevron ->
            chevron.visibility = if (showChevron) View.VISIBLE else View.GONE
            chevron.setOnClickListener {
                setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_CONFIRM to false))
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_CONFIRM to false))
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean(BUNDLE_KEY_CONFIRM, true)
                action?.let { putString(EXTRA_ACTION, it) }
            }
            setFragmentResult(REQUEST_KEY, bundle)
            dismiss()
        }

        return root
    }
}

package io.jitrapon.glom.board.item.event.widget.datetimepicker

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.plusAssign
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import io.jitrapon.glom.base.model.AndroidPluralString
import io.jitrapon.glom.base.model.AndroidString
import io.jitrapon.glom.base.model.UiModel
import io.jitrapon.glom.base.ui.widget.GlomBottomSheetDialogFragment
import io.jitrapon.glom.base.ui.widget.calendar.GlomCalendarView
import io.jitrapon.glom.base.util.*
import io.jitrapon.glom.board.DateTimePickerUiModel
import io.jitrapon.glom.board.R
import io.jitrapon.glom.board.widget.calendar.BoardCalendarDecorator
import kotlinx.android.synthetic.main.date_time_picker_bottom_sheet.*
import kotlinx.android.synthetic.main.date_time_picker_date_item.view.*
import java.util.*
import kotlin.collections.ArrayList

const val ANIMATION_DELAY = 100L

class BottomSheetDateTimePicker : GlomBottomSheetDialogFragment() {

    private var uiModel: DateTimePickerUiModel? = null

    private lateinit var viewModel: DateTimePickerViewModel

    private lateinit var onDateTimeSetListener: (Date?, Date?, Boolean) -> Unit

    private var collapsedViews: ArrayList<View>? = null
    private var expandedViews: ArrayList<View> = ArrayList()
    private var calendarView: GlomCalendarView? = null

    private var hasExpanded = false

    private val collapsedPeekHeight
            get() = 354.px

    override fun getLayoutId() = R.layout.date_time_picker_bottom_sheet

    fun init(uiModel: DateTimePickerUiModel,
             onDateTimeSetListener: (Date?, Date?, Boolean) -> Unit) {
        this.uiModel = uiModel
        this.onDateTimeSetListener = onDateTimeSetListener
    }

    private val bottomSheetStateCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        private var hasExpandedView = false
        private var hasCollapsedView = true

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (!hasCollapsedView && slideOffset < 0.35) {
                expandedViews.forEach {
                    it.hide()
                }
                collapsedViews?.forEach {
                    it.show()
                }

                // reset the constraint set of the switch to be underneath the date items
                changeViewAnchors(false)

                hasExpandedView = false
                hasCollapsedView = true
            }
            if (!hasExpandedView && slideOffset > 0.65) {
                expandedViews.forEach {
                    it.show()
                }
                collapsedViews?.forEach {
                    it.hide()
                }

                hasExpandedView = true
                hasCollapsedView = false
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> dialog?.cancel()
                BottomSheetBehavior.STATE_EXPANDED -> {
                    date_time_picker_bottom_sheet_calendar_view_stub?.apply {
                        setOnInflateListener { _, inflated ->
                            expandedViews.add(inflated)
                            inflated.findViewById<GlomCalendarView>(R.id.calendar_view)?.apply {
                                calendarView = this
                                setPadding(0, 0, 0, 16.px)
                                init(initialSelections = arrayOf(viewModel.startDate, viewModel.endDate),
                                        decoratorSources = arrayListOf(BoardCalendarDecorator(context, viewModel.occupiedDates, 5)),
                                        selectionMode = viewModel.calendarSelectionMode,
                                        isEditable = viewModel.isEditable, onDateSelectListener = { date, isSelected ->
                                    if (isSelected) {
                                        viewModel.selectCalendarDate(date)
                                    }
                                    else {
                                        viewModel.clearCurrentDate()
                                    }
                                })
                            }

                            // once calendar is found, set the constraint set of
                            // the switch to be underneath the calendar
                            changeViewAnchors(true)
                        }
                        inflate()
                    }

                    if (!hasExpanded && calendarView?.isInitialized == true) {
                        resetCalendar()

                        changeViewAnchors(true)
                    }

                    hasExpanded = true
                }
                BottomSheetBehavior.STATE_SETTLING -> {

                }
                BottomSheetBehavior.STATE_DRAGGING -> {

                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    if (hasExpanded) {
                        viewModel.requestDateTimeChange()
                    }

                    hasExpanded = false
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
            }
        }
    }

    private fun changeViewAnchors(expanded: Boolean) {
        (date_time_picker_bottom_sheet_full_day_toggle.layoutParams as ConstraintLayout.LayoutParams).apply {
            topToBottom = if (expanded) {
                calendarView!!.id
            }
            else {
                date_time_picker_date_item_1.id
            }
            date_time_picker_bottom_sheet_full_day_toggle.requestLayout()
        }
    }

    override fun onResume() {
        super.onResume()

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            setBottomSheetCallback(bottomSheetStateCallback)
            peekHeight = collapsedPeekHeight
        }
    }

    override fun onSetupView(view: View) {
        date_time_picker_date_item_1.setOnClickListener { viewModel.selectDateChoice(0) }
        date_time_picker_date_item_2.setOnClickListener { viewModel.selectDateChoice(1) }
        date_time_picker_date_item_3.setOnClickListener { viewModel.selectDateChoice(2) }
        date_time_picker_date_item_4.setOnClickListener { viewModel.selectDateChoice(3) }
        date_time_picker_date_item_5.setOnClickListener { viewModel.selectDateChoice(4) }

        date_time_picker_bottom_sheet_morning_choice.setOnClickListener { viewModel.selectDayTimeChoice(0) }
        date_time_picker_bottom_sheet_afternoon_choice.setOnClickListener { viewModel.selectDayTimeChoice(1) }
        date_time_picker_bottom_sheet_evening_choice.setOnClickListener { viewModel.selectDayTimeChoice(2) }
        date_time_picker_bottom_sheet_night_choice.setOnClickListener { viewModel.selectDayTimeChoice(3) }

        date_time_picker_bottom_sheet_cancel_button.setOnClickListener { dismiss() }
        date_time_picker_bottom_sheet_done_button.setOnClickListener { viewModel.confirmSelection() }

        date_time_picker_layout.findViewsWithContentDescription(getString(R.string.date_picker_collapsed_view)) {
            collapsedViews = this
        }

        date_time_picker_bottom_sheet_full_day_text.setOnClickListener { viewModel.toggleFullDay() }
        date_time_picker_bottom_sheet_full_day_toggle.setOnClickListener { viewModel.toggleFullDay() }

        date_time_picker_bottom_sheet_edit_button.setOnClickListener {
            date_time_picker_bottom_sheet_edit_text.requestFocus()
        }
        date_time_picker_bottom_sheet_clear_button.setOnClickListener {
            viewModel.clearCurrentDate()
        }
        date_time_picker_edit_time.setOnClickListener {
            viewModel.showTimePicker()
        }
    }

    override fun onCreateViewModel(activity: FragmentActivity) {
        viewModel = obtainViewModel(DateTimePickerViewModel::class.java).apply {
            setDateTime(uiModel)
        }
    }

    override fun onSubscribeToObservables() {
        subscribeToViewActionObservables(viewModel.observableViewAction)

        viewModel.getObservableDate().observe(viewLifecycleOwner, Observer {
            if (it == null) {
                date_time_picker_bottom_sheet_edit_text.text = null
                date_time_picker_bottom_sheet_full_day_toggle.isEnabled = false
                date_time_picker_edit_time.isEnabled = false
                date_time_picker_bottom_sheet_full_day_text.isEnabled = false
                calendarView?.clear()
            }
            else {
                val isFullDay = viewModel.getObservableFullDay().value
                date_time_picker_bottom_sheet_edit_text.text = SpannableStringBuilder(context!!.getString(it))
                date_time_picker_bottom_sheet_full_day_toggle.isEnabled = true
                date_time_picker_edit_time.isEnabled = isFullDay != null && !isFullDay
                date_time_picker_bottom_sheet_full_day_text.isEnabled = true
            }
        })
        viewModel.getObservableFullDay().observe(viewLifecycleOwner, Observer {
            it?.let { isFullDay ->
                val isEnabled = !isFullDay && viewModel.hasSelectedDate
                date_time_picker_bottom_sheet_full_day_toggle.isChecked = isFullDay
                date_time_picker_bottom_sheet_time_of_day_layout.forEach { view ->
                    view.isEnabled = isEnabled
                }
                date_time_picker_bottom_sheet_time_layout.forEach { view ->
                    view.isEnabled = isEnabled
                }
                date_time_picker_edit_time.isEnabled = isEnabled
            }
        })
        viewModel.getObservableSimpleDateChoices().observe(viewLifecycleOwner, Observer {
            it?.let {
                setSimpleDateText(date_time_picker_date_item_1, it[0])
                setSimpleDateText(date_time_picker_date_item_2, it[1])
                setSimpleDateText(date_time_picker_date_item_3, it[2])
                setSimpleDateText(date_time_picker_date_item_4, it[3])
                setSimpleDateText(date_time_picker_date_item_5, it[4])
            }
        })
        viewModel.getObservableDayTimeChoice().observe(viewLifecycleOwner, Observer {
            it?.let { choice ->
                if (choice == NOT_SELECTED_INDEX) {
                    date_time_picker_bottom_sheet_morning_choice.apply {
                        isChecked = false
                        isEnabled = false
                    }
                    date_time_picker_bottom_sheet_afternoon_choice.apply {
                        isChecked = false
                        isEnabled = false
                    }
                    date_time_picker_bottom_sheet_evening_choice.apply {
                        isChecked = false
                        isEnabled = false
                    }
                    date_time_picker_bottom_sheet_night_choice.apply {
                        isChecked = false
                        isEnabled = false
                    }
                }
                else {
                    val isFullDay = viewModel.getObservableFullDay().value?.not() ?: false
                    date_time_picker_bottom_sheet_morning_choice.apply {
                        isChecked = choice == 0
                        isEnabled = isFullDay
                    }
                    date_time_picker_bottom_sheet_afternoon_choice.apply {
                        isChecked = choice == 1
                        isEnabled = isFullDay
                    }
                    date_time_picker_bottom_sheet_evening_choice.apply {
                        isChecked = choice == 2
                        isEnabled = isFullDay
                    }
                    date_time_picker_bottom_sheet_night_choice.apply {
                        isChecked = choice == 3
                        isEnabled = isFullDay
                    }
                    date_time_picker_edit_time.isEnabled = isFullDay
                }
            }
        })
        viewModel.getObservableTimeChoices().observe(viewLifecycleOwner, Observer {
            it?.let { choices ->
                date_time_picker_bottom_sheet_time_layout.removeAllViews()
                if (choices.isEmpty()) {
                    date_time_picker_bottom_sheet_time_layout.hide()
                }
                else {
                    date_time_picker_bottom_sheet_time_layout.show()
                    val hasSelectedDayTimeChoice = viewModel.getObservableDayTimeChoice().value != NOT_SELECTED_INDEX
                    for (choice in choices) {
                        (LayoutInflater.from(context).inflate(R.layout.date_time_picker_chip, null) as Chip).apply {
                            text = context.getString(choice.timeOfDay)
                            setOnClickListener {
                                viewModel.selectTimeChoice(choice)
                            }
                            tag = choice
                            isChecked = choice.status == UiModel.Status.POSITIVE
                            date_time_picker_bottom_sheet_time_layout += this
                            setMargin(right = 3f.px)
                            isEnabled = viewModel.getObservableFullDay().value?.not()?.and(hasSelectedDayTimeChoice) ?: false
                        }
                    }
                }
            }
        })
        viewModel.getObservableTimeChoice().observe(viewLifecycleOwner, Observer {
            it?.let {
                for (view in date_time_picker_bottom_sheet_time_layout.children) {
                    if (view is Chip) {
                        view.isChecked = view.tag == it
                    }
                }
            }
        })
        viewModel.getObservableFinishEvent().observe(viewLifecycleOwner, Observer {
            dismiss()

            it?.let {
                onDateTimeSetListener(it.start, it.end, it.isFullDay)
            }
        })
        viewModel.getObservableTimePicker().observe(viewLifecycleOwner, Observer {
            val calendar = Calendar.getInstance().apply {
                time = it
            }
            TimePickerDialog(context, { _, hourOfDay, minute ->
                viewModel.setTime(hourOfDay, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)).show()
        })
        viewModel.getObservableInstruction().observe(viewLifecycleOwner, Observer {
            when (it) {
                is AndroidPluralString -> date_time_picker_bottom_sheet_date_info.text = context!!.getPluralString(it)
                is AndroidString -> date_time_picker_bottom_sheet_date_info.text = context!!.getString(it)
                else -> date_time_picker_bottom_sheet_date_info.text = null
            }
        })
    }

    private fun resetCalendar() {
        calendarView?.apply {
            select(viewModel.startOrEndDate, scrollToDate = false, selected = true)
            viewModel.selectCalendarDate(viewModel.startOrEndDate)
        }
    }

    private fun setSimpleDateText(view: View, uiModel: DateChoiceUiModel) {
        view.date_time_picker_date_item_day_of_week.text = context!!.getString(uiModel.dayOfWeek)
        view.date_time_picker_date_item_date.text = context!!.getString(uiModel.dayOfMonth)
        if (uiModel.status == UiModel.Status.POSITIVE) {
            view.background = context!!.drawable(io.jitrapon.glom.R.drawable.bg_solid_circle)
            view.backgroundTintList = ColorStateList.valueOf(context!!.colorPrimary())
            view.date_time_picker_date_item_day_of_week.setTextColor(context!!.color(io.jitrapon.glom.R.color.white))
            view.date_time_picker_date_item_date.setTextColor(context!!.color(io.jitrapon.glom.R.color.white))
        }
        else {
            view.background = null
            view.backgroundTintList = null
            view.date_time_picker_date_item_day_of_week.setTextColor(context!!.attrColor(android.R.attr.textColorPrimary))
            view.date_time_picker_date_item_date.setTextColor(context!!.attrColor(android.R.attr.textColorSecondary))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        hasExpanded = false
    }
}

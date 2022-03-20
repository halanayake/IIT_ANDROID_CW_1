package com.example.equalequations

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {

    // Object to get string equation and numerical total
    class DataObj(val equation: String, var total: Int)

    private var leftEquation:DataObj? = null
    private var rightEquation:DataObj? = null

    private val leftEqConst = "left_eq"
    private val leftValConst = "left_val"
    private val rightEqConst = "right_eq"
    private val rightValConst = "right_val"
    private val targetTimeConst = "target_time"
    private val correctCountConst = "correct_count"
    private val correctTotConst = "correct_tot"
    private val incorrectTotConst = "incorrect_tot"

    private var targetTime: Long? = null
    private var timeLeft: Long? = null
    private var timer: CountDownTimer? = null
    private var correctCount: Int = 0
    private var totCorrect: Int = 0
    private var totIncorrect: Int = 0

    // Life cycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // Generate and assign equations to textViews savedState is passed to load previous values
        generateAndAssignEquations(savedInstanceState)
        val tempTargetTime = savedInstanceState?.getLong(targetTimeConst)
        if (savedInstanceState != null) {
            correctCount = savedInstanceState.getInt(correctCountConst)
            totCorrect = savedInstanceState.getInt(correctTotConst)
            totIncorrect = savedInstanceState.getInt(incorrectTotConst)
        }
        // Target time is a future time in milliseconds
        targetTime = if (tempTargetTime != null) {
            tempTargetTime
        } else {
            val currentTimeNow = Calendar.getInstance()
            currentTimeNow.add(Calendar.SECOND, 50)
            currentTimeNow.timeInMillis
        }
        // Time left is calculated by subtracting current time from future time.
        // This way timer will not freeze when activity gets paused
        // References - https://developer.android.com/reference/android/os/CountDownTimer
        timeLeft = targetTime!! - Calendar.getInstance().timeInMillis
        timer = object : CountDownTimer(timeLeft!!, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                findViewById<TextView>(R.id.time).text = createTimeString(millisUntilFinished)
            }
            override fun onFinish() {
                showResults()
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        // Cancel the timer on pause. To avoid multiple timers when activity resumes
        timer?.cancel()
    }

    /**
     * Save attributes that needs to be persisted through activity lifecycles
     *
     * References - https://developer.android.com/guide/components/activities/activity-lifecycle
     */
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        if (leftEquation != null) {
            state.putString(leftEqConst, leftEquation!!.equation)
            state.putInt(leftValConst, leftEquation!!.total)
        }
        if (rightEquation != null) {
            state.putString(rightEqConst, rightEquation!!.equation)
            state.putInt(rightValConst, rightEquation!!.total)
        }
        if (targetTime != null) {
            state.putLong(targetTimeConst, targetTime!!)
        }
        state.putInt(correctCountConst, correctCount)
        state.putInt(correctTotConst, totCorrect)
        state.putInt(incorrectTotConst, totIncorrect)
    }

    /**
     * Create a string with remaining time which is displayed in the UI
     *
     * @param time time to be displayed in milliseconds
     * @return formatted time
     */
    private fun createTimeString(time: Long): String {
        val minutes = (time / 1000 / 60).toString()
        val seconds = (time / 1000 % 60).toString()
        return "${minutes.padStart(2, '0')}:${seconds.padStart(2, '0')}"
    }

    /**
     * Generate or load equations and assign them to textViews
     *
     * @param savedInstanceState to load saved values (if available)
     */
    private fun generateAndAssignEquations(savedInstanceState: Bundle?) {
        try {
            val tempLeftEq = savedInstanceState?.getString(leftEqConst)
            val tempLeftVal = savedInstanceState?.getInt(leftValConst)
            leftEquation = if (tempLeftEq != null && tempLeftVal != null) {
                DataObj(tempLeftEq, tempLeftVal)
            } else {
                generateEquation()
            }
            findViewById<TextView>(R.id.leftEquation).text = leftEquation!!.equation

            val tempRightEq = savedInstanceState?.getString(rightEqConst)
            val tempRightVal = savedInstanceState?.getInt(rightValConst)
            rightEquation = if (tempRightEq != null && tempRightVal != null) {
                DataObj(tempRightEq, tempRightVal)
            } else {
                generateEquation()
            }
            findViewById<TextView>(R.id.rightEquation).text = rightEquation!!.equation
        } catch (ignore:Exception) {
            Toast.makeText(this, "Recovered from an error", Toast.LENGTH_SHORT).show()
            generateAndAssignEquations(savedInstanceState)
        }
    }

    /**
     * Method used to generate both equations
     *
     * @return dataObj object with string equation and numeric result
     */
    private fun generateEquation(): DataObj {
        val operators = mutableListOf('+', '-', '*', '/')
        val selectedNumbers = mutableListOf<Int>()
        val selectedOperators = mutableListOf<Char>()
        // Number of terms is determined first
        val termCount = (1..4).random()
        for (term in (1..termCount)) {
            // generate random numbers
            selectedNumbers.add((1..20).random())
            if (term != termCount) {
                // Pick random operators (removeAt is used to avoid repetition)
                selectedOperators.add(operators.removeAt((0 until operators.size).random()))
            }
        }
        // selected numbers and operators are passed to make division without remainders
        fixEquation(selectedNumbers, selectedOperators)
        var equation = ""
        // selected numbers and operators are passed to make total less than 100
        val total = solveEquationAndResolve(selectedNumbers, selectedOperators)
        // equation string is made by adding brackets and combining with operators
        for ((index, num) in selectedNumbers.withIndex()) {
            equation += num
            if (index != 0 && index != selectedNumbers.lastIndex) {
                equation = "($equation)"
            }
            if (index != selectedNumbers.lastIndex) {
                equation += selectedOperators[index]
            }
        }
        return DataObj(equation, total)
    }

    /**
     * Call sub methods if given operators exist in the list
     *
     * @param selectedNumbers selected numbers
     * @param selectedOperators selected operators
     */
    private fun fixEquation(selectedNumbers: MutableList<Int>, selectedOperators: MutableList<Char>) {
        if ('-' in selectedOperators) {
            fixSubtraction(selectedNumbers, selectedOperators)
        }
        if ('/' in selectedOperators) {
            fixDivision(selectedNumbers, selectedOperators)
        }
    }

    /**
     * Max number and second max number in the number list is picked and placed on left and right
     * of the subtraction operator to avoid negative results. (Get called only if '-' is present)
     *
     * @param numbers selected numbers
     * @param operators selected operators
     */
    private fun fixSubtraction(numbers: MutableList<Int>, operators: MutableList<Char>) {
        if (operators.contains('/') || operators.contains('*')) {
            val max = numbers.maxOrNull()!!
            numbers.remove(max)
            operators.remove('-')
            var secondMax = 0
            for (num in numbers) {
                if (num > secondMax) {
                    secondMax = num
                }
            }
            numbers.remove(secondMax)
            numbers.add(0, max)
            numbers.add(1, secondMax)
            operators.add(0, '-')
        } else {
            val maxPos = numbers.indexOf(numbers.maxOrNull()!!)
            val opIndex = operators.indexOf('-')
            val tempVal1 = numbers[opIndex]
            numbers[opIndex] = numbers[maxPos]
            numbers[maxPos] = tempVal1
            val tempVal2 = numbers[opIndex+1]
            var secondMaxPos = numbers.lastIndex
            for ((index, num) in numbers.withIndex()) {
                if (num > numbers[secondMaxPos] && index != opIndex) {
                    secondMaxPos = index
                }
            }
            numbers[opIndex+1] = numbers[secondMaxPos]
            numbers[secondMaxPos] = tempVal2
        }
    }

    /**
     * Smallest number in the list is picked and place it as the divider. Remaining values are
     * adjusted to make the division without remainders
     *
     * @param numbers selected numbers
     * @param operators selected operators
     */
    private fun fixDivision(numbers: MutableList<Int>, operators: MutableList<Char>) {
        var min: Int = numbers.minOrNull()!!
        val opIndex = operators.indexOf('/')
        val tempVal = numbers[opIndex+1]
        numbers[numbers.indexOf(min)] = tempVal
        numbers[opIndex+1] = min
        // Only left side operators and numbers are considered
        val validOperators = operators.subList(0, opIndex)
        val validNumbers = numbers.subList(0, opIndex+1)
        var leftResult = solveEquation(validNumbers, validOperators)
        var mod = leftResult % min
        // if remainder is 0 no calculations are performed
        if (mod == 0) {
            return
        } else if (validOperators.contains('*')) {
            // if '*' is present on left. It is moved to equation's end with it's corresponding value
            val mulIndex = operators.indexOf('*')
            operators.removeAt(mulIndex)
            val removedNum = numbers.removeAt(mulIndex)
            operators.add('*')
            numbers.add(removedNum)
            fixEquation(numbers, operators)
        } else {
            while (true) {
                min = numbers.minOrNull()!!
                leftResult = solveEquation(validNumbers, validOperators)
                mod = leftResult % min
                var found = false
                for ((index, num) in validNumbers.withIndex()) {
                    if ((num - mod) >= 1 && (validOperators.size== 0
                                || (validOperators.size != index && validOperators[index] != '-'))) {
                        numbers[numbers.indexOf(num)] = (num - mod)
                        found = true
                        break
                    } else if ((num + (min - mod)) <= 20 && (index == 0
                                || (validOperators.size != index && validOperators[index-1] != '-'))) {
                        numbers[numbers.indexOf(num)] = (num + (min - mod))
                        found = true
                        break
                    }
                }
                if (found) {
                    // Recursive call. If numbers are correct doesn't loop
                    fixEquation(numbers, operators)
                    break
                } else {
                    val subtractPos = validOperators.indexOf('-')
                    if (subtractPos != -1) {
                        numbers[subtractPos] = (10..20).random()
                        numbers[subtractPos+1] = (1..numbers[subtractPos]).random()
                        fixEquation(numbers, operators)
                        break
                    } else {
                        for ((index, num) in validNumbers.withIndex()) {
                            numbers[index] = (1..20).random()
                        }
                        fixEquation(numbers, operators)
                        break
                    }
                }
            }
        }
    }

    /**
     * solve the equation without manipulating  it and return the result
     *
     * @param numbers selected numbers
     * @param operators selected operators
     * @return solution of the equation
     */
    private fun solveEquation(numbers: MutableList<Int>, operators: MutableList<Char>): Int {
        var total = 0
        for ((count, number) in numbers.withIndex()) {
            if (count == 0) {
                total += number
            } else {
                when (operators[count - 1]) {
                    '+' -> total += number
                    '-' -> total -= number
                    '*' -> total *= number
                    '/' -> total /= number
                }
            }
        }
        return total
    }

    /**
     * Solve the equation and do manipulations if result of sub expressions is greater than 100
     *
     * @param numbers selected numbers
     * @param operators selected operators
     * @return solution of the equation
     */
    private fun solveEquationAndResolve(numbers: MutableList<Int>, operators: MutableList<Char>): Int {
        var total = 0
        var flag: Boolean
        while (true) {
            flag = true
            for ((count, number) in numbers.withIndex()) {
                if (count == 0) {
                    total += number
                } else {
                    when (operators[count - 1]) {
                        '+' -> {
                            if ((total + number) >= 100) {
                                numbers[numbers.indexOf(number)] =  (1..(100-total)).random()
                                flag = false
                                fixEquation(numbers, operators)
                                break
                            }
                            total += number
                        }
                        '-' -> total -= number
                        '*' -> {
                            if ((total * number) >= 100) {
                                if ((100/total) == 0) {
                                    numbers[numbers.indexOf(number)] =  1
                                } else {
                                    numbers[numbers.indexOf(number)] =  (1..(100/total)).random()
                                }
                                flag = false
                                fixEquation(numbers, operators)
                                break
                            }
                            total *= number
                        }
                        '/' -> total /= number
                    }
                }
            }
            if (flag) {
                break
            }
        }
        return total
    }

    /**
     * Called when tapped on 'GREATER' button
     */
    fun onClickGreater(view: View) {
        if (leftEquation!!.total > rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        // Generate a new equation to be displayed
        generateAndAssignEquations(null)
    }

    /**
     * Called when tapped on '==' button
     */
    fun onClickEqual(view: View) {
        if (leftEquation!!.total == rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        generateAndAssignEquations(null)
    }

    /**
     * Called when tapped on 'LESS' button
     */
    fun onClickLess(view: View) {
        if (leftEquation!!.total < rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        generateAndAssignEquations(null)
    }

    /**
     * Create and show the 'Correct' popup. Then dismiss it after 2 seconds
     *
     * @param view view from button click which is used to attach the popupWindow
     */
    private fun showCorrect(view: View) {
        correctCount += 1
        // Stop the current timer and create a new one with extended time if the user scores 5
        // correct answers
        if (correctCount >= 5) {
            targetTime = targetTime?.plus(10000)
            timeLeft = targetTime!! - Calendar.getInstance().timeInMillis
            timer?.cancel()
            timer = object : CountDownTimer(timeLeft!!, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                    findViewById<TextView>(R.id.time).text = createTimeString(millisUntilFinished)
                }
                override fun onFinish() {
                    showResults()
                }
            }.start()
            correctCount = 0
        }
        totCorrect += 1
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.correct_feedback, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        val handler = Handler(Looper.getMainLooper())
        // Set to dismiss after 2 seconds
        val myRunnable = Runnable {
            popupWindow.dismiss()
        }
        handler.postDelayed(myRunnable, 2000)
        // if dismissed manually existing auto dismiss is removed to save performance
        popupWindow.setOnDismissListener {
            handler.removeCallbacksAndMessages(null)
        }
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        // Make the popup dismissible by touch
        popupView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                }
            }
            popupWindow.dismiss()
            true
        }
    }

    /**
     * Create and show the 'Incorrect' popup. Then dismiss it after 2 seconds
     *
     * @param view view from button click which is used to attach the popupWindow
     */
    private fun showIncorrect(view: View) {
        totIncorrect += 1
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.incorrect_feedback, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        val handler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            popupWindow.dismiss()
        }
        handler.postDelayed(myRunnable, 2000)
        popupWindow.setOnDismissListener {
            handler.removeCallbacksAndMessages(null)
        }
        val solution = when {
            (leftEquation!!.total > rightEquation!!.total) -> "GREATER"
            (leftEquation!!.total < rightEquation!!.total) -> "LESS"
            else -> "=="
        }
        popupWindow.contentView.findViewById<TextView>(R.id.correct_answer).text = solution
        popupWindow.contentView.findViewById<TextView>(R.id.left_eq).text = leftEquation!!.equation
        popupWindow.contentView.findViewById<TextView>(R.id.left_val).text = leftEquation!!.total.toString()
        popupWindow.contentView.findViewById<TextView>(R.id.right_eq).text = rightEquation!!.equation
        popupWindow.contentView.findViewById<TextView>(R.id.right_val).text = rightEquation!!.total.toString()
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                }
            }
            popupWindow.dismiss()
            true
        }
    }

    /**
     * Called when the timer comes to 0.
     * Start a new activity with results from this activity. As this activity is defined as
     * 'android:noHistory="true"' new activity becomes the immediate successor of the MainActivity.
     * Hence this activity will be inaccessible from the new activity via back button
     */
    private fun showResults() {
        val intent = Intent(this, SummaryActivity::class.java)
        intent.putExtra(correctTotConst, totCorrect)
        intent.putExtra(incorrectTotConst, totIncorrect)
        startActivity(intent)
    }

}

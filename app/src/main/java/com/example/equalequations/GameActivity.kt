package com.example.equalequations

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {

    class DataObj(val equation: String, var total: Int)

    private var leftEquation:DataObj? = null
    private var rightEquation:DataObj? = null

    private val leftEqConst = "left_eq"
    private val leftValConst = "left_val"
    private val rightEqConst = "right_eq"
    private val rightValConst = "right_val"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        generateAndAssignEquations(savedInstanceState)
    }

    private fun generateAndAssignEquations(savedInstanceState: Bundle?) {
        val tempLeftEq = savedInstanceState?.getString(leftEqConst)
        val tempLeftVal = savedInstanceState?.getInt(leftValConst)
        leftEquation = if (tempLeftEq != null && tempLeftVal != null) {
            DataObj(tempLeftEq, tempLeftVal)
        } else {
            generateEquation()
        }
        val textViewLeft: TextView = findViewById(R.id.leftEquation)
        textViewLeft.text = leftEquation!!.equation

        val tempRightEq = savedInstanceState?.getString(rightEqConst)
        val tempRightVal = savedInstanceState?.getInt(rightValConst)
        rightEquation = if (tempRightEq != null && tempRightVal != null) {
            DataObj(tempRightEq, tempRightVal)
        } else {
            generateEquation()
        }
        val textViewRight: TextView = findViewById(R.id.rightEquation)
        textViewRight.text = rightEquation!!.equation
    }

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
    }

    private fun generateEquation(): DataObj {
        val operators = mutableListOf('+', '-', '*', '/')
        val selectedNumbers = mutableListOf<Int>()
        val selectedOperators = mutableListOf<Char>()
        val termCount = (1..4).random()
        for (term in (1..termCount)) {
            selectedNumbers.add((1..20).random())
            if (term != termCount) {
                selectedOperators.add(operators.removeAt((0 until operators.size).random()))
            }
        }
        fixEquation(selectedNumbers, selectedOperators)
        var equation = ""
        val total = solveEquationAndResolve(selectedNumbers, selectedOperators)
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

    private fun fixEquation(selectedNumbers: MutableList<Int>, selectedOperators: MutableList<Char>) {
        if ('-' in selectedOperators) {
            fixSubtraction(selectedNumbers, selectedOperators)
        }
        if ('/' in selectedOperators) {
            fixDivision(selectedNumbers, selectedOperators)
        }
    }

    private fun fixSubtraction(numbers: MutableList<Int>, operators: MutableList<Char>) {
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

    private fun fixDivision(numbers: MutableList<Int>, operators: MutableList<Char>) {
        val min: Int = numbers.minOrNull()!!
        val opIndex = operators.indexOf('/')
        val tempVal = numbers[opIndex+1]
        numbers[numbers.indexOf(min)] = tempVal
        numbers[opIndex+1] = min
        val validOperators = operators.subList(0, opIndex)
        val validNumbers = numbers.subList(0, opIndex+1)
        val leftResult = solveEquation(validNumbers, validOperators)
        val mod = leftResult % min
        if (mod == 0) {
            return
        } else if (validOperators.contains('*')) {
            val mulIndex = operators.indexOf('*')
            operators.removeAt(mulIndex)
            val removedNum = numbers.removeAt(mulIndex)
            operators.add('*')
            numbers.add(removedNum)
            fixEquation(numbers, operators)
        } else {
            while (true) {
                var found = false
                for ((index, num) in validNumbers.withIndex()) {
                    if ((num - mod) >= 1 && (validOperators.size != index && validOperators[index] != '-')) {
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
                    break
                } else {
                    val subtractPos = validOperators.indexOf('-')
                    if (subtractPos != -1) {
                        numbers[subtractPos] = (10..20).random()
                        numbers[subtractPos+1] = (1..numbers[subtractPos]).random()
                    } else {
                        for ((index, num) in numbers.withIndex()) {
                            numbers[index] = (1..20).random()
                        }
                    }
                }
            }
        }
    }

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

    private fun solveEquationAndResolve(numbers: MutableList<Int>, operators: MutableList<Char>): Int {
        var total = 0
        var flag: Boolean
        Log.e("GHX", "$numbers || $operators")
        while (true) {
            flag = true
            for ((count, number) in numbers.withIndex()) {
                if (count == 0) {
                    total += number
                } else {
                    when (operators[count - 1]) {
                        '+' -> {
                            if ((total + number) > 100) {
                                numbers[numbers.indexOf(number)] =  (1..(100-total)).random()
                                flag = false
                                fixEquation(numbers, operators)
                                break
                            }
                            total += number
                        }
                        '-' -> total -= number
                        '*' -> {
                            if ((total * number) > 100) {
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

    fun onClickGreater(view: View) {
        if (leftEquation!!.total > rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        generateAndAssignEquations(null)
    }

    fun onClickEqual(view: View) {
        if (leftEquation!!.total == rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        generateAndAssignEquations(null)
    }

    fun onClickLess(view: View) {
        if (leftEquation!!.total < rightEquation!!.total) {
            showCorrect(view)
        } else {
            showIncorrect(view)
        }
        generateAndAssignEquations(null)
    }

    private fun showCorrect(view: View) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.correct_feedback, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        val handler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            Log.e("GHX", "Runnable EXECUTED")
            popupWindow.dismiss()
        }
        handler.postDelayed(myRunnable, 2000)

        popupWindow.setOnDismissListener {
            handler.removeCallbacksAndMessages(null)
        }

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

    private fun showIncorrect(view: View) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.incorrect_feedback, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        val handler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            Log.e("GHX", "Runnable EXECUTED")
            popupWindow.dismiss()
        }
        handler.postDelayed(myRunnable, 2000)
        popupWindow.setOnDismissListener {
            handler.removeCallbacksAndMessages(null)
        }
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

}

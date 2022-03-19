package com.example.equalequations

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {

    class DataObj(val equation: String, var total: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        generateLeftEquation()
        generateRightEquation()
    }

    private fun generateLeftEquation() {
        val textView: TextView = findViewById(R.id.leftEquation)
        val equation = generateEquation()
        textView.text = equation.equation
        Toast.makeText(this, "LEFT - " + equation.total, Toast.LENGTH_SHORT).show()
        Log.e("GHX", "--------------------------------------------")
    }
    private fun generateRightEquation() {
        val textView: TextView = findViewById(R.id.rightEquation)
        val equation = generateEquation()
        textView.text = equation.equation
        Toast.makeText(this, "RIGHT - " + equation.total, Toast.LENGTH_SHORT).show()
    }

    private fun generateEquation(): DataObj {
        /*val operators = arrayOf('+', '-', '*', '/')
        var equation = ""
        var total = 0
        val termCount = (1..4).random()
        for (term in (1..termCount)) {
            var operator = 'x'
            if (term != 1) {
                val operatorPicker = (0..3).random()
                operator = operators[operatorPicker]
                equation += operator
            }

            var number = (1..20).random()

            when (operator) {
                'x' -> total += number
                '+' -> total += number
                '-' -> total -= number
                '*' -> total *= number
                '/' -> total /= fixDivision(total, number)
            }

            equation += number

            if (term > 1 && termCount > 2 && term != termCount) {
                equation = "($equation)"
            }
        }*/

        val operators = mutableListOf('+', '-', '*', '/')
        var selectedNumbers = mutableListOf<Int>()
        var selectedOperators = mutableListOf<Char>()

        var equation = ""
        var total = 0

        val termCount = (1..4).random()
        for (term in (1..termCount)) {
            selectedNumbers.add((1..20).random())
            if (term != termCount) {
                selectedOperators.add(operators.removeAt((0 until operators.size).random()))
            }
        }
        if ('-' in selectedOperators) {
            Log.e("GHX", "Before Sub - $selectedNumbers || $selectedOperators")
            sortSubtraction(selectedNumbers, selectedOperators)
            Log.e("GHX", "After Sub - $selectedNumbers || $selectedOperators")
        }
        if ('/' in selectedOperators) {
            Log.e("GHX", "Before Div - $selectedNumbers || $selectedOperators")
            sortDivision(selectedNumbers, selectedOperators)
            Log.e("GHX", "After Div - $selectedNumbers || $selectedOperators")
        }

        Log.e("GHX", "Final - $selectedNumbers || $selectedOperators")

        for ((index, num) in selectedNumbers.withIndex()) {
            equation += if (index != selectedNumbers.lastIndex) {
                "$num${selectedOperators[index]}"
            } else {
                "$num"
            }
        }

        return DataObj(equation, total)
    }

    private fun sortSubtraction(numbers: MutableList<Int>, operators: MutableList<Char>) {
        Log.e("GHX", ">>In SUb - $numbers || $operators")
        val maxPos = numbers.indexOf(numbers.maxOrNull()!!)
        val opIndex = operators.indexOf('-')
        val tempVal1 = numbers[opIndex]
        numbers[opIndex] = numbers[maxPos]
        numbers[maxPos] = tempVal1
        val tempVal2 = numbers[opIndex+1]
        var secondMaxPos = numbers.lastIndex
        for ((index, num) in numbers.withIndex()) {
            if (num > numbers[secondMaxPos] && index != opIndex) {
                secondMaxPos = index;
            }
        }
        numbers[opIndex+1] = numbers[secondMaxPos]
        numbers[secondMaxPos] = tempVal2
        Log.e("GHX", ">>Out Sub - $numbers || $operators")
    }

    private fun sortDivision(numbers: MutableList<Int>, operators: MutableList<Char>) {
        Log.e("GHX", ">>In Div - $numbers || $operators")
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
            if ('-' in operators) {
                sortSubtraction(numbers, operators)
            }
            sortDivision(numbers, operators)
        } else {
            while (true) {
                var found = false
                for ((index, num) in validNumbers.withIndex()) {
                    if ((num - mod) >= 1 && (validOperators.isEmpty() || validOperators[index] != '-')) {
                        numbers[numbers.indexOf(num)] = (num - mod)
                        found = true
                        break
                    } else if ((num + (min - mod)) <= 20 && (index == 0
                                || (validOperators.isEmpty() || validOperators[index-1] != '-'))) {
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
        Log.e("GHX", ">>Out Div - $numbers || $operators")
    }

    private fun solveEquation(numbers: MutableList<Int>, operators: MutableList<Char>): Int {
        var total = 0
        for ((count, number) in numbers.withIndex()) {
            if (count == 0) {
                total += number
            } else {
                when (operators[count-1]) {
                    '+' -> total += number
                    '-' -> total -= number
                    '*' -> total *= number
                    '/' -> total /= number
                }
            }
        }
        return total
    }
}

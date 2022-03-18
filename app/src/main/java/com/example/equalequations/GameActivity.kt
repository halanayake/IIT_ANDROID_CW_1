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
            sortSubtraction(selectedNumbers, selectedOperators)
        }
        if ('/' in selectedOperators) {
            sortDivision(selectedNumbers, selectedOperators)
        }
        return DataObj(equation, total)
    }

    private fun sortSubtraction(numbers: MutableList<Int>, operators: MutableList<Char>) {
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
    }

    private fun sortDivision(numbers: MutableList<Int>, operators: MutableList<Char>) {
        Log.e("GHX", "A - $numbers | $operators")
        val min: Int = numbers.minOrNull()!!
        val opIndex = operators.indexOf('/')
        val tempVal = numbers[opIndex+1]
        numbers[numbers.indexOf(min)] = tempVal
        numbers[opIndex+1] = min
        Log.e("GHX", "B - $numbers | $operators")
        val leftResult = solveEquation(numbers.subList(0, opIndex+1), operators.subList(0, opIndex))
        val mod = leftResult % min
        if (mod == 0) {
            return
        }

        Log.e("GHX", "MOD - $leftResult | $mod")
    }

    private fun solveEquation(numbers: MutableList<Int>, operators: MutableList<Char>): Int {
        Log.e("GHX", "E - $numbers | $operators")
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

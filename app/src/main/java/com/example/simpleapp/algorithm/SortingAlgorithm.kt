package com.example.simpleapp.algorithm

import com.example.simpleapp.data.singleElement

fun sortingAlgorithm(char: String): String
{
    //loop pool для првоерки того что соритровка по алфавиту с одинаковым весом работает гарантированно
    val primaryText: String = "Primary text for a sorting algorithm which covers at least half " +
            "of the alphabet and consists of enough characters to be considered " +
            "a good testing sample loop pool"

    val listOfEntries = mutableListOf<singleElement>()

    for (word in primaryText.split(" "))
    {
        val weight = word.count{it == char.lowercase().firstOrNull()}
        listOfEntries.add(singleElement(word, weight))
    }

    //отсортированный лист по возрастанию веса и алфавитного порядка и приведенный к 1 элементу
    val weightSortList = listOfEntries.sortedWith( compareBy<singleElement>{it.weight}
        .thenBy { it.word })
        .map{it.word}

    return weightSortList.joinToString()
}

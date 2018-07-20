package com.bitvale.fabdialogdemo

/**
 * Created by Alexander Kolpakov on 17.07.2018
 */
object DataProvider {

    fun getData(): List<Lang> {
        val data = ArrayList<Lang>()
        data.add(Lang(0, "Kotlin", "2018-03-22 10:57:41", 232))
        data.add(Lang(1, "Android", "2017-01-16 10:57:41", 132))
        data.add(Lang(2, "Python", "2018-04-17 10:57:41", 156))
        data.add(Lang(3, "C++", "2018-06-15 10:57:41", 89))
        data.add(Lang(4, "C#", "2018-02-16 10:57:41", 100))
        data.add(Lang(5, "JavaScript", "2018-05-19 10:57:41", 120))
        data.add(Lang(6, "Html", "2018-05-21 10:57:41", 185))
        data.add(Lang(7, "PHP", "2018-04-16 10:57:41", 257))
        data.add(Lang(8, "Ruby", "2018-04-17 10:57:41", 189))
        data.add(Lang(9, "Swift", "2018-07-16 10:57:41", 145))
        data.add(Lang(10, "Java", "2017-05-24 10:57:41", 178))
        return data
    }

    data class Lang(val id: Int, val name: String, val date: String, val questions: Int)
}
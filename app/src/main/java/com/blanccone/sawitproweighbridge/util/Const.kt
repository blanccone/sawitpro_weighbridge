package com.blanccone.sawitproweighbridge.util

import com.blanccone.core.model.local.Menu
import com.blanccone.core.R

object Const {
    fun homeMenu() : List<Menu> {
        return listOf(
            Menu("tickets123","Tickets", R.drawable.ic_ticket_24),
            Menu("results456","Weighment Results", R.drawable.ic_result_24)
        )
    }
}
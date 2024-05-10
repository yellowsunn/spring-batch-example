package com.yellowsunn.springbatchexample.domain

import java.time.ZonedDateTime

class Customer2(
    val fullName: String,
) {
    var id: Long = 0L
    val createdAt: ZonedDateTime = ZonedDateTime.now()
}

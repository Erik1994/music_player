package com.example.myspotify.util

class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

        fun <T> loading(data: T?) = Resource(Status.LODAING, data, null)

        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LODAING
}
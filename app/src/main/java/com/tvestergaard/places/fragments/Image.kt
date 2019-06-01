package com.tvestergaard.places.fragments

import java.io.File
import java.io.Serializable

data class Image(val file: File, var selected: Boolean) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other !is Image)
            return false

        return other.file.absolutePath == file.absolutePath
    }


    override fun toString(): String = file.absolutePath
    override fun hashCode(): Int = file.hashCode()
}
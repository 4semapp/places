package com.tvestergaard.places.fragments

import java.io.File
import java.io.Serializable

data class DiskImage(val file: File, var selected: Boolean) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other !is DiskImage)
            return false

        return other.file.absolutePath == file.absolutePath
    }


    override fun toString(): String = file.absolutePath
    override fun hashCode(): Int = file.hashCode()
}
package com.martmists.allthebots.entities.mcquery


import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * Contains various utility methods for manipulating bytes and byte arrays
 *
 * @author Ryan McCann
 */
object ByteUtils {
    /**
     * Creates and returns a new array with the values of the original from index `a` to index `b`
     * and of size `(b-a)`.
     * @param in input array
     * @param a first index
     * @param b last index
     * @return a new array based on the desired range of the input
     */
    fun subarray(`in`: ByteArray, a: Int, b: Int): ByteArray {
        if (b - a > `in`.size) return `in`// TODO better error checking

        val out = ByteArray(b - a + 1)

        for (i in a..b) {
            out[i - a] = `in`[i]
        }
        return out
    }

    /**
     * Functions similarly to the standard java `String.trim()` method (except that null bytes (0x00),
     * instead of whitespace, are stripped from the beginning and end). If the input array alread has no leading/trailing null bytes,
     * is returned unmodified.
     *
     * @param arr the input array
     * @return an array without any leading or trailing null bytes
     */
    fun trim(arr: ByteArray): ByteArray {
        if (arr[0].toInt() != 0 && arr[arr.size].toInt() != 0) return arr //return the input if it has no leading/trailing null bytes

        var begin = 0
        var end = arr.size
        for (i in arr.indices)
        // find the first non-null byte
        {
            if (arr[i].toInt() != 0) {
                begin = i
                break
            }
        }
        for (i in arr.indices.reversed())
        //find the last non-null byte
        {
            if (arr[i].toInt() != 0) {
                end = i
                break
            }
        }

        return subarray(arr, begin, end)
    }

    /**
     * Spits the input array into separate byte arrays. Works similarly to `String.split()`, but always splits on a null byte (0x00).
     * @param input the input array
     * @return a new array of byte arrays
     */
    fun split(input: ByteArray): Array<ByteArray> {
        val temp = ArrayList<ByteArray>()

        val output: Array<ByteArray>

        var index_cache = 0
        for (i in input.indices) {
            if (input[i].toInt() == 0x00) {
                //				output[out_index++] = subarray(input, index_cache, i-1); //store the array from the last null byte to the current one
                val b = subarray(input, index_cache, i - 1)
                temp.add(b)
                index_cache = i + 1//note, this is the index *after* the null byte
            }
        }
        //get the remaining part
        if (index_cache != 0)
        //prevent duplication if there are no null bytes
        {
            //			output[out_index] = subarray(input, index_cache, input.length-1);
            val b = subarray(input, index_cache, input.size - 1)
            temp.add(b)
        }

        output = Array(temp.size) { ByteArray(input.size) }
        for (i in temp.indices) {
            output[i] = temp[i]
        }

        return output
    }

    /**
     * Creates an new array of length `arr+amount`, identical to the original, `arr`,
     * except with `amount` null bytes (0x00) padding the end.
     * @param arr the input array
     * @param amount the amount of byte to pad
     * @return a new array, identical to the original, with the desired padding
     */
    fun padArrayEnd(arr: ByteArray, amount: Int): ByteArray {
        val arr2 = ByteArray(arr.size + amount)
        for (i in arr.indices) {
            arr2[i] = arr[i]
        }
        for (i in arr.size until arr2.size) {
            arr2[i] = 0
        }
        return arr2
    }

    fun bytesToShort(b: ByteArray): Short {
        val buf = ByteBuffer.wrap(b, 0, 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        return buf.short
    }

    //Big endian !!
    fun intToBytes(`in`: Int): ByteArray {
        val b: ByteArray
        b = byteArrayOf((`in`.ushr(24) and 0xFF).toByte(), (`in`.ushr(16) and 0xFF).toByte(), (`in`.ushr(8) and 0xFF).toByte(), (`in`.ushr(0) and 0xFF).toByte())
        return b
    }

    fun bytesToInt(`in`: ByteArray): Int {
        return ByteBuffer.wrap(`in`).int //note: big-endian by default
    }

}
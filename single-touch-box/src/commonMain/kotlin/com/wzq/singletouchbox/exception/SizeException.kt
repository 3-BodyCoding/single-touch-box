package com.wzq.singletouchbox.exception

class MinInitContentSizeException : Exception() {
    override val message: String?
        get() = "Initial width and height needs to be larger than the control button size"
}

class MinZoomException : Exception() {
    override val message: String?
        get() = "Minimum scaling value needs to be greater than 0"
}
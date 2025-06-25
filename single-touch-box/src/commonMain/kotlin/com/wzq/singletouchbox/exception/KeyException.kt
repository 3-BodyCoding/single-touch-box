package com.wzq.singletouchbox.exception

class KeyBlankException : Exception() {
    override val message: String?
        get() = "key cannot be blank"
}

class KeyUniqueException : Exception() {
    override val message: String?
        get() = "key already exists, please keep the key unique"
}
package net.dongliu.apk.parser.utils

@Deprecated("move to kotlin sdk", ReplaceWith("first"))
val <T,R> Pair<T,R>.left get() = first
@Deprecated("move to kotlin sdk", ReplaceWith("second"))
val <T,R> Pair<T,R>.right get() = second
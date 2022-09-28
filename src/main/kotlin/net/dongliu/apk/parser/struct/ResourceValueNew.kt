package net.dongliu.apk.parser.struct

import net.dongliu.apk.parser.struct.ResValue.ResType
import net.dongliu.apk.parser.struct.resource.*
import java.util.*


internal interface IntResourceValue:ResourceValueNew<Int> {

}

internal abstract class AbstractResourceValue <T:Any?> constructor(
    override val value: T
) :ResourceValueNew<T>

sealed interface ResourceValueNew<T:Any?> {

    val value:T
    fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String
    companion object {
        internal inline fun <T:Any> invoke(value: T,crossinline block:(ResourceValueNew<T>,resourceTable: ResourceTable?, locale: Locale?)-> String = {it,_,_ -> it.value.toString()}):ResourceValueNew<T>
            = object : AbstractResourceValue<T>(value) {
                override fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String
                    = block(this,resourceTable, locale)
        }
        internal inline fun invoke(value: Int,crossinline block:(ResourceValueNew<Int>,resourceTable: ResourceTable?, locale: Locale?)-> String= {it,_,_ -> it.value.toString()})
            = object : IntResourceValue {
                override val value = value
                override fun toStringValue(resourceTable: ResourceTable?, locale: Locale?): String
                    = block(this,resourceTable, locale)

        }
        fun <T:Any> findType(dataType: Short,value:T): ResourceValueNew<out T> = when (dataType) {
            ResType.INT_DEC -> invoke(value)
            ResType.INT_HEX -> invoke<Int>(value as Int) { it, _, _ ->
                "0x" + Integer.toHexString(it.value)
            }
            ResType.FLOAT -> invoke(value)
            ResType.STRING -> TODO()
            ResType.REFERENCE -> TODO()
            ResType.INT_BOOLEAN -> invoke(value is Int && value!=0)
            ResType.NULL -> invoke("")
//
//            ResType.FLOAT -> ResourceValue.float(buffer.float)
//            ResValue.ResType.STRING -> {
//                val strRef = buffer.int
//                if (strRef >= 0) {
//                    ResourceValue.string(strRef, stringPool)
//                } else {
//                    null
//                }
//            }
//            ResValue.ResType.REFERENCE -> ResourceValue.reference(buffer.int)
//            ResValue.ResType.INT_BOOLEAN -> ResourceValue.bool(buffer.int)
//            ResValue.ResType.NULL -> ResourceValue.nullValue()
//            ResValue.ResType.INT_COLOR_RGB8, ResValue.ResType.INT_COLOR_RGB4 -> ResourceValue.rgb(
//                buffer.int,
//                6
//            )
//
//            ResValue.ResType.INT_COLOR_ARGB8, ResValue.ResType.INT_COLOR_ARGB4 -> ResourceValue.rgb(
//                buffer.int,
//                8
//            )
//
//            ResValue.ResType.DIMENSION -> ResourceValue.dimension(buffer.int)
//            ResValue.ResType.FRACTION -> ResourceValue.fraction(buffer.int)
//            else -> ResourceValue.raw(buffer.int, dataType)
//
//

            else -> invoke(value) {it,_,_->
                it.value.toString()
            }
        } as ResourceValueNew<out T>
//        fun decimal(value: Int): ResourceValueNew<Int> = invoke(value) { it, _, _->
//            it.value.toString()
//        }
//        fun hexadecimal(value: Int): ResourceValueNew<Int> = invoke(value) { it, _, _->
//            "0x" + Integer.toHexString(it.value)
//        }
//
//        fun bool(value: Int): ResourceValue {
//            return ResourceValue.BooleanResourceValue(value)
//        }
//
//        fun float(float: Float):ResourceValue
//                = ResourceValue.FloatResourceValue(value)
//
//        fun string(value: Int, stringPool: StringPool?): ResourceValue {
//            return ResourceValue.StringResourceValue(value, stringPool)
//        }
//
//        fun reference(value: Int): ResourceValue {
//            return ResourceValue.ReferenceResourceValue(value)
//        }
//
//        fun nullValue(): ResourceValue {
//            return ResourceValue.NullResourceValue.instance
//        }
//
//        fun rgb(value: Int, len: Int): ResourceValue {
//            return ResourceValue.RGBResourceValue(value, len)
//        }
//
//        fun dimension(value: Int): ResourceValue {
//            return ResourceValue.DimensionValue(value)
//        }
//
//        fun fraction(value: Int): ResourceValue {
//            return ResourceValue.FractionValue(value)
//        }
//
//        fun raw(value: Int, type: Short): ResourceValue {
//            return ResourceValue.RawValue(value, type)
//        }
    }
}


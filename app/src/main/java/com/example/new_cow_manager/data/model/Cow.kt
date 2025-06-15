package com.example.new_cow_manager.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.datetime.*
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Parcelize
data class Cow(
    val id: String = "",
    val cowNumber: String = "",
    var pregnant: Boolean = false,
    var pregnancyDuration: Int = 0,
    var pregnancyMonths: Int = 0,
    var doNotMilk: Boolean = false,
    @get:JvmName("getInseminationDateLong")
    val inseminationDate: @RawValue LocalDate? = null,
    @get:JvmName("getBirthDateLong")
    val birthDate: @RawValue LocalDate? = null,
    @get:JvmName("getGgpgFirstGLong")
    val ggpgFirstG: @RawValue LocalDate? = null,
    @get:JvmName("getGgpgSecondGLong")
    val ggpgSecondG: @RawValue LocalDate? = null,
    @get:JvmName("getGgpgPLong")
    val ggpgP: @RawValue LocalDate? = null,
    @get:JvmName("getGgpgFinalGLong")
    val ggpgFinalG: @RawValue LocalDate? = null,
    @get:JvmName("getAppliedHormones")
    val appliedHormones: @RawValue Map<String, LocalDate> = emptyMap(),
    val corpusLuteum: Map<String, String> = emptyMap(),
    val corpusRubrum: Map<String, String> = emptyMap(),
    val cysts: Map<String, String> = emptyMap(),
    val follicles: Map<String, String> = emptyMap(),
    val diagnosis: String = "",
    val comment: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable {
    data class GgpgDates(
        val firstG: LocalDate,
        val secondG: LocalDate,
        val p: LocalDate,
        val finalG: LocalDate,
        val needsNotification: Boolean = true
    )

    // This initializer block will ensure doNotMilk is set correctly when the object is created
    init {
        doNotMilk = shouldNotMilk()
    }

    fun updatePregnancyMonths(months: Int) {
        pregnancyMonths = months
        pregnancyDuration = months * 30  // Approximate days in a month
        doNotMilk = shouldNotMilk()
    }

    fun setPregnancyDays(days: Int) {
        pregnancyDuration = days
        pregnancyMonths = days / 30  // Approximate conversion to months
        doNotMilk = shouldNotMilk()
    }

    fun shouldNotMilk(): Boolean {
        return pregnant && pregnancyDuration > 200
    }

    // Firestore serialization helpers
    @get:Exclude
    private val inseminationDateLong: Long?
        get() = inseminationDate?.toEpochDays()?.toLong()

    @get:Exclude
    private val birthDateLong: Long?
        get() = birthDate?.toEpochDays()?.toLong()

    @get:Exclude
    private val ggpgFirstGLong: Long?
        get() = ggpgFirstG?.toEpochDays()?.toLong()

    @get:Exclude
    private val ggpgSecondGLong: Long?
        get() = ggpgSecondG?.toEpochDays()?.toLong()

    @get:Exclude
    private val ggpgPLong: Long?
        get() = ggpgP?.toEpochDays()?.toLong()

    @get:Exclude
    private val ggpgFinalGLong: Long?
        get() = ggpgFinalG?.toEpochDays()?.toLong()

    @get:Exclude
    private val appliedHormonesLongMap: Map<String, Long>
        get() = appliedHormones.mapValues { it.value.toEpochDays().toLong() }

    // Companion object for creating Cow objects from Firestore data
    companion object {
        fun fromFirestore(data: Map<String, Any>): Cow {
            return Cow(
                id = data["id"] as? String ?: "",
                cowNumber = data["cowNumber"] as? String ?: "",
                pregnant = data["pregnant"] as? Boolean ?: false,
                pregnancyDuration = (data["pregnancyDuration"] as? Long)?.toInt() ?: 0,
                pregnancyMonths = (data["pregnancyMonths"] as? Long)?.toInt() ?: 0,
                doNotMilk = data["doNotMilk"] as? Boolean ?: false,
                inseminationDate = (data["inseminationDateLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                birthDate = (data["birthDateLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                ggpgFirstG = (data["ggpgFirstGLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                ggpgSecondG = (data["ggpgSecondGLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                ggpgP = (data["ggpgPLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                ggpgFinalG = (data["ggpgFinalGLong"] as? Long)?.let {
                    LocalDate.fromEpochDays(it.toInt())
                },
                appliedHormones = (data["appliedHormonesMap"] as? Map<String, Long>)?.mapValues {
                    LocalDate.fromEpochDays(it.value.toInt())
                } ?: emptyMap(),
                corpusLuteum = (data["corpusLuteum"] as? Map<String, String>) ?: emptyMap(),
                corpusRubrum = (data["corpusRubrum"] as? Map<String, String>) ?: emptyMap(),
                cysts = (data["cysts"] as? Map<String, String>) ?: emptyMap(),
                follicles = (data["follicles"] as? Map<String, String>) ?: emptyMap(),
                diagnosis = data["diagnosis"] as? String ?: "",
                comment = data["comment"] as? String ?: "",
                lastUpdated = data["lastUpdated"] as? Long ?: System.currentTimeMillis()
            )
        }
    }

    // Convert to Firestore data
    fun toFirestore(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "cowNumber" to cowNumber,
            "pregnant" to pregnant,
            "pregnancyDuration" to pregnancyDuration,
            "pregnancyMonths" to pregnancyMonths,
            "doNotMilk" to shouldNotMilk(), // Always calculate the current value
            "inseminationDateLong" to inseminationDateLong,
            "birthDateLong" to birthDateLong,
            "ggpgFirstGLong" to ggpgFirstGLong,
            "ggpgSecondGLong" to ggpgSecondGLong,
            "ggpgPLong" to ggpgPLong,
            "ggpgFinalGLong" to ggpgFinalGLong,
            "appliedHormonesMap" to appliedHormonesLongMap,
            "corpusLuteum" to corpusLuteum,
            "corpusRubrum" to corpusRubrum,
            "cysts" to cysts,
            "follicles" to follicles,
            "diagnosis" to diagnosis,
            "comment" to comment,
            "lastUpdated" to lastUpdated
        )
    }

    fun calculateGgpgDates(firstGDate: LocalDate): GgpgDates {
        val secondG = firstGDate.plus(DatePeriod(days = 7))
        // For 56 hours, we'll add 2 days and then 8 hours using Instant calculations
        val pInstant = secondG.atStartOfDayIn(TimeZone.UTC)
            .plus(48.hours)
            .plus(8.hours)
        val p = pInstant.toLocalDateTime(TimeZone.UTC).date
        val finalG = p.plus(DatePeriod(days = 2))
        return GgpgDates(
            firstG = firstGDate,
            secondG = secondG,
            p = p,
            finalG = finalG,
            needsNotification = true
        )
    }
}

data class CowExamination(
    val id: String = "",
    val cowId: String = "",
    val date: Long = System.currentTimeMillis(),
    val previousState: Cow? = null,
    val newState: Cow
)
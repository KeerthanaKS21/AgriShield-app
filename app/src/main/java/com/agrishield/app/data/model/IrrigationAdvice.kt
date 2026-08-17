package com.agrishield.app.data.model

data class IrrigationAdvice(
    val action: IrrigationAction = IrrigationAction.NORMAL,
    val waterVolumeLitersPerSqm: Double = 4.5,
    val reasonEn: String = "",
    val reasonTa: String = "",
    val rainExpectedNext12h: Boolean = false,
    val nextIrrigationWindow: String = "Tomorrow Early Morning (6:00 AM)",
    val generatedAt: Long = System.currentTimeMillis()
)

enum class IrrigationAction {
    NORMAL,
    REDUCE,
    HOLD_DO_NOT_IRRIGATE,
    INCREASE
}

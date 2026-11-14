package com.vanguard.grc.domain.risk

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Risk scoring primitives used by the Risk Assessment Calculator, the Risk
 * Heatmap, the Risk Treatment Planner and the third-party assessment tool.
 */
object RiskEngine {

    /** Standard five-point likelihood scale. */
    val likelihoodLabels = listOf(
        "Rare" to "Not expected to occur in the life of the organisation",
        "Unlikely" to "Could occur, but not expected",
        "Possible" to "Might occur during normal operating periods",
        "Likely" to "Expected to occur during normal operating periods",
        "Almost certain" to "Expected to occur more often than not"
    )

    /** Standard five-point impact scale spanning financial, operational, regulatory and reputational dimensions. */
    val impactLabels = listOf(
        "Negligible" to "Immaterial loss; no customer, regulatory or reputational consequence",
        "Minor" to "Limited loss; local remediation; no regulatory notification",
        "Moderate" to "Material financial loss; customer impact; possible supervisory interest",
        "Major" to "Significant financial loss; reportable incident; material regulatory action",
        "Severe" to "Existential or near-existential loss; licence or trust impact"
    )

    data class RiskScore(
        val likelihood: Int,
        val impact: Int,
        val inherentScore: Int,
        val controlEffectiveness: Int,
        val residualScore: Double,
        val inherentRating: String,
        val residualRating: String,
        val exposureReductionPercent: Int
    )

    /**
     * Computes inherent and residual risk.
     *
     * @param controlEffectiveness percentage of the control objective that is
     *        actually operating (0..100). This is deliberately a *measured*
     * value rather than a confidence score - it is what an auditor would test.
     */
    fun score(
        likelihood: Int,
        impact: Int,
        controlEffectiveness: Int = 0
    ): RiskScore {
        val l = likelihood.coerceIn(1, 5)
        val i = impact.coerceIn(1, 5)
        val inherent = l * i
        val ce = controlEffectiveness.coerceIn(0, 100)
        // Residual likelihood and impact are each reduced by the effectiveness factor.
        val residualLikelihood = l * (1.0 - ce / 100.0)
        val residualImpact = i * (1.0 - ce / 100.0)
        val residual = residualLikelihood * residualImpact
        return RiskScore(
            likelihood = l,
            impact = i,
            inherentScore = inherent,
            controlEffectiveness = ce,
            residualScore = residual,
            inherentRating = rate(inherent),
            residualRating = rateDouble(residual),
            exposureReductionPercent = if (inherent == 0) 0 else ((1 - residual / inherent) * 100).roundToInt()
        )
    }

    /** Standard 5x5 rating bands. */
    fun rate(score: Int): String = when (score) {
        in 1..4 -> "Low"
        in 5..9 -> "Moderate"
        in 10..14 -> "High"
        in 15..19 -> "Very High"
        in 20..25 -> "Extreme"
        else -> "Not rated"
    }

    fun rateDouble(score: Double): String = when {
        score < 1.0 -> "Not rated"
        score < 5.0 -> "Low"
        score < 10.0 -> "Moderate"
        score < 15.0 -> "High"
        score < 20.0 -> "Very High"
        else -> "Extreme"
    }

    /** Colour band index for heatmap rendering: 0 low .. 4 extreme. */
    fun bandIndex(score: Double): Int = when {
        score < 4.0 -> 0
        score < 5.0 -> 1
        score < 10.0 -> 2
        score < 15.0 -> 3
        score < 20.0 -> 4
        else -> 5
    }

    // --- Quantitative (FAIR-flavoured) estimation --------------------------------

    data class QuantitativeEstimate(
        val eventFrequency: Double,
        val lossMagnitude: Double,
        val singleLossExpectancy: Double,
        val annualizedLossExpectancy: Double,
        val valueAtRisk: Double,
        val ale: Double,
        val riskScore: Double,
        val tier: String,
        val explanation: String
    )

    /**
     * A deliberately simplified FAIR-style estimate.
     *
     * This is a teaching model, not a financial model. It exists so learners
     * can reason about magnitude of exposure rather than only ordinal scores.
     */
    fun quantify(
        eventFrequency: Double,
        lossMagnitude: Double,
        confidencePct: Int = 80,
        varCost: Double = 0.0
    ): QuantitativeEstimate {
        val freq = eventFrequency.coerceAtLeast(0.0)
        val mag = lossMagnitude.coerceAtLeast(0.0)
        val sle = freq * mag
        val ale = sle * 1.0 // annualised: frequency is already annual
        val varPct = 1.0 - (confidencePct.coerceIn(1, 99) / 100.0)
        val varValue = if (freq == 0.0 || mag == 0.0) 0.0 else ale + (mag * varPct) + varCost
        val score = when {
            ale <= 10_000 -> 5
            ale <= 100_000 -> 15
            ale <= 500_000 -> 30
            ale <= 2_000_000 -> 50
            ale <= 10_000_000 -> 70
            else -> 90
        }
        val tier = when {
            score < 10 -> "Trivial"
            score < 30 -> "Minor"
            score < 50 -> "Moderate"
            score < 70 -> "Significant"
            else -> "Critical"
        }
        val explanation = buildString {
            append("Single loss expectancy = annual event frequency x loss magnitude = ")
            append(format(freq)).append(" x ").append(format(mag)).append(" = ").append(format(sle)).append(".\n")
            append("Annualised loss expectancy (ALE) = ").append(format(ale)).append(".\n")
            append("Value at risk at ").append(confidencePct).append("% confidence = ALE + tail exposure (")
            append(format(mag * varPct)).append(") + variance cost (").append(format(varCost)).append(") = ")
            append(format(varValue)).append(".\n")
            append("Tier: $tier. Treat this as an order-of-magnitude judgement and challenge the inputs with the risk owner.")
        }
        return QuantitativeEstimate(
            eventFrequency = freq,
            lossMagnitude = mag,
            singleLossExpectancy = sle,
            annualizedLossExpectancy = ale,
            valueAtRisk = varValue,
            ale = ale,
            riskScore = score.toDouble(),
            tier = tier,
            explanation = explanation
        )
    }

    // --- Treatment selection -----------------------------------------------------

    enum class TreatmentStrategy(val label: String, val guidance: String) {
        MITIGATE("Mitigate", "Reduce likelihood or impact through additional or improved controls."),
        TRANSFER("Transfer", "Shift financial consequence through insurance, indemnities or contractual risk transfer."),
        AVOID("Avoid", "Remove the activity or exposure entirely, or change the design so the risk cannot arise."),
        ACCEPT("Accept", "Take the risk consciously within the stated appetite, with a named owner and a review date."),
        ESCALATE("Escalate", "Outside the delegated authority of the risk owner; requires an executive decision and recorded rationale.")
    }

    /**
     * Guidance on which treatment options remain credible at each severity.
     */
    fun treatmentOptions(residualScore: Double): List<TreatmentStrategy> {
        val band = bandIndex(residualScore)
        return when {
            band <= 1 -> listOf(
                TreatmentStrategy.ACCEPT, TreatmentStrategy.MITIGATE, TreatmentStrategy.TRANSFER
            )
            band == 2 -> listOf(
                TreatmentStrategy.MITIGATE, TreatmentStrategy.ACCEPT, TreatmentStrategy.TRANSFER, TreatmentStrategy.AVOID
            )
            band == 3 -> listOf(
                TreatmentStrategy.MITIGATE, TreatmentStrategy.AVOID, TreatmentStrategy.ESCALATE, TreatmentStrategy.TRANSFER
            )
            band == 4 -> listOf(
                TreatmentStrategy.MITIGATE, TreatmentStrategy.AVOID, TreatmentStrategy.ESCALATE
            )
            else -> listOf(
                TreatmentStrategy.AVOID, TreatmentStrategy.ESCALATE, TreatmentStrategy.MITIGATE
            )
        }
    }

    // --- Control effectiveness model ----------------------------------------------

    data class ControlEvaluation(
        val designEffective: Boolean,
        val operatingEffective: Boolean,
        val maturityLevel: Int,
        val effectivenessPercent: Int,
        val finding: String
    )

    /**
     * Evaluates a single control. Design and operating effectiveness are
     * assessed separately, mirroring how an assurance team reaches a
     * conclusion.
     */
    fun evaluateControl(
        designEffective: Boolean,
        operatingEffective: Boolean,
        maturityLevel: Int
    ): ControlEvaluation {
        val maturity = maturityLevel.coerceIn(1, 5)
        val base = when (maturity) {
            5 -> 95
            4 -> 85
            3 -> 70
            2 -> 45
            else -> 20
        }
        var percent = base
        if (!designEffective) percent = (percent * 0.5).toInt()
        if (!operatingEffective) percent = (percent * 0.6).toInt()
        percent = percent.coerceIn(0, 100)
        val finding = when {
            !designEffective && !operatingEffective ->
                "The control does not address the risk by design and is not operating. Treat the underlying risk as unmitigated."
            !designEffective ->
                "The control does not address the risk by design, so partial operation does not reduce exposure. Redesign before relying on it."
            !operatingEffective ->
                "The control is properly designed but is not operating consistently. Remediate the operating failure and re-test."
            else ->
                "The control is designed for the risk and is operating as intended. Evidence should be refreshed on the defined cycle."
        }
        return ControlEvaluation(designEffective, operatingEffective, maturity, percent, finding)
    }

    fun format(value: Double): String {
        fun stripTrailingZero(s: String) = if (s.endsWith(".0")) s.dropLast(2) else s
        return when {
            value >= 1_000_000 -> stripTrailingZero(String.format(java.util.Locale.US, "%,.1f", value / 1_000_000)) + "M"
            value >= 1_000 -> stripTrailingZero(String.format(java.util.Locale.US, "%,.1f", value / 1_000)) + "K"
            value == value.toLong().toDouble() -> String.format(java.util.Locale.US, "%,d", value.toLong())
            else -> String.format(java.util.Locale.US, "%.2f", value)
        }
    }

    /** Compounding helper used by loss-growth illustrations. */
    fun compoundGrowth(principal: Double, rate: Double, periods: Int): Double =
        principal * (1 + rate).pow(periods)
}

package com.vanguard.grc.domain.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskEngineTest {

    @Test
    fun `inherent score is likelihood multiplied by impact`() {
        val score = RiskEngine.score(likelihood = 3, impact = 5)
        assertEquals(15, score.inherentScore)
        assertEquals("Very High", score.inherentRating)
    }

    @Test
    fun `inherent and residual scores are bounded by the matrix`() {
        val score = RiskEngine.score(likelihood = 5, impact = 5)
        assertEquals(25, score.inherentScore)
        assertEquals("Extreme", score.inherentRating)
    }

    @Test
    fun `no controls leaves residual equal to inherent`() {
        val score = RiskEngine.score(likelihood = 4, impact = 4, controlEffectiveness = 0)
        assertEquals(16, score.inherentScore)
        assertEquals(16.0, score.residualScore, 0.001)
        assertEquals(0, score.exposureReductionPercent)
    }

    @Test
    fun `full control effectiveness removes exposure`() {
        val score = RiskEngine.score(likelihood = 4, impact = 4, controlEffectiveness = 100)
        assertEquals(0.0, score.residualScore, 0.001)
        assertEquals(100, score.exposureReductionPercent)
    }

    @Test
    fun `partial control effectiveness reduces residual proportionally`() {
        // 50% effectiveness: residual = (4*0.5) * (4*0.5) = 4.0 against inherent 16.
        val score = RiskEngine.score(likelihood = 4, impact = 4, controlEffectiveness = 50)
        assertEquals(4.0, score.residualScore, 0.001)
        assertEquals(75, score.exposureReductionPercent)
    }

    @Test
    fun `out of range inputs are coerced rather than rejected`() {
        val score = RiskEngine.score(likelihood = 99, impact = -4, controlEffectiveness = 500)
        assertEquals(5, score.likelihood)
        assertEquals(1, score.impact)
        assertEquals(100, score.controlEffectiveness)
    }

    @Test
    fun `rating bands follow the standard five by five matrix`() {
        assertEquals("Low", RiskEngine.rate(4))
        assertEquals("Moderate", RiskEngine.rate(5))
        assertEquals("Moderate", RiskEngine.rate(9))
        assertEquals("High", RiskEngine.rate(10))
        assertEquals("High", RiskEngine.rate(14))
        assertEquals("Very High", RiskEngine.rate(15))
        assertEquals("Very High", RiskEngine.rate(19))
        assertEquals("Extreme", RiskEngine.rate(20))
    }

    @Test
    fun `band index maps to heatmap colours`() {
        assertEquals(0, RiskEngine.bandIndex(2.0))
        assertEquals(1, RiskEngine.bandIndex(4.9))
        assertEquals(2, RiskEngine.bandIndex(9.9))
        assertEquals(3, RiskEngine.bandIndex(14.9))
        assertEquals(4, RiskEngine.bandIndex(19.9))
        assertEquals(5, RiskEngine.bandIndex(25.0))
    }

    @Test
    fun `quantified estimate computes loss expectancy`() {
        val estimate = RiskEngine.quantify(
            eventFrequency = 0.2,
            lossMagnitude = 4_500_000.0,
            confidencePct = 80
        )
        assertEquals(900_000.0, estimate.ale, 0.01)
        assertEquals(900_000.0, estimate.singleLossExpectancy, 0.01)
        assertTrue(estimate.tier.isNotBlank())
        assertTrue(estimate.explanation.contains("Single loss expectancy"))
    }

    @Test
    fun `quantified estimate scales with frequency`() {
        val once = RiskEngine.quantify(0.1, 1_000_000.0)
        val twice = RiskEngine.quantify(0.2, 1_000_000.0)
        assertEquals(twice.ale, once.ale * 2, 0.01)
    }

    @Test
    fun `quantified estimate treats zero frequency as no exposure`() {
        val estimate = RiskEngine.quantify(0.0, 10_000_000.0)
        assertEquals(0.0, estimate.ale, 0.001)
        assertEquals(0.0, estimate.valueAtRisk, 0.001)
    }

    @Test
    fun `design failure halves control effectiveness`() {
        val evaluation = RiskEngine.evaluateControl(
            designEffective = false,
            operatingEffective = true,
            maturityLevel = 4
        )
        val designed = RiskEngine.evaluateControl(
            designEffective = true,
            operatingEffective = true,
            maturityLevel = 4
        )
        assertTrue(evaluation.effectivenessPercent < designed.effectivenessPercent)
        assertTrue(evaluation.finding.contains("design"))
    }

    @Test
    fun `operating failure reduces effectiveness further than a maturity shortfall`() {
        val full = RiskEngine.evaluateControl(true, true, 4)
        val notOperating = RiskEngine.evaluateControl(true, false, 4)
        val lowMaturity = RiskEngine.evaluateControl(true, true, 2)
        assertTrue(notOperating.effectivenessPercent < full.effectivenessPercent)
        assertTrue(lowMaturity.effectivenessPercent < full.effectivenessPercent)
    }

    @Test
    fun `treatment options narrow as severity increases`() {
        val low = RiskEngine.treatmentOptions(3.0)
        val extreme = RiskEngine.treatmentOptions(24.0)
        assertTrue(low.contains(RiskEngine.TreatmentStrategy.ACCEPT))
        assertTrue(!extreme.contains(RiskEngine.TreatmentStrategy.ACCEPT))
        assertTrue(extreme.contains(RiskEngine.TreatmentStrategy.ESCALATE))
    }

    @Test
    fun `numbers are formatted for executive reporting`() {
        assertEquals("1.5M", RiskEngine.format(1_500_000.0))
        assertEquals("12K", RiskEngine.format(12_000.0))
        assertEquals("42", RiskEngine.format(42.0))
    }
}

package com.superlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CouponSectionStateTest {

    @Test
    fun single_coupon_keeps_header_and_action_card() {
        val coupons = listOf(
            CouponRecord(
                id = "coupon-1",
                number = "11447830316028",
                remainingBalance = "80",
                balanceLastCheckedAt = "2026-03-16T14:15:18Z",
                lastImportedAt = "2026-03-16T14:07:55Z",
            ),
        )

        val sectionState = buildCouponSectionState(coupons)

        assertNotNull(sectionState.featuredCoupon)
        assertEquals("11447830316028", sectionState.featuredCoupon?.number)
        assertEquals(1, sectionState.actionCoupons.size)
        assertEquals("coupon-1", sectionState.actionCoupons.single().id)
    }

    @Test
    fun multiple_coupons_keep_all_action_cards() {
        val coupons = listOf(
            CouponRecord(id = "coupon-1", number = "111111111"),
            CouponRecord(id = "coupon-2", number = "222222222"),
        )

        val sectionState = buildCouponSectionState(coupons)

        assertEquals("coupon-1", sectionState.featuredCoupon?.id)
        assertEquals(2, sectionState.actionCoupons.size)
        assertEquals(listOf("coupon-1", "coupon-2"), sectionState.actionCoupons.map { it.id })
    }
}
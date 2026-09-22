package com.taskflow.app

import com.taskflow.app.util.Validators
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test fun validEmailsAreAccepted() {
        assertTrue(Validators.isValidEmail("thabo@example.com"))
        assertTrue(Validators.isValidEmail("  sindi.ntuli@varsity.ac.za "))
    }

    @Test fun invalidEmailsAreRejected() {
        listOf("", "abc", "a@b", "a b@c.com", "@x.com", "a@.com").forEach { assertFalse(it, Validators.isValidEmail(it)) }
    }

    @Test fun passwordNeedsLengthLetterAndNumber() {
        assertTrue(Validators.isValidPassword("Passw0rd1"))
        assertFalse(Validators.isValidPassword("short1"))
        assertFalse(Validators.isValidPassword("onlyletters"))
        assertFalse(Validators.isValidPassword("12345678"))
        assertFalse(Validators.isValidPassword("a".repeat(73) + "1"))
    }

    @Test fun passwordsMustMatchExactly() {
        assertTrue(Validators.passwordsMatch("Passw0rd1", "Passw0rd1"))
        assertFalse(Validators.passwordsMatch("Passw0rd1", "passw0rd1"))
    }

    @Test fun nameAndTitleRules() {
        assertTrue(Validators.isValidName("Al"))
        assertFalse(Validators.isValidName(" A "))
        assertTrue(Validators.isValidTitle("Submit assignment"))
        assertFalse(Validators.isValidTitle("   "))
        assertFalse(Validators.isValidTitle("x".repeat(201)))
    }

    @Test fun timeFormat() {
        assertTrue(Validators.isValidTime("16:00"))
        assertFalse(Validators.isValidTime("24:00"))
        assertFalse(Validators.isValidTime("9:5"))
    }
}

package com.sameneed.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

    @Test
    public void testAliasGenerator_positionOne_isCreator() {
        assertEquals("Group Creator", AliasGenerator.generate(1));
    }

    @Test
    public void testAliasGenerator_positionTwo_isMember02() {
        assertEquals("Member 02", AliasGenerator.generate(2));
    }

    @Test
    public void testAliasGenerator_generateForCount_zeroCount_isCreator() {
        assertEquals("Group Creator", AliasGenerator.generateForCount(0));
    }

    @Test
    public void testAliasGenerator_generateForCount_fourMembers_isMember05() {
        assertEquals("Member 05", AliasGenerator.generateForCount(4));
    }

    @Test
    public void testPasswordUtil_hashAndVerify_succeeds() {
        String plain = "TestPassword123";
        String hashed = PasswordUtil.hash(plain);
        assertNotNull(hashed);
        assertNotEquals(plain, hashed);
        assertTrue(PasswordUtil.verify(plain, hashed));
    }

    @Test
    public void testPasswordUtil_wrongPassword_fails() {
        String plain = "CorrectPassword";
        String hashed = PasswordUtil.hash(plain);
        assertFalse(PasswordUtil.verify("WrongPassword", hashed));
    }

    @Test
    public void testPasswordUtil_nullInput_returnsFalse() {
        assertFalse(PasswordUtil.verify(null, "somehash"));
        assertFalse(PasswordUtil.verify("password", null));
    }

    @Test
    public void testDateUtil_validDate_parsed() {
        LocalDate date = DateUtil.parseDate("2027-06-15");
        assertNotNull(date);
        assertEquals(2027, date.getYear());
        assertEquals(6, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    public void testDateUtil_invalidDate_returnsNull() {
        assertNull(DateUtil.parseDate("not-a-date"));
        assertNull(DateUtil.parseDate(null));
        assertNull(DateUtil.parseDate(""));
    }

    @Test
    public void testDateUtil_validTime_parsed() {
        LocalTime time = DateUtil.parseTime("14:30");
        assertNotNull(time);
        assertEquals(14, time.getHour());
        assertEquals(30, time.getMinute());
    }

    @Test
    public void testJsonUtil_serialiseAndDeserialise() {
        TestData data = new TestData("hello", 42);
        String json = JsonUtil.toJson(data);
        assertTrue(json.contains("hello"));
        TestData back = JsonUtil.fromJson(json, TestData.class);
        assertNotNull(back);
        assertEquals("hello", back.name);
        assertEquals(42, back.value);
    }

    static class TestData {
        public String name;
        public int value;
        public TestData() {}
        public TestData(String name, int value) { this.name = name; this.value = value; }
    }
}

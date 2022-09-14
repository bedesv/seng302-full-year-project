package nz.ac.canterbury.seng302.portfolio.util;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ValidationUtilTest {

    Model model;
    String attribute = "username";
    String title = "Username";
    String validString = "Māori, 私のプロジェクト";
    String invalidString = "Hello❤🧡💛💚💙💞💢✝☦⛎";

    @ParameterizedTest
    @ValueSource(strings = {
            "Normal Title",
            "SENG302",
            "Māori, a-zA-Z0123456789àáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆŠŽð,. '",
            "私のプロジェクト",
            "amy.s@gmail.com"})
    void givenValidTitle_testTitleValid(String title) {
        assertTrue(ValidationUtil.titleValid(title));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "😎💖❤🎂🎉✔🎁",
            "amy.s@gmail.com❤❤❤",
            "https://eng-git.canterbury.ac.nz/seng302-2022/team-400/-/branches😎💖❤🎂🎉✔🎁"})
    void givenInvalid_testTitleValid(String title){assertFalse(ValidationUtil.titleValid(title));}

    @Test
    void givenValidAttribute_testValidAttribute(){
        assertTrue(ValidationUtil.validAttribute(model, attribute, title, validString));
    }

    @Test
    void givenInvalidAttribute_testValidAttribute(){
        assertFalse(ValidationUtil.validAttribute(model, attribute, title, invalidString));
    }

    @Test
    void givenValidAttribute_testStripTitle(){
        assertEquals(validString, ValidationUtil.stripTitle(validString));
    }

    @Test
    void givenInvalidAttribute_testStripTitle(){
        assertEquals("Hello", ValidationUtil.stripTitle(invalidString));
    }

}

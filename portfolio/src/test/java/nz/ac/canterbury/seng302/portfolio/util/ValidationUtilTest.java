package nz.ac.canterbury.seng302.portfolio.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class ValidationUtilTest {

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

}

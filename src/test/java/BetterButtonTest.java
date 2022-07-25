import org.junit.jupiter.api.Test;
import tsi_minesweeper.BetterButton;

import static org.junit.jupiter.api.Assertions.*;

public class BetterButtonTest {

    @Test
    public void test_BetterButton(){
        BetterButton testButton = new BetterButton(5, 7);

        assertEquals(5, testButton.x, "X coordinate was not set");
        assertEquals(7, testButton.y, "Y coordinate was not set");
    }

    @Test
    public void test_getRawValue(){

        BetterButton testButton = new BetterButton(0, 0);
        testButton.setValue(5);
        int returnedValue = testButton.getRawValue();

        assertEquals(5, returnedValue, "Returned value was not correct");

    }

    @Test
    public void test_getBomb(){

        BetterButton testButton = new BetterButton(0, 0);

        assertFalse(testButton.getBomb(), "Bomb value was not set");

        testButton.setBomb();

        assertTrue(testButton.getBomb(), "Bomb value was not set");
    }
}

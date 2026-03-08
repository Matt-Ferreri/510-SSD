package libby.test.bs.testSuite;

import libby.test.bs.invalid.*;
import libby.test.bs.section1.*;
import libby.test.bs.section2.*;
import libby.test.bs.section3.*;
import libby.test.bs.section4.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // Section 1 test cases
        Test_12_2_00.class,
        Test_12_7_00.class,
        Test_15_7_00.class,
        Test_16_2_00.class,
        // Section 2 test cases
        Test_6_2_00.class,
        Test_9_7_00.class,
        Test_10_9_00.class,
        Test_11_4_00.class,
        // Section 3 test cases
        Test_A2_2_00.class,
        Test_A3_10_00.class,
        Test_A7_8_00.class,
        Test_A8_3_00.class,
        // Section 4 test cases
        Test_22_2_00.class,
        Test_55_7_00.class,
        Test_77_9_00.class,
        Test_1010_6_00.class,

        // Invalid test cases
        TestHandHasOneCard.class,
        TestInvalidCard.class,
        TestInvalidUpCard.class,
        TestNoUpCard.class,
        TestNullHand.class
})

public class TestSuite00 {
    // Empty
}

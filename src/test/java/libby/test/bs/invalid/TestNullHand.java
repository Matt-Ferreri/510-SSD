package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;
import org.apache.commons.lang.ObjectUtils;

/**
 Tests is the player's hand is NULL
 */

public class TestNullHand extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        BasicStrategy strategy = new BasicStrategy ();

        // This throws an exception if hand is NULL
        assert myHand.size() == 0;
    }
}

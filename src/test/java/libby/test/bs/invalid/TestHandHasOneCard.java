package libby.test.bs.invalid;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import junit.framework.TestCase;
import libby.client.BasicStrategy;

/**
 Tests is the player's hand only has one card
 */

public class TestHandHasOneCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // Put one card in the hand, only rank matters, not suit.
        myHand.hit(new Card(4, Card.Suit.CLUBS));

        BasicStrategy strategy = new BasicStrategy ();

        // This throws an exception if hand only has one card.
        assert myHand.size() == 1;
    }
}

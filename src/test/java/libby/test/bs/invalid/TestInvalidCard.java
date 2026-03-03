package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import libby.client.BasicStrategy;

/**
 Tests is the player's hand has an invalid card
 */

public class TestInvalidCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        myHand.hit(new Card(4, Card.Suit.CLUBS));
        // 30 is an invalid rank
        myHand.hit(new Card(30, Card.Suit.HEARTS));

        BasicStrategy strategy = new BasicStrategy ();

        // This throws an exception if there's an invalid card
        // We can't have more than 21 with 2 cards in hand
        assert myHand.size() == 2 && myHand.getValue() > 21;
    }
}
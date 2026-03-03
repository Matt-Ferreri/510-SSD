package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import libby.client.BasicStrategy;

/**
 Tests is there is no up card
 */

public class TestNoUpCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // cards are dealt
        myHand.hit(new Card(4, Card.Suit.CLUBS));
        myHand.hit(new Card(3, Card.Suit.HEARTS));

        BasicStrategy strategy = new BasicStrategy ();

        // put a null up card
        Card upCard = null;

        // throws an exception if upCard is null
        assert upCard != null;

    }
}
package libby.test.bs.invalid;

import charlie.util.Play;
import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import libby.client.BasicStrategy;

/**
 The up card is of invalid rank
 */

public class TestInvalidUpCard extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // player gets dealt two cards
        myHand.hit(new Card(9, Card.Suit.CLUBS));
        myHand.hit(new Card(10, Card.Suit.HEARTS));

        // up-card has rank 12
        Card upCard = new Card(12,Card.Suit.HEARTS);

        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy
        Play play = strategy.getPlay(myHand, upCard);


        // Upcard can not be of value 12
        // This throws an exception if play is not the expected Play NONE.
        assert play == Play.NONE;
    }
}
package libby.test.bs.invalid;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import libby.client.BasicStrategy;

/**
 The play gets dealt three cards and breaks
 */

public class TestDealtThreeCards extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // player gets dealt three cards and then breaks
        myHand.hit(new Card(9, Card.Suit.CLUBS));
        myHand.hit(new Card(10, Card.Suit.HEARTS));
        myHand.hit(new Card(7, Card.Suit.SPADES));

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(2,Card.Suit.HEARTS);

        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy
        Play play = strategy.getPlay(myHand, upCard);


        // Players can't get dealt three cards so the player should break right away
        // This throws an exception if play is not the expected Play NONE.
        assert play = Play.NONE;
    }
}
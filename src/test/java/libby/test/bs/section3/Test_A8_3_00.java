package libby.test.bs.section3;

import junit.framework.TestCase;
import charlie.card.Card;
import charlie.card.Hand;
import charlie.card.Hid;
import charlie.dealer.Seat;
import charlie.util.Play;
import libby.client.BasicStrategy;

/**
 * Tests my A,2 vs dealer 2 which should be STAY.
 * <p>Except this version is only a starter that fails without
 * completing BasicStrategy.</p>
 * @author Ron.Coleman
 */
public class Test_A8_3_00 extends TestCase {
    /**
     * Runs the test.
     */
    public void test() {
        // Hand needs a hid which we can generate with a seat.
        Hand myHand = new Hand(new Hid(Seat.YOU));

        // Put two cards in the hand, only rank matters, not suit.
        myHand.hit(new Card(Card.ACE, Card.Suit.CLUBS));
        myHand.hit(new Card(8, Card.Suit.DIAMONDS));

        // Again, only up-card rank matters, not suit.
        Card upCard = new Card(3,Card.Suit.HEARTS);

        //refactored BasicStrategyStarter -> BasicStrategy.
        BasicStrategy strategy = new BasicStrategy ();

        // Play should match the basic strategy.
        Play play = strategy.getPlay(myHand, upCard);

        // This throws an exception if play is not the expected Play.
        assert play == Play.STAY;
    }
}
package libby.test.shoe;
import charlie.card.Card;


public class SplitAcesShoe1 extends charlie.shoe.Shoe {
    /**
     * Initializes the shoe.
     */
    @Override
    public void init() {
        cards.clear();

        // in order to get 2 blackjacks we need the following order:
        // Ace, King, Ace, Jack, Jack, King so we add these cards
        // suit does not matter so they can all be hearts

        // player gets ace
        cards.add(new Card(Card.ACE,Card.Suit.HEARTS));

        // dealer gets King
        cards.add(new Card(Card.KING,Card.Suit.HEARTS));

        // player gets ace
        cards.add(new Card(Card.ACE,Card.Suit.HEARTS));

        //player will now split and get 2 blackjacks
        cards.add(new Card(Card.QUEEN,Card.Suit.HEARTS));
        cards.add(new Card(Card.JACK,Card.Suit.HEARTS));

        //give dealer last card
        cards.add(new Card(Card.KING,Card.Suit.HEARTS));
    }
    /**
     * Returns true if shuffle needed.
     * @return True if shuffle needed, false otherwise.
     */
    @Override
    public boolean shuffleNeeded() {
        return false;
    }
}

package libby.plugin;

import charlie.card.Card;

public class BotShoe02 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 2
        // Huey: 6+5+7 double/lose
        // You: 10+3 stay/lose
        // Dewey: 10+6 stay/lose
        // Dealer: A+5+3
        cards.add(new Card(6, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.HEARTS));
        cards.add(new Card(Card.ACE, Card.Suit.SPADES));

        cards.add(new Card(5, Card.Suit.SPADES));
        cards.add(new Card(3, Card.Suit.SPADES));
        cards.add(new Card(6, Card.Suit.HEARTS));
        cards.add(new Card(5, Card.Suit.HEARTS));

        cards.add(new Card(7, Card.Suit.DIAMONDS));
        cards.add(new Card(3, Card.Suit.CLUBS));
    }
}

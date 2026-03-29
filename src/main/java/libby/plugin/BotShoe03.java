package libby.plugin;

import charlie.card.Card;

public class BotShoe03 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 3
        // Huey: 5+7+8 hit/win
        // You: 10+8 stay/win
        // Dewey: 6+3+9 double/win
        // Dealer: 7+7+3
        cards.add(new Card(5, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(6, Card.Suit.SPADES));
        cards.add(new Card(7, Card.Suit.SPADES));

        cards.add(new Card(7, Card.Suit.HEARTS));
        cards.add(new Card(8, Card.Suit.SPADES));
        cards.add(new Card(3, Card.Suit.SPADES));
        cards.add(new Card(7, Card.Suit.HEARTS));

        cards.add(new Card(8, Card.Suit.DIAMONDS));
        cards.add(new Card(9, Card.Suit.DIAMONDS));
        cards.add(new Card(3, Card.Suit.CLUBS));
    }
}

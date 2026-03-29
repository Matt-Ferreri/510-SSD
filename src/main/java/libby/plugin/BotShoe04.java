package libby.plugin;

import charlie.card.Card;

public class BotShoe04 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 4
        // Huey: 7+8+10 hit/break
        // You: 10+7 stay/lose
        // Dewey: 5+7+8 hit/win
        // Dealer: 6+8+4
        cards.add(new Card(7, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(5, Card.Suit.SPADES));
        cards.add(new Card(6, Card.Suit.SPADES));

        cards.add(new Card(8, Card.Suit.SPADES));
        cards.add(new Card(7, Card.Suit.HEARTS));
        cards.add(new Card(7, Card.Suit.CLUBS));
        cards.add(new Card(8, Card.Suit.HEARTS));

        cards.add(new Card(10, Card.Suit.HEARTS));
        cards.add(new Card(8, Card.Suit.DIAMONDS));
        cards.add(new Card(4, Card.Suit.SPADES));
    }
}

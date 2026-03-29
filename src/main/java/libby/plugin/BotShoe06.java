package libby.plugin;

import charlie.card.Card;

public class BotShoe06 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 6
        // Huey: 2+2+3+10 hit/hit/push
        // You: 10+8 win
        // Dewey: 9+7+10 hit/break
        // Dealer: 10+7
        cards.add(new Card(2, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.SPADES));
        cards.add(new Card(9, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.HEARTS));

        cards.add(new Card(2, Card.Suit.HEARTS));
        cards.add(new Card(8, Card.Suit.SPADES));
        cards.add(new Card(7, Card.Suit.SPADES));
        cards.add(new Card(7, Card.Suit.HEARTS));

        cards.add(new Card(3, Card.Suit.SPADES));
        cards.add(new Card(10, Card.Suit.DIAMONDS));
        cards.add(new Card(10, Card.Suit.CLUBS));
    }
}

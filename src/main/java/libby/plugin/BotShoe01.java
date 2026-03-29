package libby.plugin;

import charlie.card.Card;

/**
 * a single shoe that holds all 6 playtesting games in sequence.
 * the dealer will play through them one after another without reshuffling,
 * so you can verify each scenario from the lab appendix back-to-back.
 *
 * card layout per game (3 players + dealer):
 *   round 1: Huey card1, You card1, Dewey card1, Dealer card1
 *   round 2: Huey card2, You card2, Dewey card2, Dealer card2
 *   hits:    dealt in turn order as players request them
 */
public class BotShoe01 extends charlie.shoe.Shoe {

    @Override
    public void init() {
        cards.clear();

        // game 1: Huey hits to 18 and wins, You hits to 19 and wins,
        //         Dewey stays at 20 and wins, Dealer busts at 17
        // Huey:   K + 6 + 2  = 18  hit/win
        // You:    10 + 5 + 4 = 19  hit/win
        // Dewey:  10 + 10    = 20  stay/win
        // Dealer: K + 7      = 17  (loses to all)
        cards.add(new Card(Card.KING, Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.SPADES));   // You card1
        cards.add(new Card(10,        Card.Suit.HEARTS));   // Dewey card1
        cards.add(new Card(Card.KING, Card.Suit.HEARTS));   // Dealer card1

        cards.add(new Card(6,         Card.Suit.SPADES));   // Huey card2  -> K+6=16
        cards.add(new Card(5,         Card.Suit.SPADES));   // You card2   -> 10+5=15
        cards.add(new Card(10,        Card.Suit.DIAMONDS)); // Dewey card2 -> 10+10=20
        cards.add(new Card(7,         Card.Suit.SPADES));   // Dealer card2 -> K+7=17

        cards.add(new Card(2,         Card.Suit.SPADES));   // Huey hit    -> 16+2=18
        cards.add(new Card(4,         Card.Suit.SPADES));   // You hit     -> 15+4=19

        // game 2: Huey doubles on 11 and loses, You and Dewey stay and lose,
        //         Dealer draws to 19 and beats everyone
        // Huey:   6 + 5 + 7  = 18  double/lose
        // You:    10 + 3      = 13  stay/lose
        // Dewey:  10 + 6      = 16  stay/lose
        // Dealer: A + 5 + 3  = 19  (beats all)
        cards.add(new Card(6,         Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.SPADES));   // You card1
        cards.add(new Card(10,        Card.Suit.HEARTS));   // Dewey card1
        cards.add(new Card(Card.ACE,  Card.Suit.SPADES));   // Dealer card1

        cards.add(new Card(5,         Card.Suit.SPADES));   // Huey card2  -> 6+5=11
        cards.add(new Card(3,         Card.Suit.SPADES));   // You card2   -> 10+3=13
        cards.add(new Card(6,         Card.Suit.HEARTS));   // Dewey card2 -> 10+6=16
        cards.add(new Card(5,         Card.Suit.HEARTS));   // Dealer card2 -> A+5=16

        cards.add(new Card(7,         Card.Suit.DIAMONDS)); // Huey double -> 11+7=18
        cards.add(new Card(3,         Card.Suit.CLUBS));    // Dealer hit  -> 16+3=19

        // game 3: Huey hits to 20 and wins, You stays at 18 and wins,
        //         Dewey doubles on 9 and wins, Dealer draws to 17 and loses
        // Huey:   5 + 7 + 8  = 20  hit/win
        // You:    10 + 8      = 18  stay/win
        // Dewey:  6 + 3 + 9  = 18  double/win
        // Dealer: 7 + 7 + 3  = 17  (loses to all)
        cards.add(new Card(5,         Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.SPADES));   // You card1
        cards.add(new Card(6,         Card.Suit.SPADES));   // Dewey card1
        cards.add(new Card(7,         Card.Suit.SPADES));   // Dealer card1

        cards.add(new Card(7,         Card.Suit.HEARTS));   // Huey card2  -> 5+7=12
        cards.add(new Card(8,         Card.Suit.SPADES));   // You card2   -> 10+8=18
        cards.add(new Card(3,         Card.Suit.SPADES));   // Dewey card2 -> 6+3=9
        cards.add(new Card(7,         Card.Suit.HEARTS));   // Dealer card2 -> 7+7=14

        cards.add(new Card(8,         Card.Suit.DIAMONDS)); // Huey hit    -> 12+8=20
        cards.add(new Card(9,         Card.Suit.DIAMONDS)); // Dewey double -> 9+9=18
        cards.add(new Card(3,         Card.Suit.CLUBS));    // Dealer hit  -> 14+3=17

        // game 4: Huey hits and busts, You stays and loses,
        //         Dewey hits to 20 and wins, Dealer draws to 18
        // Huey:   7 + 8 + 10 = 25  hit/bust
        // You:    10 + 7      = 17  stay/lose
        // Dewey:  5 + 7 + 8  = 20  hit/win
        // Dealer: 6 + 8 + 4  = 18  (beats You, loses to Dewey)
        cards.add(new Card(7,         Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.SPADES));   // You card1
        cards.add(new Card(5,         Card.Suit.SPADES));   // Dewey card1
        cards.add(new Card(6,         Card.Suit.SPADES));   // Dealer card1

        cards.add(new Card(8,         Card.Suit.SPADES));   // Huey card2  -> 7+8=15
        cards.add(new Card(7,         Card.Suit.HEARTS));   // You card2   -> 10+7=17
        cards.add(new Card(7,         Card.Suit.CLUBS));    // Dewey card2 -> 5+7=12
        cards.add(new Card(8,         Card.Suit.HEARTS));   // Dealer card2 -> 6+8=14

        cards.add(new Card(10,        Card.Suit.HEARTS));   // Huey hit    -> 15+10=25 bust
        cards.add(new Card(8,         Card.Suit.DIAMONDS)); // Dewey hit   -> 12+8=20
        cards.add(new Card(4,         Card.Suit.SPADES));   // Dealer hit  -> 14+4=18

        // game 5: Huey stays at 19 and wins, You stays at 20 and wins,
        //         Dewey hits to 18 and pushes, Dealer draws to 18
        // Huey:   10 + 9     = 19  stay/win
        // You:    10 + 10    = 20  stay/win
        // Dewey:  6 + 7 + 5  = 18  hit/push
        // Dealer: 6 + 4 + 8  = 18  (loses to Huey/You, pushes Dewey)
        cards.add(new Card(10,        Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.HEARTS));   // You card1
        cards.add(new Card(6,         Card.Suit.SPADES));   // Dewey card1
        cards.add(new Card(6,         Card.Suit.HEARTS));   // Dealer card1

        cards.add(new Card(9,         Card.Suit.SPADES));   // Huey card2  -> 10+9=19
        cards.add(new Card(10,        Card.Suit.DIAMONDS)); // You card2   -> 10+10=20
        cards.add(new Card(7,         Card.Suit.SPADES));   // Dewey card2 -> 6+7=13
        cards.add(new Card(4,         Card.Suit.SPADES));   // Dealer card2 -> 6+4=10

        cards.add(new Card(5,         Card.Suit.SPADES));   // Dewey hit   -> 13+5=18
        cards.add(new Card(8,         Card.Suit.SPADES));   // Dealer hit  -> 10+8=18

        // game 6: Huey hits twice to 17 and pushes, You stays at 18 and wins,
        //         Dewey hits and busts, Dealer stays at 17
        // Huey:   2 + 2 + 3 + 10 = 17  hit/hit/push
        // You:    10 + 8          = 18  stay/win
        // Dewey:  9 + 7 + 10      = 26  hit/bust
        // Dealer: 10 + 7          = 17  (pushes Huey, loses to You)
        cards.add(new Card(2,         Card.Suit.SPADES));   // Huey card1
        cards.add(new Card(10,        Card.Suit.SPADES));   // You card1
        cards.add(new Card(9,         Card.Suit.SPADES));   // Dewey card1
        cards.add(new Card(10,        Card.Suit.HEARTS));   // Dealer card1

        cards.add(new Card(2,         Card.Suit.HEARTS));   // Huey card2  -> 2+2=4
        cards.add(new Card(8,         Card.Suit.SPADES));   // You card2   -> 10+8=18
        cards.add(new Card(7,         Card.Suit.SPADES));   // Dewey card2 -> 9+7=16
        cards.add(new Card(7,         Card.Suit.HEARTS));   // Dealer card2 -> 10+7=17

        cards.add(new Card(3,         Card.Suit.SPADES));   // Huey hit1   -> 4+3=7
        cards.add(new Card(10,        Card.Suit.DIAMONDS)); // Dewey hit   -> 16+10=26 bust
        cards.add(new Card(10,        Card.Suit.CLUBS));    // Huey hit2   -> 7+10=17
    }
}

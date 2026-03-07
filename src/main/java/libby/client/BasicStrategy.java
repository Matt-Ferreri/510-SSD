/*
 * Copyright (c) 2026 Hexant, LLC
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package libby.client;

import charlie.card.Card;
import charlie.card.Hand;
import charlie.util.Play;

/**
 * This class is an incomplete starter implementation of the Basic Strategy.
 * <p>It is table-driven, missing most of the rules and all validation.
 * @author Ron.Coleman
 */
public class BasicStrategy {
    // These help make table formatting compact to look like the pocket card.
    public final static Play P = Play.SPLIT;
    public final static Play H = Play.HIT;
    public final static Play S = Play.STAY;
    public final static Play D = Play.DOUBLE_DOWN;

    /** Rules for section 1; see Instructional Services (2000) pocket card */
    Play[][] section1Rules = {
            /*         2  3  4  5  6  7  8  9  T  A  */
            /* 21 */ { S, S, S, S, S, S, S, S, S, S },
            /* 20 */ { S, S, S, S, S, S, S, S, S, S },
            /* 19 */ { S, S, S, S, S, S, S, S, S, S },
            /* 18 */ { S, S, S, S, S, S, S, S, S, S },
            /* 17 */ { S, S, S, S, S, S, S, S, S, S },
            /* 16 */ { S, S, S, S, S, H, H, H, H, H },
            /* 15 */ { S, S, S, S, S, H, H, H, H, H },
            /* 14 */ { S, S, S, S, S, H, H, H, H, H },
            /* 13 */ { S, S, S, S, S, H, H, H, H, H },
            /* 12 */ { H, H, S, S, S, H, H, H, H, H }
    };

    Play[][] section2Rules = {
            /*          2  3  4  5  6  7  8  9  T  A  */
            /*  11 */ { D, D, D, D, D, D, D, D, D, H },
            /*  10 */ { D, D, D, D, D, D, D, D, H, H },
            /*   9 */ { H, D, D, D, D, H, H, H, H, H },
            /* 5-8 */ { H, H, H, H, H, H, H, H, H, H }
    };

    Play[][] section3Rules = {
            /*             2  3  4  5  6  7  8  9  T  A  */
            /* A 8-10 */ { S, S, S, S, S, S, S, S, S, S },
            /*    A,7 */ { S, D, D, D, D, S, S, H, H, H },
            /*    A,6 */ { H, D, D, D, D, H, H, H, H, H },
            /*    A,5 */ { H, H, D, D, D, H, H, H, H, H },
            /*    A,4 */ { H, H, D, D, D, H, H, H, H, H },
            /*    A,3 */ { H, H, H, D, D, H, H, H, H, H },
            /*    A,2 */ { H, H, H, D, D, H, H, H, H, H }
    };

    Play[][] section4Rules = {
            /*              2  3  4  5  6  7  8  9  T  A  */
            /* A,A 8,8 */ { P, P, P, P, P, P, P, P, P, P },
            /*   10,10 */ { S, S, S, S, S, S, S, S, S, S },
            /*     9,9 */ { P, P, P, P, P, S, P, P, S, S },
            /*     7,7 */ { P, P, P, P, P, P, H, H, H, H },
            /*     6,6 */ { P, P, P, P, P, H, H, H, H, H },
            /*     5,5 */ { D, D, D, D, D, D, D, D, H, H },
            /*     4,4 */ { H, H, H, P, P, H, H, H, H, H },
            /*     3,3 */ { P, P, P, P, P, P, H, H, H, H },
            /*     2,2 */ { P, P, P, P, P, P, H, H, H, H }
    };

    /**
     * Gets the play for player's hand vs. dealer up-card.
     * @param hand Hand player hand
     * @param upCard Dealer up-card
     * @return Play based on basic strategy
     */
    public Play getPlay(Hand hand, Card upCard) {
        // There is no play if the hand is empty
        if(!isValid(hand, upCard)) {
            return Play.NONE;
        }

        Card card1 = hand.getCard(0);
        Card card2 = hand.getCard(1);

        if(hand.isPair()) {
            return doSection4(hand,upCard);
        }
        else if(hand.size() == 2 && (card1.getRank() == Card.ACE || card2.getRank() == Card.ACE)) {
            return doSection3(hand,upCard);
        }
        else if(hand.getValue() >= 5 && hand.getValue() < 12) {
            return doSection2(hand,upCard);
        }
        else if(hand.getValue() >= 12)
            return doSection1(hand,upCard);

        return Play.NONE;
    }

    /**
     * Does section 1 processing of the basic strategy, 12-21 (player) vs. 2-A (dealer)
     * @param hand Player's hand
     * @param upCard Dealer's up-card
     */
    protected Play doSection1(Hand hand, Card upCard) {
        int value = hand.getValue();

        // Section 1 only supports hands >= 12 (see above).
        if(value < 12)
            return Play.NONE;

        // TODO: Complete getting the row in the table.

        // Subtract 21 since the player's hand starts at 21, and we're working
        // our way down through section 1 from index 0.
        int rowIndex = 21 - value;

        Play[] row = section1Rules[rowIndex];

        // TODO: Complete getting the column in the table.

        // Subtract 2 since the dealer's up-card starts at 2
        int colIndex = upCard.getRank() - 2;

        if(upCard.isFace())
            colIndex = 10 - 2;

            // Ace is the 10th card (index 9)
        else if(upCard.isAce())
            colIndex = 9;

        // At this row, col we should have the correct play defined.
        Play play = row[colIndex];

        return play;
    }

    protected Play doSection2(Hand hand, Card upCard) {
        int value = hand.getValue();

        // Section 2 only supports hands 5 - 11 (see above).
        if(value < 5 || value > 11) {
            return Play.NONE;
        }

        // Subtract 11 since the player's hand starts at 11, and we're working
        // our way down through section 2 from index 0.
        int rowIndex;
        if(value <= 11 && value > 8) {
            rowIndex = 11 - value;
        } else {
            rowIndex = 3;
        }

        Play[] row = section2Rules[rowIndex];

        // Subtract 2 since the dealer's up-card starts at 2
        int colIndex = upCard.getRank() - 2;

        if(upCard.isFace())
            colIndex = 10 - 2;

            // Ace is the 10th card (index 9)
        else if(upCard.isAce())
            colIndex = 9;

        // At this row, col we should have the correct play defined.
        Play play = row[colIndex];

        return play;
    }

    protected Play doSection3(Hand hand, Card upCard){
        int value = hand.getValue();

        // Section 3 only supports hands with an Ace and 2-10 (see above).
        // values 21-13
        if(value < 13 || value > 21) {
            return Play.NONE;
        }

        // A 8-10 is rowIndex 0 and Subtract 19 for A 7-2, and we're working
        // our way down through section 3 from index 0.
        int rowIndex;
        if(value >= 19 && value <= 21) {
            rowIndex = 0;
        } else {
            rowIndex = 19 - value;
        }

        Play[] row = section3Rules[rowIndex];

        // Subtract 2 since the dealer's up-card starts at 2
        int colIndex = upCard.getRank() - 2;

        if(upCard.isFace())
            colIndex = 10 - 2;

            // Ace is the 10th card (index 9)
        else if(upCard.isAce())
            colIndex = 9;

        // At this row, col we should have the correct play defined.
        Play play = row[colIndex];

        return play;
    }

    protected Play doSection4(Hand hand, Card upCard){
        int value = hand.getValue();

        // Section 4 only supports hands that are pairs (see above).
        if(!hand.isPair()) {
            return Play.NONE;
        }

        // Subtract 10 since the player's hand is a pair, we use half the value, and we're working
        // our way down through section 4 from index 0.
        int rowIndex;
        if(value < 20) {
            rowIndex = 10 - (value / 2);
        } else {
            rowIndex = 11 - (value / 2);
        }

        Play[] row = section4Rules[rowIndex];

        // Subtract 2 since the dealer's up-card starts at 2
        int colIndex = upCard.getRank() - 2;

        if(upCard.isFace())
            colIndex = 10 - 2;

            // Ace is the 10th card (index 9)
        else if(upCard.isAce())
            colIndex = 9;

        // At this row, col we should have the correct play defined.
        Play play = row[colIndex];

        return play;
    }

    /**
     * Validates a hand and up-card.
     * @param hand Hand
     * @param upCard Up-card
     * @return True if both are valid, false otherwise
     */
    boolean isValid(Hand hand, Card upCard) {
        return isValid(hand) && isValid(upCard);
    }

    /**
     * Validates a hand.
     * @param hand Hand
     * @return True if valid, false otherwise
     */
    boolean isValid(Hand hand) {
        return hand.size() >= 2;
    }

    /**
     * Validates a card
     * @param card Card
     * @return True if valid, false otherwise
     */
    boolean isValid(Card card) {
        return card.getRank() <= 10;
    }
}
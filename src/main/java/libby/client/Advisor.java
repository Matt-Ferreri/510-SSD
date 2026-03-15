package libby.client;


import charlie.card.Card;
import charlie.card.Hand;
import charlie.plugin.IAdvisor;
import charlie.util.Play;
/*
This is a class that plugs in to the project to advise
the player what move should be made based on the
BasicStrategy. If plays would like, they can have "Advise"
enabled and if the select what doesn't align with the BasicStrategy
a warning will pop up advising what to do.

@authors Matthew Ferreri and Libby Foley
 */

public class Advisor implements IAdvisor {

    // create a new instance of the basic strategy
    private final BasicStrategy bs = new BasicStrategy();

    @Override
    public Play advise(Hand myHand, Card upCard) {
        // return the play based on what the bs (BasicStratgey) says to do
        return bs.getPlay(myHand, upCard);
    }
}

package bidold;

import battlecode.common.GameActionException;

public class Bidder extends RobotPlayer {
    private static final int FIRST_ROUND_BID = 50;
    private static int BASE_INCREMENT = 5;
    // WS = Win Streak, LS = Lost Streak
    private static final double WS_ALPHA = 0.9;
    private static final double WS_BETA = 0.3;
    private static final double WS_K = 10;
    private static final double LS_GROWTH = 2;
    private static final int INCREMENT_INFLUENCE_THRESHOLD = 1200;

    private int roundsLost = 0;
    private int roundsWon = 0;
    private int lastRoundVotes = 0;
    private int lastBid = FIRST_ROUND_BID;

    public void setLastBid(int bid) {
        lastBid = bid;
    }

    public int bid() throws GameActionException {
        // stop voting if we get majority
        int thisRoundVotes = rc.getTeamVotes();
        if (thisRoundVotes > 750) {
            return 0;
        }

        int bid = getNewBid(rc.getInfluence(), thisRoundVotes > lastRoundVotes);

        // update state
        lastRoundVotes = thisRoundVotes;
        lastBid = bid;

        return bid;
    }

    private int getNewBid(int influence, boolean wonLastVote) {
        int bid = 0;

        if (wonLastVote) {
            
            if (BASE_INCREMENT -1 > 1){
                --BASE_INCREMENT;

            } else {
                BASE_INCREMENT = 1;
            }
            Math.max(BASE_INCREMENT-1, 1);
            roundsLost = 0;
            ++roundsWon;

            // the lowest we want to bid is 1
            if (lastBid > 1) {
                // decrement = [ α ^ (β * roundsWon) ] * k
                // β < α < 1
                int decrement = (int) (Math.pow(WS_ALPHA, WS_BETA * roundsWon) * WS_K);
                // System.out.println("BIDDING DECREMENT");

                // System.out.println(decrement);

                bid = lastBid - 1;
                //System.out.println("decrement by " + decrement);
            } else {
                bid = 1;
            }
        } else {
            roundsWon = 0;
            ////////
            ++BASE_INCREMENT;  
            ///////
            ++roundsLost;

            int increment = BASE_INCREMENT;
            if (influence > INCREMENT_INFLUENCE_THRESHOLD) {
                increment = (roundsLost * BASE_INCREMENT);
            }

            //System.out.println("increment by " + increment);
            bid = lastBid + increment;
        }

        if (bid > rc.getInfluence()) {
            bid = rc.getInfluence();
        }
        
        //System.out.printf("roundsWon = %d, roundsLost = %d\n", roundsWon, roundsLost);
        // System.out.println("ENEMY BID (BIDOLD)");
        // System.out.println(Math.max(bid, 0));
        // System.out.println("=======");
        return Math.max(bid, 0);
    }
}

package ourplayer;

import java.util.ArrayList;
import java.util.Map;

import battlecode.common.*;

public class Slanderer extends RobotPlayer implements RoleController {
    
    private int age;
    private MapLocation spawnEC;
    private int spawnECid;
    private double endRadius2;


    public Slanderer(){
        age = 0;
        endRadius2 = 40;
    }

    public void run() throws GameActionException {
        if (age == 0){
            runFirstTurn();
        } 

        Direction bestMove = getBest(getPossibleMoves());
        if(bestMove != null){
            tryMove(bestMove);
        }
    
        age++;
    }

    private double calculateTargetRadius2(){

        return ((double) age * endRadius2)/300;
    }
    private void runFirstTurn(){
        //first turn stuff

        //set spawnEC
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for (int i = 0; i < nearby.length; i++){
            
            // This WILL break if there is more than one EC next to
            if (nearby[i].type == RobotType.ENLIGHTENMENT_CENTER){
                spawnEC = nearby[i].location;
                spawnECid = nearby[i].ID;
                System.out.println("HOME ENLIGHTENMENT CENTER LOCATED: "+ spawnEC.x+" , "+spawnEC.y);
                System.out.println("HOME ENLIGHTENMENT CENTER ID: " + nearby[i].ID);
            }
        }
        
    }

    private ArrayList<Direction> getPossibleMoves(){
        ArrayList<Direction> possibleMoves = new ArrayList<>();
        for(Direction d : Direction.values()){
            if(rc.canMove(d)){
                possibleMoves.add(d);
            }
        }
        return possibleMoves;
    }

    private Direction getBest(ArrayList<Direction> possibleMoves){
        //~~~~~~~~ MODIFY THESE TO CHANGE PRIORITIZATION ~~~~~~~~~//
        //double kRadius = 0.5f;
        //double kClockwise = 0.5f;
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

        double bestWeight = 0.0f;
        Direction bestDirection = null;

        int ecVecX = spawnEC.x - rc.getLocation().x; 
        int ecVecY = spawnEC.x - rc.getLocation().x; 

        int targetVecX = 0;
        int targetVecY = 0;
        
        for(Direction d : possibleMoves){
            switch(d){
                case NORTH:
                    targetVecX = 0;
                    targetVecY = 1;
                    break;
                case NORTHEAST:
                    targetVecX = 1;
                    targetVecY = 1;
                    break;
                case EAST:
                    targetVecX = 1;
                    targetVecY = 0;
                    break;
                case SOUTHEAST:
                    targetVecX = 1;
                    targetVecY = -1;
                    break;
                case SOUTH:
                    targetVecX = 0;
                    targetVecY = -1;
                    break;
                case SOUTHWEST:
                    targetVecX = -1;
                    targetVecY = -1;
                    break;
                case WEST:
                    targetVecX = -1;
                    targetVecY = 0;
                    break;
                case NORTHWEST:
                    targetVecX = -1;
                    targetVecY = 1;
            }
            
            //to calculate r^2 to given move, use the current pos (ecVec) and add the move's vector (targetVec), then r^2 it
            int moveR2 = (int) (Math.pow(ecVecX + targetVecX, 2) + Math.pow(ecVecY + targetVecY, 2));
            double deltaR2 = Math.abs(calculateTargetRadius2() - moveR2);
            // equation here for adjustment :
            // y = 1 / (ax + 1)
            // adjust a to produce steeper or smoother down
            double r2Weight =  1/(1+deltaR2);
            
            double crossProduct = ecVecX * targetVecY - ecVecY * targetVecX;

            double totalWeight = r2Weight*crossProduct;

            if(totalWeight > bestWeight){
                bestWeight = totalWeight;
                bestDirection = d;
            } 
        }
        return bestDirection;

         //step 1: turn directions into weighted edge graph where weight = desirablility of possible move, weight (1 ... -1)
         /*   
               o  o  o
                \ | /
              o - ø – o
                / | \
               o  o  o
         */ //O_O novel coronavirus
        
        //step 2: weight graph including a 0 cost path to itself
        //step 3: choose path of lowest cost
        //will prioritize standing still over moving backwards

        //obstacles:
        //  easy: prioritize radius
        //  hard?: weight moves by how clockwise they are (negative for backwards)
            // "perpendicularity" of vector to ec and vector to move
            // cross product

    }

}

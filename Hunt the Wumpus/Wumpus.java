import java.util.*;

public class Wumpus {

    Environment[][] world; //matrix representing the wumpus world which is a 4x4 grid. Will define it as 4x4 later

    int playerHorizontal;   //location of player on the X axis
    int playerVertical;     //location of the player on the Y axis
    int playerDirection;    //where the player is currentWorldly facing
    int oldDirection;       //previous direction where the player was facing
    int around;
    int aroundCount;
    int dead=0;   //Checks if player is alive

    int wumpusHorizontal; //location of wumpus at the x axis
    int wumpusVertical;  //location of wumpus at the y axis

    int goldHorizontal; //gold x axis
    int goldVertical; //gold y axis

    //initializing bumps, arrow, and scream
    String bump="";
    String arrow="";
    String scream="";

    static Random randGenerator;

    public Wumpus() {
        randGenerator = new Random();
    }

    public void WumpusWorld()
    {
        //places player in room in the bottom left tile, at point (x,y)=(0,0), facing right
        playerHorizontal=0; //x axis for players location
        playerVertical=0; //y axis for players location
        playerDirection=0; //the direction where the player is facing

        //initializes wumpus and gold. Later will generate location
        wumpusHorizontal=0;
        wumpusVertical=0;
        goldHorizontal=0;
        goldVertical=0;

        //initializes bump, arrow, variable for 
        bump="";
        arrow="";
        around=0;
        aroundCount=0;
        Environment currentWorld = new Environment();

        world = new Environment[4][4]; //creates the 4x4 matrix of the world
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                world[i][j] = new Environment();
            }
        }

        while (wumpusHorizontal == 0 && wumpusVertical == 0) {
            wumpusHorizontal = randGenerator.nextInt(4); //random generating of x axis for wumpus given its not at 0,0
            wumpusVertical = randGenerator.nextInt(4); //same for y
        }

        currentWorld = world[wumpusHorizontal][wumpusVertical];
        currentWorld.attributes.add("W");
        addAround("S",wumpusHorizontal,wumpusVertical); //add the stenches 1 tile around the wumpus up, down, left, right

        while ( (goldHorizontal == 0 && goldVertical == 0) || (goldHorizontal == wumpusHorizontal && goldVertical == wumpusVertical))
        //randomly select. Cannot be [0][0] or in the same tile as the wumpus
        {
            goldHorizontal = randGenerator.nextInt(4);
            goldVertical = randGenerator.nextInt(4);
        }

        currentWorld = world[goldHorizontal][goldVertical];
        currentWorld.attributes.add("G");

        for (int i = 0; i < 4; i++) //randomly add pits but not with the gold or in 1,1
        {
            for (int j = 0; j < 4; j++) {

                if (i != 0 && j != 0) //it cannot be in the players starting square 
                {
                    int rand = randGenerator.nextInt(10); //generates a number in the range 0 to 9 randomly
                    if (rand <= 1) {
                        //20% to generate the pit since there is a 0.2 i.e. 20% chance to get 0 or 1 in thisrange (2 out of 10) 
                        currentWorld = world[i][j];
                        currentWorld.attributes.add("P");
                        addAround("B",i,j); //add the breeze around the pit
                    }
                }
            }
        }
    } //the location of the wumpus, pit and gold has been generated. The player is initialized in the bottom left tile of the matrix

    public void addAround(String attributeAround, int x, int y) //adds the attributes around the point (x,y)
    {
        Environment currentWorld=new Environment();

        x--;
        if(x>=0){
            currentWorld = world[x][y];
            currentWorld.attributes.add(attributeAround);
        }

        x=x+2;

        if(x<=3){
            currentWorld = world[x][y];
            currentWorld.attributes.add(attributeAround);
        }
        x--;

        y--;
        if(y>=0){
            currentWorld = world[x][y];
            currentWorld.attributes.add(attributeAround);
        }

        y=y+2;

        if(y<=3){
            currentWorld = world[x][y];
            currentWorld.attributes.add(attributeAround);
        }
    }

    public void removeAround(String attributeAround, int x, int y) //removes the attributes around the point (x,y) e.g. when wumpus is dead
    {
        Environment currentWorld=new Environment();

        x--;
        if(x>=0){
            currentWorld = world[x][y];
            currentWorld.attributes.remove(attributeAround);
        }
        x=x+2;

        if(x<=3) {
            currentWorld = world[x][y];
            currentWorld.attributes.remove(attributeAround);
        }
        x--;
        y--;
        if(y>=0) {
            currentWorld = world[x][y];
            currentWorld.attributes.remove(attributeAround);
        }
        y=y+2;

        if(y<=3) {
            currentWorld = world[x][y];
            currentWorld.attributes.remove(attributeAround);
        }
    }



    public int result(String action)
    //returns the new World state, which occurs after the agents next action, aong with the cost of that performed action
    {
        scream="";
        String bumps="";
        Environment currentWorld=new Environment();
        int returnValue=0; //just to initialize

        if(action.equalsIgnoreCase("turnLeft"))
        {
            turn(-1);
            returnValue=-1;
        }

        if(action.equalsIgnoreCase("turnRight"))
        {
            turn(1);
            returnValue=-1;
        }

        if(action.equalsIgnoreCase("forward"))
        {
            bumps=move(playerDirection);
            returnValue=-1;

            currentWorld=world[playerHorizontal][playerVertical]; //checks for pits and wumpus after moving

            for(int i=0;i<currentWorld.attributes.size();i++){
                if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("P"))
                {
                    returnValue = -1000;
                    //The agent fell into the pit and died
                    dead++;
                }
            }
            if(playerHorizontal==wumpusHorizontal && playerVertical==wumpusVertical){
                returnValue = -1000;
                //Agent ran into a wumpus and died
                dead++;
            }
        }

        if(action.equalsIgnoreCase("grab"))
        {
            currentWorld=world[playerHorizontal][playerVertical];
            for(int i=0;i<currentWorld.attributes.size();i++){
                if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("G")){
                    returnValue = 1000; //player wins after collecting gold
                    currentWorld.attributes.remove("G");
                }
            }
            if(returnValue!=1000){
                returnValue=-1;
            }
        }
        //shoots arrow and checks if it killed the wumpus
        if(action.equalsIgnoreCase("shoot") && returnValue!=-1000)
        {
            returnValue = shoot();
        }
        bump=bumps;
        return returnValue;
    }

    //does the arrow thing and returns 1000 for hit and -10 for miss
    public int shoot()
    {
        int returnValue=-10;
        int x;
        int y;
        Environment currentWorld=new Environment();

        if(playerDirection==0) {      //0 means the player is facing right

            x=playerHorizontal+1;

            while(x<=3){
                currentWorld = world[x][playerVertical];
                for(int i=0;i<currentWorld.attributes.size();i++){
                    if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("W"))
                        returnValue=-10;
                    currentWorld.attributes.remove("W");
                    removeAround("S",x,playerVertical);
                    scream="yes";
                }
                x++;
            }
        }

        if(playerDirection==1){
            //1 means player is facing up

            y=playerVertical+1;
            while(y<=3){
                currentWorld = world[playerHorizontal][y];
                for(int i=0;i<currentWorld.attributes.size();i++){
                    if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("W"))
                        returnValue=-10;
                    currentWorld.attributes.remove("W");
                    removeAround("S",playerHorizontal,y);
                    scream="yes";
                }
                y++;
            }
        }
        //at 2, player is facing left
        if(playerDirection==2){
            x=playerHorizontal-1;
            while(x>=0){
                currentWorld = world[x][playerVertical];
                for(int i=0;i<currentWorld.attributes.size();i++){
                    if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("W"))
                        returnValue=-10;
                    currentWorld.attributes.remove("W");
                    removeAround("S",x,playerVertical);
                    scream="yes";
                }
                x--;
            }
        }
        //at 3, agent is facing down
        if(playerDirection==3){
            y=playerVertical-1;
            while(y>=0){
                currentWorld = world[playerHorizontal][y];
                for(int i=0;i<currentWorld.attributes.size();i++){
                    if( ((String)currentWorld.attributes.get(i)).equalsIgnoreCase("W"))
                        returnValue=-10;
                    currentWorld.attributes.remove("W");
                    removeAround("S",playerHorizontal,y);
                    scream="yes";
                }
                y--;
            }
        }
        return returnValue;
    }


    public void turn(int r) //turns the agent left or right
    {
        //0 - right, 1 - up, 2 - left, 3 - down
        oldDirection=playerDirection;

        if(r==1) playerDirection--; //to turn to the right
        else playerDirection++; //to turn to the left

        if(playerDirection==4) playerDirection=0;
        if(playerDirection==-1) playerDirection=3;
    }

    //to move the agent
    public String move(int d)
    {
        String returnValue="";

        //Right
        if(d==0) playerHorizontal++;

        //Up
        if(d==1) playerVertical++;

        //Left
        if(d==2) playerHorizontal--;

        //Down
        if(d==3) playerVertical--;

        if(playerHorizontal>3){
            playerHorizontal=3;
            returnValue="bump";
        }
        if(playerVertical>3){
            playerVertical=3;
            returnValue="bump";
        }
        if(playerHorizontal<0){
            playerHorizontal=0;
            returnValue="bump";
        }
        if(playerVertical<0){
            playerVertical=0;
            returnValue="bump";
        }

        //Bump represents the sound when the agent hits a wall. If that happens, it does not move
        return returnValue;
    }
    
    //looks at the world returns its action
    public String agentSteps()
    {
        String returnValue="";
        boolean stenchy=false;
        boolean breeze=false;
        //get currentWorld senses
        Environment currentWorld=world[playerHorizontal][playerVertical];
        for(int i=0;i<currentWorld.attributes.size();i++)
        {
            String temp=(String)currentWorld.attributes.get(i);
            if(temp.equalsIgnoreCase("S")){
                if(arrow.equalsIgnoreCase("")){
                    returnValue="shoot";
                    arrow="shot";
                }else{
                    arrow="";
                }
            }
            if(temp.equalsIgnoreCase("G"))
                returnValue="grab";
        }
        if(returnValue.equalsIgnoreCase("")){
            int pick=randGenerator.nextInt(3);
            //turn left
            if(pick==0){
                returnValue="turnLeft";
            }
            //turn right
            if(pick==1){
                returnValue="turnRight";
            }
            //move forward
            if(pick==2){
                returnValue="forward";
            }
        }
        return returnValue;
    }

    //
    //
    //
    //this is to test and run the program
    //
    //
    //
    public static void main(String args[]){
        Wumpus test;
        int score;
        int moves;
        int points;
        int avg=0;
        int times=0;
        String makeAction="";
        System.out.println("Starting Simulation");

        test = new Wumpus();

        while(times<10000){
            times++;
            test.WumpusWorld();
            score=0;
            moves=0;
            points=0;
            test.around=0;
            while ( (points != -1000 && points != 1000) && moves < 1000) {
                if (moves < 1000) {
                    makeAction = test.agentSteps();
                    points = test.result(makeAction);
                    System.out.println("Action: "+makeAction+"   points: "+points+"    moves: " + moves);
                    score = score + points;
                }
                moves++;
            }
            System.out.println("Score: " + score);
            avg=avg+score;
        }
        System.out.println("Agent average score: "+avg/10000);

    }
}
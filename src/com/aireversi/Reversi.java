// Matthew Doyle
// 301322233
// mwdoyle@sfu.ca

// Example of play
//Black Moves first
//
//        A B C D E F G H
//        1 _ _ _ _ _ _ _ _
//        2 _ _ _ _ _ _ _ _
//        3 _ _ _ * _ _ _ _
//        4 _ _ * W B _ _ _
//        5 _ _ _ B W * _ _
//        6 _ _ _ _ * _ _ _
//        7 _ _ _ _ _ _ _ _
//        8 _ _ _ _ _ _ _ _
//
//        Place move (Black):
//        d3
//
//        Black: 4 White: 1
//
//        A B C D E F G H
//        1 _ _ _ _ _ _ _ _
//        2 _ _ _ _ _ _ _ _
//        3 _ _ * B * _ _ _
//        4 _ _ _ B B _ _ _
//        5 _ _ * B W _ _ _
//        6 _ _ _ _ _ _ _ _
//        7 _ _ _ _ _ _ _ _
//        8 _ _ _ _ _ _ _ _
//
//        Place move (White):
//        e3
//
//        White: 3 Black: 3
//
//        A B C D E F G H
//        1 _ _ _ _ _ _ _ _
//        2 _ _ _ _ _ * _ _
//        3 _ _ _ B W * _ _
//        4 _ _ _ B W * _ _
//        5 _ _ _ B W * _ _
//        6 _ _ _ _ _ * _ _
//        7 _ _ _ _ _ _ _ _
//        8 _ _ _ _ _ _ _ _

// Reversi class inspired by: http://www.codebytes.in/2014/12/reversi-two-players-java-program.html

package com.aireversi;

import com.rits.cloning.Cloner;

import java.util.*;

public class Reversi{
    // change values here to affect the heuristics
    // Black uses pure M.C.T.S and 1000 play outs
    private static final int BLACK_PLAY_OUTS = 1000;
    // White uses and 1000 play outs
    private static final int WHITE_PLAY_OUTS = 1000;
    // choose how often white makes a random guess (explore) rather than doing play outs
    private static final int doRandomGuessEvery = 999; // ie. 0 = random guess every turn, 999 = never make random guess
    // points awarded for a white player win in random play outs
    private static final int whiteWinHeuristic = 5;
    private static final int whiteLossHeuristic = -5;
    private static final int whiteDrawHeuristic = 5;
    // increase this number to get data from multiple games
    final static int gamesToPlay = 10;

    public static int twoPlayers(Board b, boolean userPlayer) {

        Scanner scan = new Scanner(System.in);
        Board.Point move = b.new Point(-1, -1);

        System.out.println("The black player moves first");

        int result;
        Boolean skip;
        String input;
        int counter = 0;
        boolean randomPlayOut = true;

        while(true){
            skip = false;

            HashSet<Board.Point> blackPlaceableLocations = b.getPlaceableLocations('B', 'W');
            HashSet<Board.Point> whitePlaceableLocations = b.getPlaceableLocations('W', 'B');

            b.showPlaceableLocations(blackPlaceableLocations, 'B', 'W');
            result = b.gameResult(whitePlaceableLocations, blackPlaceableLocations);

            // result = 1 white wins, -1 black wins, 0 draw
            if(result == 0){System.out.println("The game is a draw.");return result;}
            else if(result==1){System.out.println("The white player wins: "+b.WScore+":"+b.BScore+" by "+(Math.abs(b.WScore - b.BScore)));return result;}
            else if(result==-1){System.out.println("The black player wins: "+b.BScore+":"+b.WScore+" by "+(Math.abs(b.BScore - b.WScore)));return result;}

            if(blackPlaceableLocations.isEmpty()){
                System.out.println("Black needs to skip its turn, white may play");
                skip = true;
            }

            if(!skip){
                System.out.println("It's blacks turn: ");

                if (userPlayer){    // the user is playing
                    boolean userInput = true;
                    while(userInput) {
                        // for user input
                        input = scan.next();
                        if (Character.isLetter(input.charAt(0)) && Character.isDigit(input.charAt(1))){
                            move.y = b.coordinateX(input.charAt(0));
                            move.x = (Integer.parseInt(input.charAt(1)+"")-1);
                            userInput = false;
                        } else {
                            // invalid inout
                            System.out.println("Invalid input (try something like d3): ");
                        }
                    }
                }
                else {  // the computer is playing
                    // create data structure for winning moves
                    Map<Board.Point, Integer> moveWinCount = new HashMap<>();

                    for (Board.Point loc : blackPlaceableLocations) {
                        moveWinCount.put(loc, 0);
                    }

                    // make random play outs
                    for (Board.Point loc : blackPlaceableLocations) {
                        // change the number of play outs for each move
                        for (int i = 0; i < BLACK_PLAY_OUTS; i++) {
                            // play out each move i times
                            int resultValue = randomPlayout(b, loc, 'B', 'W');
                            moveWinCount.put(loc, moveWinCount.getOrDefault(loc, 0) + resultValue);
                        }
                    }

                    // display results
                    System.out.println(Arrays.toString(moveWinCount.entrySet().toArray()));

                    // find move with the highest score
                    Map.Entry<Board.Point, Integer> maxEntry = null;
                    for (Map.Entry<Board.Point, Integer> entry : moveWinCount.entrySet())
                    {
                        if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                        {
                            maxEntry = entry;
                        }
                    }
                    // make the move
                    System.out.println("Black chooses...");
                    System.out.println("["+maxEntry.getKey().x + ", " + maxEntry.getKey().y+"]");
                    move.y = maxEntry.getKey().y;
                    move.x = maxEntry.getKey().x;
                }

                while(!blackPlaceableLocations.contains(move)){
                    System.out.println("Invalid move!\nIt's still blacks turn: ");
                    boolean userInput = true;
                    while(userInput) {
                        // for user input
                        input = scan.next();

                        if (Character.isLetter(input.charAt(0)) && Character.isDigit(input.charAt(1))){
                            move.y = b.coordinateX(input.charAt(0));
                            move.x = (Integer.parseInt(input.charAt(1)+"")-1);
                            userInput = false;
                        } else {
                            // invalid input
                            System.out.println("Invalid input (try something like d3): ");
                        }
                    }
                }
                b.placeMove(move, 'B', 'W');
                b.updateScores();
                System.out.println("\nBlack: "+b.BScore+" White: "+b.WScore);
            }
            skip = false;

            whitePlaceableLocations = b.getPlaceableLocations('W', 'B');
            blackPlaceableLocations = b.getPlaceableLocations('B', 'W');

            b.showPlaceableLocations(whitePlaceableLocations, 'W', 'B');
            result = b.gameResult(whitePlaceableLocations, blackPlaceableLocations);

            if(result==0){System.out.println("The game is a draw.");return result;}
            else if(result==1){System.out.println("The white player wins: "+b.WScore+":"+b.BScore+" by "+(Math.abs(b.WScore - b.BScore)));return result;}
            else if(result==-1){System.out.println("The black player wins: "+b.BScore+":"+b.WScore+" by "+(Math.abs(b.BScore - b.WScore)));return result;}

            if(whitePlaceableLocations.isEmpty()){
                System.out.println("White needs to skip... Passing to Black");
                skip = true;
            }

            if(!skip){
                System.out.println("It's whites turn: ");

                if (counter == doRandomGuessEvery) {
                    randomPlayOut = false;
                }

                if (randomPlayOut) { // make random play outs
                    // create data structure for winning moves
                    Map<Board.Point, Integer> moveWinCount = new HashMap<>();

                    for (Board.Point loc : whitePlaceableLocations) {
                        moveWinCount.put(loc, 0);
                    }

                    // make random play outs
                    for (Board.Point loc : whitePlaceableLocations) {
                        // change the number of play outs for each move
                        for (int i = 0; i < WHITE_PLAY_OUTS; i++) {
                            // play out each move i times
                            int resultValue = randomPlayout(b, loc, 'W', 'B');
                            moveWinCount.put(loc, moveWinCount.getOrDefault(loc, 0) + resultValue);
                        }
                    }

                    // display results
                    System.out.println(Arrays.toString(moveWinCount.entrySet().toArray()));

                    // find move with the highest score
                    Map.Entry<Board.Point, Integer> maxEntry = null;
                    for (Map.Entry<Board.Point, Integer> entry : moveWinCount.entrySet())
                    {
                        if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                        {
                            maxEntry = entry;
                        }
                    }
                    // make the move
                    System.out.println("white chooses...");
                    System.out.println("["+maxEntry.getKey().x + ", " + maxEntry.getKey().y+"]");
                    move.y = maxEntry.getKey().y;
                    move.x = maxEntry.getKey().x;

                    // for user input of white player
//                    input = scan.next();
//                    move.y = b.coordinateX(input.charAt(0));
//                    move.x = (Integer.parseInt(input.charAt(1)+"")-1);
//                    System.out.println("move placed: " + move.y + move.x);
                    counter++;


                } else { // don't make random play outs just choose randomly (explore)
                    System.out.println("white chooses randomly...");

                    List<Board.Point> listWhite = new ArrayList<Board.Point>(whitePlaceableLocations);
                    Random rand = new Random();
                    Board.Point randomMoveWhite = listWhite.get(rand.nextInt(listWhite.size()));

                    move.y = randomMoveWhite.y;
                    move.x = randomMoveWhite.x;
                    System.out.println("["+randomMoveWhite.x + ", " + randomMoveWhite.y+"]");

                    randomPlayOut = true;
                    counter = 0;
                }


                while(!whitePlaceableLocations.contains(move)){
                    System.out.println("Invalid move!\nIt's still whites turn: ");
                    input = scan.next();
                    move.y = b.coordinateX(input.charAt(0));
                    move.x = (Integer.parseInt(input.charAt(1)+"")-1);
                }
                b.placeMove(move, 'W', 'B');
                b.updateScores();
                System.out.println("\nWhite: "+b.WScore+" Black: "+b.BScore);
            }
        }
    }

    private static int randomPlayout(Board b, Board.Point loc, char p, char o) {
        // imported library from https://search.maven.org/artifact/io.github.kostaskougios/cloning/1.10.3/bundle
        Cloner cloner = new Cloner();
        Board clone = cloner.deepClone(b);

        int statusOfGame;
        // make move initial move
        clone.placeMove(loc, p, o);

        // make random play outs
        if (p == 'B') { // if black use these basic heuristics
            statusOfGame = BlackPureMCTS(clone, p, o);
        } else { // if white use these custom heuristics
            statusOfGame = WhiteHeuristics(clone, p, o);
        }

        return statusOfGame;
    }

    private static int BlackPureMCTS(Board clone, char p, char o) {

        Board.Point move = clone.new Point(-1, -1);
        int result;
        Boolean skip;

        while(true){
            skip = false;

            HashSet<Board.Point> blackPlaceableLocations = clone.getPlaceableLocations('B', 'W');
            HashSet<Board.Point> whitePlaceableLocations = clone.getPlaceableLocations('W', 'B');

            // if white just went start here...
            if (p != 'B') {

                result = clone.gameResult(whitePlaceableLocations, blackPlaceableLocations);

                // change heuristics here
                // result = 1 white wins, -1 black wins, 0 draw
                if (result == 0){return 0;}
                else if (result == 1){return -1;}
                else if (result == -1){return 1;}

                if (blackPlaceableLocations.isEmpty()) {
                    skip = true;
                }

                if (!skip) {

                    List<Board.Point> listBlack = new ArrayList<Board.Point>(blackPlaceableLocations);

                    // trying random
                    Random rand = new Random();
                    Board.Point randomMoveBlack = listBlack.get(rand.nextInt(listBlack.size()));

                    move.y = randomMoveBlack.y;
                    move.x = randomMoveBlack.x;
                    clone.placeMove(move, 'B', 'W');
                    clone.updateScores();
                }
            }

            p = 'X';

            skip = false;

            // if black went first start here...
            whitePlaceableLocations = clone.getPlaceableLocations('W', 'B');
            blackPlaceableLocations = clone.getPlaceableLocations('B', 'W');

            result = clone.gameResult(whitePlaceableLocations, blackPlaceableLocations);

            // change heuristics here
            // result = 1 white wins, -1 black wins, 0 draw
            if(result==0){return 0;}
            else if(result==1){return -1;}
            else if(result==-1){return 1;}

            if(whitePlaceableLocations.isEmpty()){
                skip = true;
            }

            if(!skip){
                List<Board.Point> listWhite = new ArrayList<Board.Point>(whitePlaceableLocations);
                Random rand = new Random();
                Board.Point randomMoveWhite = listWhite.get(rand.nextInt(listWhite.size()));

                move.y = randomMoveWhite.y;
                move.x = randomMoveWhite.x;

                clone.placeMove(move, 'W', 'B');
                clone.updateScores();
            }
        }
    }

    private static int WhiteHeuristics(Board clone, char p, char o) {

        Board.Point move = clone.new Point(-1, -1);
        int result;
        Boolean skip;

        while(true){
            skip = false;

            HashSet<Board.Point> blackPlaceableLocations = clone.getPlaceableLocations('B', 'W');
            HashSet<Board.Point> whitePlaceableLocations = clone.getPlaceableLocations('W', 'B');

            // if white just went start here...
            if (p != 'B') {

                result = clone.gameResult(whitePlaceableLocations, blackPlaceableLocations);

                // change heuristics here
                // result = 1 white wins, -1 black wins, 0 draw
                if (result == 0){return whiteDrawHeuristic;}
                else if (result == 1){return whiteWinHeuristic;}
                else if (result == -1){return whiteLossHeuristic;}

                if (blackPlaceableLocations.isEmpty()) {
                    skip = true;
                }

                if (!skip) {

                    List<Board.Point> listBlack = new ArrayList<Board.Point>(blackPlaceableLocations);

                    // trying random
                    Random rand = new Random();
                    Board.Point randomMoveBlack = listBlack.get(rand.nextInt(listBlack.size()));

                    move.y = randomMoveBlack.y;
                    move.x = randomMoveBlack.x;
                    clone.placeMove(move, 'B', 'W');
                    clone.updateScores();
                }
            }

            p = 'X';

            skip = false;

            // if black went first start here...
            whitePlaceableLocations = clone.getPlaceableLocations('W', 'B');
            blackPlaceableLocations = clone.getPlaceableLocations('B', 'W');

            result = clone.gameResult(whitePlaceableLocations, blackPlaceableLocations);

            // change heuristics here
            // result = 1 white wins, -1 black wins, 0 draw
            if(result==0){return whiteDrawHeuristic;}
            else if(result==1){return whiteWinHeuristic;}
            else if(result==-1){return whiteLossHeuristic;}

            if(whitePlaceableLocations.isEmpty()){
                skip = true;
            }

            if(!skip){
                List<Board.Point> listWhite = new ArrayList<Board.Point>(whitePlaceableLocations);
                Random rand = new Random();
                Board.Point randomMoveWhite = listWhite.get(rand.nextInt(listWhite.size()));

                move.y = randomMoveWhite.y;
                move.x = randomMoveWhite.x;

                clone.placeMove(move, 'W', 'B');
                clone.updateScores();
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("Enter your choice...\n" +
                           "-Play the computer (p)\n" +
                           "-Collect data using computer vs computer (d)");
        // https://www.w3schools.com/java/java_user_input.asp
        boolean userInput = true;
        int blackWins = 0;
        int whiteWins = 0;
        int draw = 0;

        while (userInput) {
            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter choice:");
            String userChoice = myObj.nextLine();  // Read user input
            System.out.println("Choice is: " + userChoice);  // Output user input
            if (userChoice.charAt(0) == 'p') {
                Board b = new Board();
                System.out.println("You are the black player input something like d3");  // Output user input
                twoPlayers(b, true);
                userInput = false;
            } else if (userChoice.charAt(0) == 'd'){
                for (int i = 0; i < gamesToPlay; i++) {
                    Board b = new Board();
                    System.out.println("\n***********Game "+(i+1)+"***********");
                    int r = twoPlayers(b, false);
                    // result = 1 white wins, -1 black wins, 0 draw
                    if (r == 1){
                        whiteWins++;
                    } else if (r == -1){
                        blackWins++;
                    } else {
                        draw++;
                    }
                }
                userInput = false;
            } else {
                // do nothing
                System.out.println("Invalid choice choose p or d");
            }
        }

        System.out.println("\nEnd of run stats...\nBlack won "+blackWins+" game(s),"+" White won "+whiteWins+" game(s),"+" Draw games "+draw);

    }
}
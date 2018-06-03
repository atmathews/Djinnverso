/* This program is meant to simulate the game Djinnverso described in the Children of the Lamp series by P.B Kerr
* Essentially it is a type of Poker with Dice
* There are 7 Dies each ranging from 1 through 8. The dice can form patterns like pairs, or triplets or a pair and a triplet etc
* These patterns are ranked in terms of rarity, and named Alpha through Mike (Because I'm too uncreative). The higher the rarity, the more that pattern is worth.
* If two rolls provide similar patterns, they higher numbers win.
* For example a Triplet of 1s will beat a Pair of 8s, but a Pair of 8s will beat a Pair of 7s
* The first player receives a pre-rolled die set and gets to bid any pattern of choosing
* The phone is then passed to the next player who can either challenge the Bid or Keep it
* If challenged, the dice are revealed. If the stated bid is worse or equal to the current board state, the challenger loses. Else the bidder loses
* If the bid is accepted, the dice are shown to the accepting player. He can then roll anywhere from 1 - all 8 dice, or roll none
* The player must then bid a pattern that is HIGHER than the previous bid. The game then continues to the next player
* */


package com.atmathews.djinnverso;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

// For Implementing Official support of multiplayer rather than have it be controlled outside the app. Eventually
    int numPlayers = 0;

    // Setting up the dies
    Dice[] vices = new Dice[]{new Dice(R.id.die1),
            new Dice(R.id.die2),
            new Dice(R.id.die3),
            new Dice(R.id.die4),
            new Dice(R.id.die5),
            new Dice(R.id.die6),
            new Dice(R.id.die7)};

    // Setting up the reference board states to give the pattern names meaning
// The values are used to clarify the hirearchy of states without a giant mess of if statements. Might be possible to do with an array ...
    final BoardState[] referenceBoards = new BoardState[]{
            new BoardState("Alpha", 8, 1, new int[]{1}, ""),
            new BoardState("Bravo", 16, 9, new int[]{2}, ""),
            new BoardState("Charlie", 30, 17, new int[]{2, 2}, ""),
            new BoardState("Delta", 38, 31, new int[]{3}, ""),
            new BoardState("Echo", 52, 39, new int[]{3, 2}, ""),
            new BoardState("Foxtrot", 66, 53, new int[]{3, 3}, ""),
            new BoardState("Golf", 74, 67, new int[]{4}, ""),
            new BoardState("Hotel", 88, 75, new int[]{4, 2}, ""),
            new BoardState("India", 102, 89, new int[]{4, 3}, ""),
            new BoardState("Juliet", 110, 103, new int[]{5}, ""),
            new BoardState("Kilo", 124, 111, new int[]{5, 2}, ""),
            new BoardState("Lima", 132, 125, new int[]{6}, ""),
            new BoardState("Mike", 140, 133, new int[]{7}, "")};

// Initializing the betState that is independent of the current board state and all the buttons and boxes etc
    BoardState betState = new BoardState();

    Button rollButton;
    Button bidButton;
    Button acceptButton;
    Button challengeButton;
    Button nextStatePlusButton;
    Spinner nameSpinner;
    Spinner numSpinner;
    Spinner explainSpinner;
    TextView betBox;
    TextView bigBetBox;
    TextView valueTextBox;
    TextView printer;
    int acceptActionMode = 1;

    AdapterView.OnItemSelectedListener nameListener;
// Trying to implement a quick way to just bet 1 higher. Still buggy
    boolean plusButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// Function used to initialize the buttons / sync to the XML etc.
        this.initializing();
// Initial random dice roll
        for (int i = 0; i < vices.length; i++) {

            vices[i].forcedRoll();
        }
        setEnables(1);

    }

    //Method to set all the find by view IDs. So I can collapse/group these
    private void initializing() {

        rollButton = (Button) findViewById(R.id.roll_button);
        bidButton = (Button) findViewById(R.id.bid_button);
        acceptButton = (Button) findViewById(R.id.accept_button);
        challengeButton = (Button) findViewById(R.id.challenge_button);
        nextStatePlusButton = (Button) findViewById(R.id.next_state_button);
        nameSpinner = (Spinner) findViewById(R.id.name_spinner);
        numSpinner = (Spinner) findViewById(R.id.number_spinner);
        explainSpinner = (Spinner) findViewById(R.id.explanation_spinner);
        betBox = (TextView) findViewById(R.id.current_bet_box);
        valueTextBox = (TextView) findViewById(R.id.current_value_box);
        bigBetBox = (TextView) findViewById(R.id.big_bet_text);
        printer = (TextView) findViewById(R.id.testPrintBox);


        // Section on Changing What numbers are available, depending on the name picked
        final ArrayAdapter<CharSequence> numAdapter1 = ArrayAdapter.createFromResource(this,
                R.array.number_picker_list, android.R.layout.simple_spinner_item);
        numAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<CharSequence> numAdapter2 = ArrayAdapter.createFromResource(this,
                R.array.number_picker_list2, android.R.layout.simple_spinner_item);
        numAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        nameListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (plusButtonPressed) {
                    plusButtonPressed = false;
                    return;
                }
                if (selectedItem.equals("Alpha") ||
                        selectedItem.equals("Bravo") ||
                        selectedItem.equals("Delta") ||
                        selectedItem.equals("Golf") ||
                        selectedItem.equals("Juliet") ||
                        selectedItem.equals("Lima") ||
                        selectedItem.equals("Mike")) {
                    numSpinner.setAdapter(numAdapter1);
                } else {
                    numSpinner.setAdapter(numAdapter2);
                }
                plusButtonPressed = false;
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) { }
        };

        nameSpinner.setOnItemSelectedListener(nameListener);


    }

    // Rolling all the dices, if they've been selected
    public void RollAction(View v) {

        for (int i = 0; i < vices.length; i++) {
            vices[i].roll();
        }
        valueTextBox.setText("Current Board: " + getPattern().getPublishableName());
        setEnables(1);
    }

    // Action to set the bid
    public void BidAction(View v) {

        String attemptStateString = nameSpinner.getSelectedItem().toString() + "#" + numSpinner.getSelectedItem().toString();
        BoardState attemptState = new BoardState();
        attemptState.fillFromName(attemptStateString);

//        printer.setText(betState.getStrinng() + "\n\n" + attemptState.getStrinng());

        if (betState.getReferenceValue() < attemptState.getReferenceValue()) {
            giveToast("Your bid has been accepted");
            betState = attemptState;

        } else {
            giveToast("Your bid must be greater than the current bid");
            return;
        }

        betBox.setText("Current Bet: " + betState.getPublishableName());
        bigBetBox.setText("Current Bet: " + betState.getPublishableName());
        setTheScreen(1);
        rollButton.setEnabled(true);

// If the maximum pattern is bid, then cannot accept and must challenge
        if (betState.getReferenceValue() == 140) {
            acceptButton.setEnabled(false);
        }

    }
    //A handler so the accept button does different things on whether it was challenged
    public void AcceptActionHandler(View v) {
        if (acceptActionMode == 2) {
            ResetAction(v);
            return;
        }
        AcceptAction(v);

    }
    public void AcceptAction(View v) {
        setTheScreen(2);
        setEnables(2);
    }

    // What happens when the challenge happens
    public void ChallengeAction(View v) {
        challengeButton.setEnabled(false);

        BoardState currentBoard = getPattern();
        setTheScreen(3);

        acceptButton.setText("Reset");
        acceptButton.setEnabled(true);
        acceptActionMode = 2;

        // Get instance of Vibrator from current Context
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern;

        if (betState.getReferenceValue() > currentBoard.getReferenceValue()) {

            pattern = new long[]{0, 200, 100, 200, 1000};

            giveToast("Correct: Your opponent loses a life");
        } else {

            pattern = new long[]{0, 500, 100};

            giveToast("Wrong: You Lose a Life");
        }
        vib.vibrate(pattern, -1);

    }

    // Resets the board after someone loses a life
    public void ResetAction(View v) {
        acceptActionMode = 1;
        acceptButton.setText("Accept");

        for (int i = 0; i < vices.length; i++) {

            vices[i].forcedRoll();
        }

        setTheScreen(2);
        setEnables(1);

        valueTextBox.setText("Current Board: " + getPattern().getPublishableName());
        betState = new BoardState();
        betBox.setText("Current Bet: ");

    }

    // Still working. The plus button should set the bid to the next biddable state, but sometimes it can cause problems with the spinner menus
    public void plusButtonAction(View v) {
        plusButtonPressed = false;

        // Finding the next biddable state that the button is supposed to display
        BoardState nextBiddableState = new BoardState();
        int nextBiddableValue = betState.getReferenceValue() + 1;

        if (nextBiddableValue > 140) {
            giveToast("Sorry, no higher value");
            return;
        }

        //Creating a temporary state from the nextBiddableValue so that things like names can be extracted
        nextBiddableState.fillFromValue(nextBiddableValue);
        String nextBiddableName = nextBiddableState.getNameOfState();
        String firstPart = nextBiddableName.substring(0, nextBiddableName.indexOf("#"));
        int secondPart = Integer.parseInt(nextBiddableName.substring(nextBiddableName.indexOf("#") + 1));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.main_types_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        nameSpinner.setAdapter(adapter);

        if (!firstPart.equals(null)) {
            int spinnerPosition = adapter.getPosition(firstPart);
            nameSpinner.setSelection(spinnerPosition);
        }

        // Section on Changing What numbers are available, depending on the name picked
        final ArrayAdapter<CharSequence> numAdapter1 = ArrayAdapter.createFromResource(this,
                R.array.number_picker_list, android.R.layout.simple_spinner_item);
        numAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<CharSequence> numAdapter2 = ArrayAdapter.createFromResource(this,
                R.array.number_picker_list2, android.R.layout.simple_spinner_item);
        numAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        if (firstPart.equals("Alpha") ||
                firstPart.equals("Bravo") ||
                firstPart.equals("Delta") ||
                firstPart.equals("Golf") ||
                firstPart.equals("Juliet") ||
                firstPart.equals("Lima") ||
                firstPart.equals("Mike")) {
            numSpinner.setAdapter(numAdapter1);
            int spinnerPosition = numAdapter1.getPosition("" + secondPart);
            numSpinner.setSelection(spinnerPosition);

        } else {
            numSpinner.setAdapter(numAdapter2);
            int spinnerPosition = numAdapter2.getPosition("" + secondPart);
            numSpinner.setSelection(spinnerPosition);

        }


/*        ArrayAdapter<CharSequence> numAdapter = (ArrayAdapter<CharSequence>) numSpinner.getAdapter();

        numSpinner.setAdapter(numAdapter);
*/


        plusButtonPressed = true;

    }

    //A quick method to just give a toast with any string
    public void giveToast(CharSequence theMessage) {
        Toast toast = Toast.makeText(this, theMessage, Toast.LENGTH_SHORT);
        toast.show();
    }

    // A very janky/duct-taped way to switch screens. Probably should be done with seperate layouts and intents, but that is for the future ... once i understand that better
    public void setTheScreen(int setting) {
        if (setting == 1) {

            rollButton.setVisibility(View.GONE);
            bidButton.setVisibility(View.GONE);

            acceptButton.setVisibility(View.VISIBLE);
            challengeButton.setVisibility(View.VISIBLE);

            valueTextBox.setVisibility(View.INVISIBLE);
            betBox.setVisibility(View.INVISIBLE);
            bigBetBox.setVisibility(View.VISIBLE);

            for (int i = 0; i < vices.length; i++) {
                vices[i].seen(false);
            }

            nameSpinner.setSelection(0);
            numSpinner.setSelection(0);
            explainSpinner.setSelection(0);

            nameSpinner.setVisibility(View.GONE);
            numSpinner.setVisibility(View.GONE);
            nextStatePlusButton.setVisibility(View.GONE);


        } else if (setting == 2) {
            rollButton.setVisibility(View.VISIBLE);
            bidButton.setVisibility(View.VISIBLE);

            acceptButton.setVisibility(View.GONE);
            challengeButton.setVisibility(View.GONE);

            valueTextBox.setVisibility(View.VISIBLE);
            betBox.setVisibility(View.VISIBLE);
            bigBetBox.setVisibility(View.GONE);

            for (int i = 0; i < vices.length; i++) {
                vices[i].seen(true);
            }

            nameSpinner.setSelection(0);
            numSpinner.setSelection(0);
            explainSpinner.setSelection(0);


            nameSpinner.setVisibility(View.VISIBLE);
            numSpinner.setVisibility(View.VISIBLE);
            nextStatePlusButton.setVisibility(View.VISIBLE);
        } else if (setting == 3) {


            valueTextBox.setVisibility(View.VISIBLE);
            betBox.setVisibility(View.VISIBLE);
            bigBetBox.setVisibility(View.GONE);

            for (int i = 0; i < vices.length; i++) {
                vices[i].seen(true);
            }

        }


    }

    // A section of code used to set various buttons as active depending on the input. Setting 1 = After a Roll. Setting 2 = Before a Roll
    public void setEnables(int setting) {
        if (setting == 1) {
            rollButton.setEnabled(false);
            challengeButton.setEnabled(true);


            for (int i = 0; i < vices.length; i++) {
                vices[i].enable(false);
            }
        } else if (setting == 2) {

            rollButton.setEnabled(true);
            challengeButton.setEnabled(true);


            for (int i = 0; i < vices.length; i++) {
                vices[i].enable(true);
            }
        } else {
            giveToast("Wrong int used in code setting");
        }
    }

    // Algorithm to work out what the current board state/pattern is on the board
    public BoardState getPattern() {

        BoardState toBeReturned = new BoardState();

        int[] diceValues = new int[vices.length];

        // Get Values from dice into a int array
        for (int i = 0; i < vices.length; i++) {
            int currentDiceValue = vices[i].getValue();
            diceValues[i] = currentDiceValue;
        }

        //Find repeats and put in diceValueRepeats
        int[] diceValueRepeats = new int[vices[0].getMax()];
        for (int i = 0; i < diceValues.length; i++) {
            diceValueRepeats[diceValues[i] - 1]++;
        }

        int biggestRepeat = 0;
        int biggestRepeatNum = 0;
        int secondBiggestRepeat = 0;
        int secondBiggestRepeatNum = 0;

        for (int i = 0; i < diceValueRepeats.length; i++) {
            if (diceValueRepeats[i] >= biggestRepeat) {
                biggestRepeat = diceValueRepeats[i];
                biggestRepeatNum = i + 1;
            }
        }

        for (int i = 0; i < diceValueRepeats.length; i++) {
            if (diceValueRepeats[i] >= secondBiggestRepeat && (i + 1 != biggestRepeatNum)) {
                secondBiggestRepeat = diceValueRepeats[i];
                secondBiggestRepeatNum = i + 1;
            }
        }

        int[] currentPattern;
        int power = biggestRepeatNum;
        if (secondBiggestRepeat > 1) {
            power += secondBiggestRepeatNum;
            currentPattern = new int[]{biggestRepeat, secondBiggestRepeat};
        } else {
            currentPattern = new int[]{biggestRepeat};
        }

        String refString = "";

        for (int i = 0; i < referenceBoards.length; i++) {
            if (Arrays.equals(currentPattern, referenceBoards[i].getPatternOfState())) {
                refString = referenceBoards[i].getNameOfState() + "#" + power;
            }
        }
        toBeReturned.fillFromName(refString);

        toBeReturned.setMessageOfState("You have " + biggestRepeat + " repeats of " + biggestRepeatNum + " and  " + secondBiggestRepeat + " repeats of " + secondBiggestRepeatNum);
        return toBeReturned;


    }

    // B/c I couldn't get sepearate files working properly. Eventually to be moved to its own class file once I learn how
    public class Dice {
        private int value;
        private int idval;
        private int min = 1;
        private int max = 8;
        ToggleButton button;


//        MainActivity mActivity;

        private boolean isSelected = false;

        public Dice(/*MainActivity activity, */int idNum) {
            value = 1;
            idval = idNum;
            //mActivity = activity;
        }

        public int getValue() {
            return value;
        }

        public int getId() {
            return idval;
        }

        public int getMin() {
            return min;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public int getMax() {
            return max;
        }

        public void setMin(int newMin) {
            min = newMin;
        }

        public void setMax(int newMax) {
            max = newMax;
        }

        public void setValue(int num) {
            value = num;
        }

        public void setId(int num) {
            idval = num;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public void updateDisplay() {

            button.setTextOff("" + value);
            button.setTextOn("" + value);
            button.setText("" + value);
        }

        public void seen(boolean boo) {
            if (boo) {
                button.setVisibility(View.VISIBLE);
            } else {
                button.setVisibility(View.GONE);
            }
        }

        public void roll() {
            if (button.isChecked()) {
                Random rand = new Random();
                value = min + rand.nextInt(max);
                updateDisplay();
//                button.setEnabled(false);
            }
        }

        public void forcedRoll() {

            button = (ToggleButton) findViewById(idval);
            Random rand = new Random();
            value = min + rand.nextInt(max);
            updateDisplay();
//            button.setChecked(false);
            valueTextBox.setText("Current Board: " + getPattern().getPublishableName());
        }

        public void enable(boolean boo) {
            button.setChecked(false);
            button.setEnabled(boo);

        }

    }

    // Boardstate is used to both bid and to figure out the current actual board state.
    public class BoardState {

        private String nameOfState;
        private int referenceValue;
        private int minReferenceValue;
        private int[] patternOfState;
        private String messageOfState;

        public String getNameOfState() {
            return nameOfState;
        }

        public String getPublishableName() {
            if (nameOfState == "") {
                return nameOfState;
            }

            String firstPart = nameOfState.substring(0, nameOfState.indexOf("#"));
            int secondPart = Integer.parseInt(nameOfState.substring(nameOfState.indexOf("#") + 1));
            return firstPart + " " + secondPart;
        }

        public int getReferenceValue() {
            return referenceValue;
        }

        public int getMinReferenceValue() {
            return minReferenceValue;
        }

        public int[] getPatternOfState() {
            return patternOfState;
        }

        public String getMessageOfState() {
            return messageOfState;
        }

        public void setMessageOfState(String mess) {
            messageOfState = mess;
        }

        public BoardState(String name, int val, int minVal, int[] pattern, String message) {
            nameOfState = name;
            referenceValue = val;
            minReferenceValue = minVal;
            patternOfState = pattern;
            messageOfState = message + "Name: " + nameOfState + ": " + referenceValue;
        }

        public BoardState() {
            nameOfState = "";
            referenceValue = 0;
            minReferenceValue = 0;
            patternOfState = new int[]{0};
            messageOfState = "Empty AF";
        }

        public void setBoardState(String name, int val, int minVal, int[] pattern, String message) {
            nameOfState = name;
            referenceValue = val;
            minReferenceValue = minVal;
            patternOfState = pattern;
            messageOfState = message + "Name: " + nameOfState + ": " + referenceValue;
        }

        public String getStrinng() {
            return nameOfState + "  " + referenceValue + "  " + minReferenceValue + "  " + Arrays.toString(patternOfState) + "\n" + messageOfState;
        }

        public void fillFromValue(int givenValue) {

            referenceValue = givenValue;
            for (int i = 0; i < referenceBoards.length; i++) {

                int min = referenceBoards[i].getMinReferenceValue();
                int max = referenceBoards[i].getReferenceValue();

                if (referenceValue >= min && referenceValue <= max) {

                    nameOfState = referenceBoards[i].getNameOfState();
                    minReferenceValue = min;
                    patternOfState = referenceBoards[i].getPatternOfState();
                    messageOfState = referenceBoards[i].getMessageOfState();

                    int minRollNum = referenceBoards[i].getPatternOfState().length;
                    int diff = givenValue - min + minRollNum;

                    nameOfState += "#" + diff;
                    return;


                }
            }
        }

        // Assumes given Name will be in format of Alpha#4 or Kilo#12 etc
        public void fillFromName(String givenName) {

            nameOfState = givenName;
            String firstPart = givenName.substring(0, givenName.indexOf("#"));
            int secondPart = Integer.parseInt(givenName.substring(givenName.indexOf("#") + 1));
            for (int i = 0; i < referenceBoards.length; i++) {

                if (firstPart.contentEquals(referenceBoards[i].getNameOfState())) {
                    minReferenceValue = referenceBoards[i].getMinReferenceValue();
                    patternOfState = referenceBoards[i].getPatternOfState();
                    messageOfState = referenceBoards[i].getMessageOfState();

                    int minRollNum = referenceBoards[i].getPatternOfState().length;

                    referenceValue = minReferenceValue + secondPart - minRollNum;
                    return;
                }
            }

        }


    }
}

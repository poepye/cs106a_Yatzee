/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	private void playGame() {
		/* You fill this in */
		
		int[] score = new int[nPlayers];
		int[] last_cat = new int[nPlayers];
		int[][] cat_score = new int[nPlayers][N_CATEGORIES]; // auto initialized as 0-0
		
		for (int round = 0; round < N_SCORING_CATEGORIES; round++) { // totally 13 rounds for each player
			
			for (int player = 0; player < nPlayers; player++) { // every round for each player
				// create an array to store the result of each player's roll
				dice = new int[N_DICE];
				display.printMessage(playerNames[player] + "'S TURN.");
				
				// 1st roll, all 6 dice are randomly created
				display.waitForPlayerToClickRoll(player + 1);
				for (int j = 0; j < N_DICE; j++)
					dice[j] = rgen.nextInt(1, 6);
				display.displayDice(dice);
				
				// 2/3 roll, only the selected dice changes
				for ( int i = 0; i < 2; i++) {
					display.printMessage(playerNames[player] + "'S ROLL " + (i+2));
					display.waitForPlayerToSelectDice();
					for (int j = 0; j < N_DICE; j++) {
						if (display.isDieSelected(j)) 
							dice[j] = rgen.nextInt(1, 6);
					}
					display.displayDice(dice);
				}
				display.printMessage(playerNames[player] + ", SELECT YOUR CATEGORY.");
				
				// select category			
				int category = display.waitForPlayerToSelectCategory();
				while(true) {
					if (category == last_cat[player]) {
						display.printMessage(playerNames[player] + ", YOU CANNOT CHOOSE THE CATEGORY YOU USED LAST ROUND !!!");
						category = display.waitForPlayerToSelectCategory();
					}else
						break;
				}
				last_cat[player] = category;
		
				switch (category) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6: // Ones - Sixes
					for (int i = 0; i < N_DICE; i++) {
						if (dice[i] == category)
							cat_score[player][category-1] += category;
					}
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 9:
				case 10: // THREE/FOUR_OF_A_KIND
					if (myCheckCategory(dice, category)) {
						for (int i = 0; i < N_DICE; i++)
							cat_score[player][category-1] += dice[i];
					}
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 11: // FULL_HOUSE
					if (myCheckCategory(dice, category))
						cat_score[player][category-1] = 25;
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 12: // SMALL_STRAIGHT
					if (myCheckCategory(dice, category))
						cat_score[player][category-1] = 30;
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 13: // LARGE_STRAIGHT
					if (myCheckCategory(dice, category))
						cat_score[player][category-1] = 40;
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 14: // YAHTZEE
					if (myCheckCategory(dice, category))
						cat_score[player][category-1] = 50;
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				case 15: // CHANCE
					for (int i = 0; i < N_DICE; i++)
						cat_score[player][category-1] += dice[i];
					display.updateScorecard(category, player+1, cat_score[player][category-1]);
					break;
				default:
					break;	
				}
	
				// Update total score
				score[player] = 0;
				for (int i = 0; i < N_CATEGORIES; i++) {
					score[player] += cat_score[player][i];
				}
				display.updateScorecard(TOTAL, player+1, score[player]);
			}
		}
	}
	
	private boolean myCheckCategory(int[] myDice, int myCategory) {
		boolean result = false;
		
		switch (myCategory) {
		case 9: { // THREE_OF_A_KIND
			int max = 0, count = 0;
			for (int i = 0; i < N_DICE; i++) {
				for (int j = i; j < N_DICE; j++) {
					if (myDice[i] == myDice[j])
						count++;
				}
				if (count > max) max = count;
				count = 0;
			}
			if (max >= 3)
				result = true;
			break;
			}
		case 10: { // FOUR_OF_A_KIND
			int max = 0, count = 0;
			for (int i = 0; i < N_DICE; i++) {
				for (int j = i; j < N_DICE; j++) {
					if (myDice[i] == myDice[j])
						count++;
				}
				if (count > max) max = count;
				count = 0;
			}
			if (max >= 4)
				result = true;
			break;
			}
		case 11: { // FULL_HOUSE
			int count1 = 0, count2 = 0;
			for (int i = 0; i < N_DICE; i++) {
				if (myDice[i] == myDice[0])
					count1++;
			}
			if (count1 == 2 || count1 == 3) {
				int val = 1;
				while (myDice[val] == myDice[0])
					val++;
				for (int j = val; j < N_DICE; j++) {
					if (myDice[val] == myDice[j])
						count2++;
				}
				if (count1 + count2 == N_DICE)
					result = true;
			}
			break;
			}
		case 14: { // YAHTZEE
			int count = 0;
			for (int i = 0; i < N_DICE; i++) {
				if (myDice[i] == myDice[0])
					count++;
			}
			if (count == N_DICE) result = true;
			break;
			}
		case 12: { // SMALL_STRAIGHT
			int[] flag = new int[6]; // array to record the dice value
			for (int i = 0; i < N_DICE; i++) {
				flag[myDice[i] - 1] = 1;
			}
			int sum = 0;
			for (int i = 0; i < 3; i++) {
				sum = flag[i] + flag[i+1] + flag[i+2] + flag[i+3];
				if (sum < 4) sum = 0; // no four consecutive numbers as of now
				if (sum >= 4) break; // find four consecutive numbers
			}
			if (sum > 0)
				result = true;
			break;
			}
		case 13: { // LARGE_STRAIGHT
			int[] flag = new int[6]; // array to record the dice value
			for (int i = 0; i < N_DICE; i++) {
				flag[myDice[i] - 1] = 1;
			}
			int sum = 0;
			for (int i = 0; i < 2; i++) {
				sum = flag[i] + flag[i+1] + flag[i+2] + flag[i+3] + flag[i+4];
				if (sum < 5) sum = 0; // no four consecutive numbers as of now
				if (sum >= 5) break; // find four consecutive numbers
			}
			if (sum > 0)
				result = true;
			break;
			}
		}
		
		return result;
	}

/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[] dice;
}

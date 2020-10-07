package Server.Master;

import java.util.ArrayList;


public class Player {
    public Player(String name, int numberofRoundsPlayed, ArrayList remainingCards, int totalScore) {
        this.name = name;
        this.numberofRoundsPlayed = numberofRoundsPlayed;
        this.remainingCards = remainingCards;
        this.totalScore = totalScore;
    }
    public Player() {
        this.name = "";
        this.numberofRoundsPlayed = 0;
        this.remainingCards = new ArrayList();
        this.totalScore = 0;
    }

    private String name;
    private int numberofRoundsPlayed;
    private ArrayList remainingCards;
    private int totalScore;

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", numberofRoundsPlayed=" + numberofRoundsPlayed +
                ", remainingCards=" + remainingCards +
                ", totalScore=" + totalScore +
                '}';
    }

    public ArrayList getRemainingCards() {
        return remainingCards;
    }

    public void setRemainingCards(ArrayList remainingCards) {
        this.remainingCards = remainingCards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberofRoundsPlayed() {
        return numberofRoundsPlayed;
    }

    public void setNumberofRoundsPlayed(int numberofRoundsPlayed) {
        this.numberofRoundsPlayed = numberofRoundsPlayed;
    }



    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
}

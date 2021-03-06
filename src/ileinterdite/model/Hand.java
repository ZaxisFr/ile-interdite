package ileinterdite.model;

import java.util.ArrayList;

public class Hand {

    public static final int NB_MAX_CARDS = 5;
    private ArrayList<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public void clearHand() {
        this.cards.clear();
    }

    public int getSize() {
        return cards.size();
    }
}
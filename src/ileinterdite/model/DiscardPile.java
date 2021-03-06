package ileinterdite.model;

import ileinterdite.util.Parameters;
import ileinterdite.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class DiscardPile {

    private ArrayList<Card> cards;
    private Utils.CardType cardType;

    public DiscardPile(Utils.CardType cardType) {
        this.cards = new ArrayList<>();
        this.cardType = cardType;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void clearPile() {
        this.cards.clear();
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public Utils.CardType getCardType() {
        return cardType;
    }

    public void shuffle() {
        if (Parameters.RANDOM) {
            Collections.shuffle(cards);
        }
    }
}
package ileinterdite.controller;

import ileinterdite.factory.DeckFactory;
import ileinterdite.factory.DiscardPileFactory;
import ileinterdite.model.*;
import ileinterdite.model.adventurers.Adventurer;
import ileinterdite.util.Parameters;
import ileinterdite.util.Utils;
import ileinterdite.view.PilesView;
import ileinterdite.view.DrawView;

import java.util.ArrayList;
import java.util.HashMap;

public class DeckController {

    private GameController controller; //< A reference to the main controller

    private HashMap<Utils.CardType, Deck> decks; //< The decks in the game
    private HashMap<Utils.CardType, DiscardPile> discardPiles; //< The discard piles in the game

    private DrawView drawView; //< The view when cards are drawn from a pile
    private PilesView pilesView; //< The view that shows piles and discard piles

    public DeckController(GameController c) {
        this.controller = c;
        drawView = new DrawView();
        pilesView = new PilesView();

        controller.getWindow().setPilesView(pilesView);

        initDecks(controller.getGridController().getGrid());
    }

    /**
     * Create all of the deck and cards
     */
    private void initDecks(Grid grid) {
        Deck deckTmp;
        DiscardPile discardPileTmp;
        this.decks = new HashMap<>();
        deckTmp = DeckFactory.deckFactory(Utils.CardType.FLOOD, grid);
        deckTmp.shuffle();
        decks.put(deckTmp.getCardType(), deckTmp);

        this.discardPiles = new HashMap<>();
        discardPileTmp = DiscardPileFactory.discardPileFactory(Utils.CardType.FLOOD);
        deckTmp.setDiscardPile(discardPileTmp);
        discardPiles.put(discardPileTmp.getCardType(), discardPileTmp);

        deckTmp = DeckFactory.deckFactory(Utils.CardType.TREASURE, grid);
        deckTmp.shuffle();
        decks.put(deckTmp.getCardType(), deckTmp);

        discardPileTmp = DiscardPileFactory.discardPileFactory(Utils.CardType.TREASURE);
        deckTmp.setDiscardPile(discardPileTmp);
        discardPiles.put(discardPileTmp.getCardType(), discardPileTmp);
    }

    public void drawTreasureCards(int nbCard, Adventurer adv) {
        ArrayList<Card> drewCards = this.decks.get(Utils.CardType.TREASURE).drawCards(nbCard);
        ArrayList<Card> tempAdventurerHandCards = new ArrayList<>(adv.getCards());
        adv.getHand().clearHand();
        for (Card drewCard : drewCards) {
            if (!drewCard.getCardName().equalsIgnoreCase("Montee des eaux")) {
                tempAdventurerHandCards.add(drewCard);
            } else {
                controller.getWaterScaleController().increaseWaterScale();
                if (!discardPiles.get(Utils.CardType.FLOOD).getCards().isEmpty()) {
                    this.discardPiles.get(Utils.CardType.FLOOD).shuffle();
                    ArrayList<Card> discardFloodCards = this.discardPiles.get(Utils.CardType.FLOOD).getCards();
                    this.decks.get(Utils.CardType.FLOOD).addAtTheTop(discardFloodCards);
                    this.discardPiles.get(Utils.CardType.FLOOD).clearPile();
                }
                this.discardPiles.get(Utils.CardType.TREASURE).addCard(drewCard);
            }
        }
        if (tempAdventurerHandCards.size() > Hand.NB_MAX_CARDS) {
            controller.getInterruptionController().initDiscard(adv, tempAdventurerHandCards, true);
        } else {
            for (Card card : tempAdventurerHandCards) {
                controller.getAdventurerController().giveCard(adv, card);
            }
        }

        drawView.setCardsToShow(drewCards);
    }

    /**
     * Change state of card et put in the discard pile if needed.
     */
    public void drawFloodCards(int nbCard) {
        ArrayList<Card> drewCards = decks.get(Utils.CardType.FLOOD).drawCards(nbCard);
        ArrayList<Adventurer> rescueList = new ArrayList<>();
        Utils.State state;
        for (Card card : drewCards) {
            FloodCard floodCard;
            floodCard = (FloodCard) card;
            Cell linkedCell = floodCard.getLinkedCell();
            state = linkedCell.getState();
            switch (state) {
                case NORMAL:
                    floodCard.getLinkedCell().setState(Utils.State.FLOODED);
                    discardPiles.get(Utils.CardType.FLOOD).addCard(floodCard);
                    break;
                case FLOODED:
                    floodCard.getLinkedCell().setState(Utils.State.SUNKEN);
                    rescueList.addAll(floodCard.getLinkedCell().getAdventurers());
                    break;
                case SUNKEN:
                    if (Parameters.LOGS) {
                        System.out.println("Carte inondation : " + card.getCardName() + " supprimée : Tuile déjà coulée");
                    }
                    break;
                default:
                    throw new RuntimeException();
            }

            controller.getGridController().getGridView().updateCell(linkedCell.getName(), linkedCell.getState());
        }

        if (!rescueList.isEmpty()) {
            controller.getInterruptionController().setRescueList(rescueList);
        }

        updatePiles();
        drawView.setCardsToShow(drewCards);
    }

    public Deck getDeck(Utils.CardType type) {
        return decks.get(type);
    }

    public DiscardPile getDiscardPile(Utils.CardType type) {
        return discardPiles.get(type);
    }

    public void updatePiles() {
        pilesView.updatePile(Utils.CardType.TREASURE, getDiscardPile(Utils.CardType.TREASURE).getCards());
        pilesView.updatePile(Utils.CardType.FLOOD, getDiscardPile(Utils.CardType.FLOOD).getCards());
    }
}

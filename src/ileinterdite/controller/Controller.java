package ileinterdite.controller;

import ileinterdite.factory.BoardFactory;
import ileinterdite.model.Cell;
import ileinterdite.model.Deck;
import ileinterdite.model.DiscardPile;
import ileinterdite.model.Grid;
import ileinterdite.model.adventurers.Adventurer;
import ileinterdite.model.adventurers.Engineer;
import ileinterdite.model.adventurers.Navigator;
import ileinterdite.util.*;
import ileinterdite.util.Utils.Action;
import ileinterdite.util.Utils.Pawn;
import ileinterdite.view.AdventurerView;
import ileinterdite.view.GridView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements IObserver<Message> {
    private ControllerMainMenu controllerMainMenu;

    private Grid grid;
    private Utils.State[][] cellStates;

    private ArrayList<Adventurer> players;
    private Adventurer currentAdventurer;
    private Adventurer currentActionAdventurer;

    private Collection<Deck> decks;
    private Collection<DiscardPile> discardPiles;

    private GridView gridView;
    private AdventurerView adventurerView;

    // Turn state
    private Action selectedAction;
    private static final int NB_ACTIONS_PER_TURN = 3;
    private int remainingActions;

    private boolean powerEngineer = false;

    public Controller(ControllerMainMenu cm, AdventurerView view, GridView gview) {
        this.controllerMainMenu = cm;

        Object[] builtStuff;
        builtStuff = BoardFactory.boardFactory();
        this.adventurerView = view;
        this.gridView = gview;
        this.players = (ArrayList<Adventurer>) builtStuff[0];
        this.grid = new Grid((Cell[][])builtStuff[1],null);
        this.definePlayer(players);

        this.initBoard();
        this.gridView.showGrid(this.grid.getCells());
        this.gridView.showAdventurers(players);

        //TODO Add the pawns placement on cell

        nextAdventurer();
    }

    private void changeCurrentAdventurer() {
        players.add(players.remove(0));
        currentAdventurer = players.get(0);
        Pawn currentPawn = currentAdventurer.getPawn();
        adventurerView.setColor(currentPawn.getColor(), currentPawn.getTextColor());
        adventurerView.setText(currentAdventurer.getName(), currentAdventurer.getClassName());
    }

    /**
     * Lance les actions pour le deplacement de l'aventurier.
     * puis l'interaction avec l'interface
     *
     * @param adventurer
     */
    public void initMovement(Adventurer adventurer) {
        currentActionAdventurer = adventurer;
        cellStates = adventurer.getAccessibleCells();
        gridView.showSelectableCells(cellStates);
    }

    /**
     * Lance les actions pour le deplacement de l'aventurier.
     * puis l'interaction avec l'interface
     *
     * @param adventurer
     */
    public void initDryable(Adventurer adventurer) {
        currentActionAdventurer = adventurer;
        cellStates = adventurer.getDryableCells();
        gridView.showSelectableCells(cellStates);
    }

    /**
     * Renvoie un boolean si la case choisie par l'utilisateur est accesible
     *
     * @param x,y
     * @return boolean
     */
    public boolean isCellAvailable(int x, int y) {
        return cellStates[y][x] == Utils.State.ACCESSIBLE;
    }

    /**
     * @param x
     * @param y deplacement de l'avanturier en X,Y et actualisation de la vue
     */
    public void movement(int x, int y){
        currentActionAdventurer.move(x,y);
        gridView.updateAdventurer(currentActionAdventurer);
    }

    /**
     * Asséchement de la grille en X,Y et actualisation de la vue
     *
     * @param x
     * @param y
     */
    public void dry(int x, int y){
        this.getGrid().dry(x,y);
        gridView.updateCell(x, y, Utils.State.NORMAL);
    }

    /**
     * Split the message contents to retrieve the position
     *
     * @param msg Message The message received from the view
     * @return Tuple&lt;Integer, Integer&gt;
     */
    private Tuple<Integer, Integer> getPositionFromMessage(String msg) {
        Pattern p = Pattern.compile("(\\d).+(\\d)");
        Matcher m = p.matcher(msg);
        if (m.find()) {
            int x = Integer.valueOf(m.group(1));
            int y = Integer.valueOf(m.group(2));
            return new Tuple<>(x, y);
        }

        return null; // This should never happen, as coordinates are sent by components
    }

    private boolean validateCellAction(int x, int y) {
        if (x >= 0 && y >= 0 && x < Grid.WIDTH && y < Grid.HEIGHT && isCellAvailable(x, y)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean handleAction(String msg) {
        Tuple<Integer, Integer> coords = getPositionFromMessage(msg);
        switch (selectedAction) {
            case MOVE:
                if (coords != null) {
                    int x = coords.x - 1;
                    int y = coords.y - 1;
                    if (validateCellAction(x, y)) {
                        movement(x, y);
                        reduceNbActions();
                        powerEngineer = false;
                        return true;
                    }
                }
                break;
            case DRY:
                if (coords != null) {
                    int x = coords.x - 1;
                    int y = coords.y - 1;
                    if (validateCellAction(x, y)) {
                        dry(x, y);
                        if (!powerEngineer) {
                            reduceNbActions();
                            if (currentActionAdventurer instanceof Engineer) {
                                powerEngineer = true;
                            }
                        } else {
                            powerEngineer = false;
                        }
                        return true;
                    }
                }
                break;
            case GIVE_CARD:
                break;
            case GET_TREASURE:
                break;
        }

        return false;
    }

    public void drawTreasureCards() {

    }

    public void drawFloodCards() {

    }

    public void nextAdventurer() {
        changeCurrentAdventurer();
        currentAdventurer.newTurn();
        setNbActions(NB_ACTIONS_PER_TURN);
        selectedAction = null;

        gridView.newTurn();
    }

    /**
     * @param nb
     */
    public void setNbActions(int nb) {
        this.remainingActions = Math.max(nb, NB_ACTIONS_PER_TURN);
    }

    public void reduceNbActions() {
        this.remainingActions--;
    }

    /**
     * Get the adventurer with the given class name.
     * @return Null if not found
     */
    private Adventurer findAdventurerByClassName(String name) {
        Adventurer adv;
        int i = 0;
        do {
            adv = players.get(i);
            i++;
        } while (i < players.size() && !name.equalsIgnoreCase(adv.getClassName()));

        return (name.equalsIgnoreCase(adv.getClassName())) ? adv : null;
    }


    @Override
    public void update(IObservable<Message> o, Message m) {
        switch (m.action) {
            case NAVIGATOR_CHOICE:
                selectedAction = Action.MOVE;
                // The message contains a string with the format "ClassName (PlayerName)"
                currentActionAdventurer = findAdventurerByClassName(m.message.substring(0, m.message.indexOf(' ')));
                if (currentActionAdventurer != null) {
                    initMovement(currentActionAdventurer);
                }
                break;
            case MOVE:
                if (currentAdventurer instanceof Navigator) {
                    adventurerView.showAdventurers(players);
                } else {
                    selectedAction = Action.MOVE;
                    initMovement(currentAdventurer);
                }
                break;
            case DRY:
                selectedAction = Action.DRY;
                initDryable(currentAdventurer);
                break;
            case GIVE_CARD:
                selectedAction = Action.GIVE_CARD;
                break;
            case GET_TREASURE:
                selectedAction = Action.GET_TREASURE;
                break;
            case VALIDATE_ACTION:
                if (selectedAction != null && handleAction(m.message)) {
                    currentActionAdventurer = null;
                    selectedAction = null;
                }
                break;
            case END_TURN:
                nextAdventurer();
                break;
            case CANCEL_ACTION:
                selectedAction = null;
                break;
        }

        if (remainingActions == 0 && (!powerEngineer || selectedAction != Action.DRY && selectedAction != null)) {
            nextAdventurer();
        }
    }

    private Grid getGrid() {
        return grid;
    }

    private void initBoard() {
        for (Adventurer adventurer : players) {
            adventurer.setGrid(this.grid);
        }

        Cell[][] cells = this.getGrid().getCells();
        for (int j = 0; j < Grid.HEIGHT; j++) {
            for (int i = 0; i < Grid.WIDTH; i++) {
                if(players.contains(cells[j][i].getAdventurerSpawn())) {
                    cells[j][i].spawnAdventurer(i, j);
                }
            }
        }
    }

    private ArrayList<Adventurer> randomPlayer(ArrayList<Adventurer> players, int nbPlayers) {
        Collections.shuffle(players);
        while (players.size() > nbPlayers) {
            players.remove(players.size() - 1);
        }
        return players;
    }

    public ArrayList<Adventurer> definePlayer(ArrayList<Adventurer> players){
        
        ArrayList<String> playersName = controllerMainMenu.getPlayersName();

        players = randomPlayer(players, playersName.size());
        for (int i = 0; i < playersName.size(); i++) {
            players.get(i).setName(playersName.get(i));
        }
        return players;
    }
}

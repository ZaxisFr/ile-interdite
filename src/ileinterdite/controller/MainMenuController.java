package ileinterdite.controller;

import ileinterdite.factory.BoardFactory;
import ileinterdite.util.IObservable;
import ileinterdite.util.IObserver;
import ileinterdite.util.Parameters;
import ileinterdite.util.StartMessage;
import ileinterdite.view.PlayerSelectionView;

import java.util.ArrayList;

public class MainMenuController implements IObserver<StartMessage> {

    private ArrayList<String> playerName = new ArrayList<>();
    private boolean playerSelection = false;
    private int difficulty = 0;

    public ArrayList<String> getPlayersName() {
        return playerName;
    }

    @Override
    public void update(IObservable<StartMessage> o, StartMessage message) {
        if (!playerSelection) {
            playerName = message.playerName;
            Parameters.LOGS = message.logOption;
            Parameters.DEMOMAP = message.demoOption;
            Parameters.RANDOM = message.randomOption;
            difficulty = message.difficulty;

            BoardFactory.initBoardFactory();
            if (!Parameters.RANDOM) {
                new PlayerSelectionView(BoardFactory.getAdventurers(), playerName.size()).addObserver(this);
                playerSelection = true;
            } else {
                new GameController(this, difficulty);
            }
        } else {
            BoardFactory.setAdventurers(message.adventurers);
            new GameController(this, difficulty);
        }
    }
}

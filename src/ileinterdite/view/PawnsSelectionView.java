package ileinterdite.view;

import ileinterdite.model.adventurers.Adventurer;
import ileinterdite.util.IObservable;
import ileinterdite.util.IObserver;
import ileinterdite.util.Message;
import ileinterdite.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class PawnsSelectionView implements IObservable<Message> {

    private final CopyOnWriteArrayList<IObserver<Message>> observers;

    private JFrame window;
    private JPanel mainPanel;
    private JButton cancelButton;
    private JButton validateButton;
    private JPanel buttonPanel;
    private ArrayList<JLabel> pawnsIco;
    private ArrayList<Integer> pawnsSelected;
    private ArrayList<Adventurer> adventurers;
    private ArrayList<BufferedImage> normalIco;
    private ArrayList<BufferedImage> selectedIco;
    private JPanel choicePanel;


    public PawnsSelectionView() {
        pawnsSelected = new ArrayList<>();
        adventurers = new ArrayList<>();
        observers = new CopyOnWriteArrayList<>();
        pawnsIco = new ArrayList<>();

        normalIco = new ArrayList<>();
        selectedIco = new ArrayList<>();

        SwingUtilities.invokeLater(() -> {
            mainPanel = new JPanel(new BorderLayout());
            this.initFrame();
            validateButton = new JButton("Valider");
            validateButton.addActionListener(actionEvent -> {
                Message m = new Message(Utils.Action.ADVENTURER_CHOICE, buildStringMessage(pawnsSelected));

                mainPanel.remove(choicePanel);
                windowClose();
                notifyObservers(m);
            });

            cancelButton = new JButton("Annuler");
            cancelButton.addActionListener(actionEvent -> {
                Message m = new Message(Utils.Action.CANCEL_ACTION, null);
                windowClose();
                notifyObservers(m);
            });

            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            buttonPanel.add(validateButton, c);
            c.gridx = 2;
            c.gridy = 0;
            buttonPanel.add(cancelButton, c);

            window.add(mainPanel);
        });
    }

    public void update(ArrayList<Adventurer> availableAdventurers, int nbAdventurers, boolean showValidation, boolean showCancel) {
        resetIcons();

        SwingUtilities.invokeLater(() -> {

            validateButton.setEnabled(showValidation);
            validateButton.setVisible(showValidation);

            cancelButton.setEnabled(showCancel);
            cancelButton.setVisible(showCancel);
        });
        adventurers = new ArrayList<>(availableAdventurers);

        final int adventurerSize = adventurers.size();
        SwingUtilities.invokeLater(() -> choicePanel = new JPanel(new GridLayout(1, adventurerSize - 1)));

        for (Adventurer adventurer : adventurers) {

            String path = "pions/" + "pion" + adventurer.getPawn().toString();

            BufferedImage img = Utils.loadImage(path + ".png");
            if (img != null) {
                selectedIco.add(img);
                final ImageIcon icon = new ImageIcon(img.getScaledInstance(img.getWidth() / 2, img.getHeight() / 2, Image.SCALE_SMOOTH));
                final ArrayList<BufferedImage> normalImg = new ArrayList<>(normalIco);
                final ArrayList<BufferedImage> selectedImg = new ArrayList<>(selectedIco);
                SwingUtilities.invokeLater(() -> {
                    JPanel pawnPanel = new JPanel(new GridLayout(2, 1));
                    JLabel pawnIco = new JLabel("", SwingConstants.CENTER);
                    pawnIco.setIcon(icon);
                    pawnIco.addMouseListener(new MouseListener() {

                        @Override
                        public void mouseClicked(MouseEvent mouseEvent) {
                            int pawnIndex;

                            JLabel component = (JLabel) mouseEvent.getComponent();
                            pawnIndex = pawnsIco.indexOf(component);
                            if (pawnIndex != -1) {
                                JLabel pawn = pawnsIco.get(pawnIndex);
                                if (pawnsSelected.contains(pawnIndex)) {
                                    pawn.setIcon(new ImageIcon(normalImg.get(pawnIndex).getScaledInstance(normalImg.get(pawnIndex).getWidth() / 2, normalImg.get(pawnIndex).getHeight() / 2, Image.SCALE_SMOOTH)));
                                    pawnsSelected.remove((Integer) pawnIndex);
                                } else {
                                    pawn.setIcon(new ImageIcon(selectedImg.get(pawnIndex).getScaledInstance(selectedImg.get(pawnIndex).getWidth() / 2, selectedImg.get(pawnIndex).getHeight() / 2, Image.SCALE_SMOOTH)));
                                    pawnsSelected.add(pawnIndex);
                                }
                            }

                            if (pawnsSelected.size() == nbAdventurers) {
                                Message m = new Message(Utils.Action.ADVENTURER_CHOICE, buildStringMessage(pawnsSelected));

                                mainPanel.remove(choicePanel);
                                windowClose();
                                notifyObservers(m);

                            }
                        }

                        @Override
                        public void mousePressed(MouseEvent mouseEvent) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent mouseEvent) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent mouseEvent) {
                        }

                        @Override
                        public void mouseExited(MouseEvent mouseEvent) {
                        }
                    });

                    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

                    pawnsIco.add(pawnIco);

                    pawnPanel.add(pawnIco);
                    pawnPanel.add(new JLabel(adventurer.getName(), SwingConstants.CENTER));


                    choicePanel.add(pawnPanel);
                    mainPanel.add(choicePanel, BorderLayout.CENTER);

                    choicePanel.repaint();
                    mainPanel.revalidate();
                    window.setVisible(true);
                });
            }
        }

        this.createIcons();
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < pawnsIco.size(); i++) {
                pawnsIco.get(i).setIcon(new ImageIcon(normalIco.get(i).getScaledInstance(normalIco.get(i).getWidth() / 2, normalIco.get(i).getHeight() / 2, Image.SCALE_SMOOTH)));
            }
        });
    }

    /**
     * window Initialisation set the look, the size, ect
     */
    private void initFrame() {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(600, 200);
        window.setTitle("Selection de Pions");
        window.setLocationRelativeTo(null);
        window.setUndecorated(true);


    }

    /**
     * Convert a index ArrayList to String with adventurer separated by /
     */
    private String buildStringMessage(ArrayList<Integer> pawnsSelected) {
        StringBuilder stringMessage = new StringBuilder();
        for (Integer i : pawnsSelected) {
            stringMessage.append("/").append(adventurers.get(i).getClassName());
        }
        return stringMessage.toString();
    }

    /**
     * Hide the window
     */
    private void windowClose() {

        window.setVisible(false);

    }

    private void createIcons() {
        for (BufferedImage img : selectedIco) {
            normalIco.add(Utils.deepCopy(img));
            Utils.setOpacity(normalIco.get(normalIco.size() - 1), 128);

        }
    }

    /**
     * clear window last usage
     */
    private void resetIcons() {
        pawnsIco.clear();
        pawnsSelected.clear();
        normalIco.clear();
        selectedIco.clear();
    }

    @Override
    public void addObserver(IObserver<Message> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(IObserver<Message> o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Message message) {
        for (IObserver<Message> o : observers) {
            o.update(this, message);
        }
    }
}

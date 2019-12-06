package ro.mta.se.lab.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.json.JSONException;
import ro.mta.se.lab.model.Model;
import ro.mta.se.lab.view.View;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Model model;
    private View view;

    private Double currencyRate;
    private ArrayList<String> coinListFrom;
    private ToggleGroup toggleGroup;
    private Thread myThread;
    private Boolean threadOpen;
    private String inputValue;


    @FXML
    private ComboBox<String> comboBoxFrom;
    @FXML
    private ComboBox<String> comboBoxTo;
    @FXML
    private TextField textValueReal;
    @FXML
    private TextField textValueThread;
    @FXML
    private TextField textValueTo;
    @FXML
    private TextArea textArea;
    @FXML
    private Text textRate;
    @FXML
    private Toggle toggleRealTime;
    @FXML
    private Toggle toggleThread;

    public Controller(Model model) {
        this.model = model;
        this.myThread = null;
        this.threadOpen = false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initComboBoxes();
        initTextValueListeners();
        setToggleBehavior();

        comboBoxToAction();
        comboBoxFromAction();

        textValueDisable(true);
        textArea.setEditable(false);
        textValueTo.setEditable(false);
    }

    /**
     * disable or enable all textValue boxes based on state parameter
     */
    private void textValueDisable(Boolean state) {
        textValueReal.setDisable(state);
        textValueThread.setDisable(state);
        textValueTo.setDisable(state);
    }

    /**
     * initialize toggle group and default selected one. Call toggleListener
     */
    private void setToggleBehavior() {
        this.toggleGroup = new ToggleGroup();
        toggleRealTime.setToggleGroup(toggleGroup);
        toggleThread.setToggleGroup(toggleGroup);
        toggleThread.setSelected(true);
        toggleListener();
    }

    /**
     * Set listener for toggleGroup
     * Real time selected -> enable proper listener for real time method
     * Thread selected -> enable proper listener for thread method
     */
    private void toggleListener() {
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    if (newValue.equals(toggleGroup.getToggles().get(0))) {
                        textValueThread.setVisible(false);
                        textValueReal.setVisible(true);
                        textValueReal.clear();
                        textValueThread.clear();
                        textValueTo.clear();
                    } else {
                        textValueReal.setVisible(false);
                        textValueThread.setVisible(true);
                        textValueReal.clear();
                        textValueThread.clear();
                        textValueTo.clear();
                    }
                }

            }
        });
    }

    private void populateTextArea() {
        for (int i = 0; i < model.getCoinList().size(); i++) {
            textArea.appendText(model.getCoinList().get(i) + "   \t:\t" + model.getCurrencyList().get(i));
            textArea.appendText("\n");
        }
    }

    /**
     * Set listener for first comboBox - comboBoxFrom
     * When item selected populate second comboBox with Api response
     * Populate textArea with all coins and currencies
     */
    private void comboBoxFromAction() {
        comboBoxFrom.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String selectedModel = comboBoxFrom.getValue();
                model.clearCoinList();
                model.clearCurrencyList();
                try {
                    model.getCurrency(selectedModel);

                    ObservableList<String> listCurrency = FXCollections.observableArrayList(model.getCoinList());
                    comboBoxTo.setItems(listCurrency);
                    comboBoxTo.setValue("");

                    textArea.clear();
                    textValueReal.clear();
                    textValueThread.clear();
                    textValueTo.clear();

                    populateTextArea();
                } catch (IOException | JSONException e) {
                    System.out.print(e.getMessage());
                }
                textValueDisable(true);
            }
        });
    }

    /**
     * Set listener for second comboBox - comboBoxFrom
     * Displays currency Rate for selected items
     */
    private void comboBoxToAction() {
        comboBoxTo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String selectedModel = comboBoxTo.getValue();
                for (int i = 0; i < model.getCoinList().size(); i++) {
                    if (model.getCoinList().get(i).equals(selectedModel)) {
                        textRate.setText(model.getCurrencyList().get(i));
                        currencyRate = Double.parseDouble(model.getCurrencyList().get(i));
                    }
                }
                textValueThread.clear();
                textValueReal.clear();
                textValueTo.clear();
                textValueDisable(false);
            }
        });
    }

    /**
     * Set listeners for Real Time method and Thread method
     * Real - it instantly calculates input * rate
     * Thread - use MyThread for calculation
     */
    private void initTextValueListeners() {
        textValueReal.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                inputValue = newValue;
                if (String.valueOf(newValue).matches("[0-9]+[.]?[0-9]*")) {
                    getResult();
                } else if (newValue.equals("")) {
                    textValueTo.setText("");
                } else {
                    textValueTo.setText("Wrong input");
                }
            }
        });

        textValueThread.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue.matches("[0-9]+[.]?[0-9]*") && !oldValue.equals(newValue) && !newValue.equals("")) {
                    inputValue = newValue;
                    threadOpen = true;
                    if (myThread != null) {
                        synchronized (myThread) {
                            myThread.notify();
                        }
                    } else {
                        myThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (threadOpen) {
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    getResult();
                                    if (myThread != null) {
                                        synchronized (myThread) {
                                            try {
                                                myThread.wait();
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                            }
                                        }
                                    }
                                }
                            }
                        });

                        myThread.start();
                    }
                } else if (newValue.equals("")) {
                    textValueTo.setText("");
                } else {
                    textValueTo.setText("Wrong input");
                }
            }
        });
    }

    /**
     * Display result with precision
     */
    private void getResult() {
        Double result = Double.parseDouble(String.valueOf(inputValue)) * currencyRate;
        DecimalFormat dec = new DecimalFormat("#0.00");

        textValueTo.setText(dec.format(result));
    }

    /**
     * initialize comboBoxes for 'RON' base
     */
    private void initComboBoxes() {
        ObservableList<String> listTo = FXCollections.observableArrayList(model.getCoinList());
        comboBoxTo.setItems(listTo);

        populateTextArea();
        this.coinListFrom = new ArrayList<>();

        coinListFrom = model.getCoinList();

        ArrayList<String> coinCollection = new ArrayList<>(coinListFrom);
        coinCollection.add(0, "RON");
        Collections.sort(coinCollection);

        ObservableList<String> listFrom = FXCollections.observableArrayList(coinCollection);
        comboBoxFrom.setItems(listFrom);
        comboBoxFrom.setValue("RON");
    }

}

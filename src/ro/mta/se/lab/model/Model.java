package ro.mta.se.lab.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Model {

    private ArrayList<String> coinList;
    private ArrayList<String> currencyList;

    public Model() {
        initializeCurrencyList();
    }

    /**
     * Call getCurrency for 'RON' base
     */
    private void initializeCurrencyList() {
        this.coinList = new ArrayList<>();
        this.currencyList = new ArrayList<>();
        try {
            getCurrency("RON");
        } catch (IOException | JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Call api for base and initialize coins list and currencies list
     */
    public void getCurrency(String base) throws IOException, JSONException {

        String urlString = "https://api.fixer.io/latest?base=" + base;
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            int character;
            while ((character = rd.read()) != -1) {
                result.append((char) character);
            }
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONObject jsonRates = (JSONObject) jsonObject.get("rates");
            String[] rateList = jsonRates.toString().split(",");

            for (int count = 0; count < jsonRates.length(); count++) {
                String first = rateList[count].substring(rateList[count].indexOf("\"") + 1, rateList[count].indexOf("\"", rateList[count].indexOf("\"") + 1));
                coinList.add(first);
                String second = rateList[count].substring(rateList[count].indexOf(":") + 1);
                currencyList.add(second);
            }
            checkCurrency();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void checkCurrency() {
        for (int i = 0; i < currencyList.size(); i++) {
            if (currencyList.get(i).contains("}")) {
                String a = currencyList.get(i).substring(0, currencyList.get(i).length() - 1);
                currencyList.set(i, a);
            }
        }
    }


    public ArrayList<String> getCoinList() {
        return coinList;
    }

    public ArrayList<String> getCurrencyList() {
        return currencyList;
    }


    public void clearCurrencyList() {
        this.currencyList.clear();
    }

    public void clearCoinList() {
        this.coinList.clear();
    }

}

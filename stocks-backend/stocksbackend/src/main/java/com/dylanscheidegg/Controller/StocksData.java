package com.dylanscheidegg.Controller;

import java.util.HashMap;

import com.dylanscheidegg.DataHandling.UploadDownloadHandling;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// Annotation
@Controller
public class StocksData {
    //https://stackoverflow.com/questions/56328474/origin-http-localhost4200-has-been-blocked-by-cors-policy-in-angular7
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping("/GetData/{id}")
    @ResponseBody
    public JSONArray Data(@PathVariable("id") String id) {
        final String url = "https://finviz.com/search.ashx?p=" + id;
        System.out.println(url);
        HashMap<String, String> stockHold = new HashMap<String, String>();
        UploadDownloadHandling DataHandling = new UploadDownloadHandling();

        try {
            final Document document = Jsoup.connect(url).get();
            System.out.println(document.select("p").text());

            if (document.select("p").text()
                    .contains("Sorry, we couldn\u2019t find anything related to \u201CASDASDF\u201D.")) {
                System.out.println("Document is null");
            } else {
                final String urlMain = "https://finviz.com/quote.ashx?t=" + id + "&p=d";
                final Document documentMain = Jsoup.connect(urlMain).get();

                try {
                    for (Element row : documentMain.select("table.snapshot-table2 tr")) {
                        // final String ticker = row.select("td:nth-of-type(3)").text();
                        String holdKey = "";
                        for (Element td : row.select("td")) {
                            if (holdKey == "") {
                                holdKey = td.text();
                            } else {
                                // If dupe key add a 1 to the end
                                if (stockHold.containsKey(holdKey)) {
                                    holdKey = holdKey + " -1";
                                }
                                stockHold.put(holdKey, td.text());
                                holdKey = "";
                            }
                        }
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (stockHold.isEmpty()) {
            stockHold.put("Error", "No Data Found");
        }
        JSONObject stockDataNew = new JSONObject(stockHold);
        DataHandling.UploadData(stockDataNew, id);
        JSONObject stockDataOld = new JSONObject(DataHandling.DownloadData(id));

        // create jsonobject array
        JSONArray stockData = new JSONArray();
        stockData.add(stockDataNew);
        stockData.add(stockDataOld);

        return stockData;
    }
}

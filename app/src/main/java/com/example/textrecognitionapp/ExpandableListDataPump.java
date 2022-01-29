package com.example.textrecognitionapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {

    // TODO: This should be change when connecting to actual database
    // currently it is hard coded data for testing
    // the format of the data is: [<item grouped in year>]
    // example database structure for the item [bitmap,datetime,float(A1C in %),float(A1C in mmol/mol)]
    // [imageTaken, "2019-01-01 24:12", 0.8, 0.8]
    public static HashMap<String, List<List<String>>> getData() {

        HashMap<String, List<List<String>>> expandableListDetail = new HashMap<String, List<List<String>>>();

        List<List<String>> y2022 = new ArrayList<List<String>>();
        y2022.add(Arrays.asList("ImageBitmapHere","Dec 13", "01:26pm","5.2/33.6"));
        y2022.add(Arrays.asList("ImageBitmapHere","Nov 12", "02:12pm","2.2/33.6"));
        y2022.add(Arrays.asList("ImageBitmapHere","Mac 11", "12:40pm","5.1/33.6"));
        y2022.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));
        y2022.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));
        y2022.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));

        List<List<String>> y2021 = new ArrayList<List<String>>();
        y2021.add(Arrays.asList("ImageBitmapHere","Jul 9", "11:12am","1.1/33.6"));
        y2021.add(Arrays.asList("ImageBitmapHere","Jun 4", "09:19am","4.2/33.6"));
        y2021.add(Arrays.asList("ImageBitmapHere","May 2", "01:12pm","6.6/33.6"));
        y2021.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));
        y2021.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));

        List<List<String>> y2020 = new ArrayList<List<String>>();
        y2020.add(Arrays.asList("ImageBitmapHere","Sep 3", "12:18pm","5.7/33.6"));
        y2020.add(Arrays.asList("ImageBitmapHere","Jul 2", "01:52pm","4.3/33.6"));
        y2020.add(Arrays.asList("ImageBitmapHere","Jan 13", "06:22am","5.9/33.6"));
        y2020.add(Arrays.asList("ImageBitmapHere","Jan 10", "10:17am","3.8/33.6"));

        List<List<String>> y2019 = new ArrayList<List<String>>();
        y2019.add(Arrays.asList("ImageBitmapHere","Sep 3", "12:18pm","5.7/33.6"));
        y2019.add(Arrays.asList("ImageBitmapHere","Jul 2", "01:52pm","4.3/33.6"));
        y2019.add(Arrays.asList("ImageBitmapHere","Jan 13", "06:22am","5.9/33.6"));

        expandableListDetail.put("Year 2022 Records", y2022);
        expandableListDetail.put("Year 2021 Records", y2021);
        expandableListDetail.put("Year 2020 Records", y2020);
        expandableListDetail.put("Year 2019 Records", y2019);
        return expandableListDetail;
    }
}

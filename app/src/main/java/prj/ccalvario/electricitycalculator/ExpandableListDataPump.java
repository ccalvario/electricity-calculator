package prj.ccalvario.electricitycalculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static LinkedHashMap<String, List<String>> getData() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        int categoriesLength = SaveData.getInstance().getLengthCatSampleList();
        for(int i = 0; i < categoriesLength; i++){
            List<String> items = new ArrayList<String>();
            int itemsLength = SaveData.getInstance().getLenghtSampleItems(i);
            for(int j = 0; j < itemsLength; j++){
                SampleItem sampleItem = SaveData.getInstance().getSampleItem(i, j);
                items.add(sampleItem.getName() + "    " + sampleItem.getWatts() + " W");
            }
            expandableListDetail.put(SaveData.getInstance().getCategoryName(i), items);
        }

        return expandableListDetail;
    }
}
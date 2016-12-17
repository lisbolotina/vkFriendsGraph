package com.lisa.vk;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.friends.responses.GetResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    private static VkApiClient VK = new VkApiClient(new HttpTransportClient());

    public static void main(String[] args) {
        writeToFile(sort(friendsGraph(Integer.valueOf(args[0]))), args[1]);
    }

    private static void writeToFile(Map<Integer, List<Integer>> data, String pathToFile) {
        try(PrintWriter pw = new PrintWriter(new File(pathToFile))) {

            for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
                StringBuilder builder = new StringBuilder();
                builder.append(entry.getKey()).append(": ");
                for (Integer id : entry.getValue()) {
                    builder.append(id).append(" ");
                }

                builder.append("\n");
                pw.write(builder.toString());
            }

        } catch (FileNotFoundException e) {
            System.out.println("Can't write to " + pathToFile);
        }
    }

    private static Map<Integer, List<Integer>> sort(Map<Integer, List<Integer>> friendsGraph) {
        Set<Integer> originalFriends = friendsGraph.keySet();

        Map<Integer, List<Integer>> rawDataCopy = new HashMap<>(friendsGraph);
        for (Map.Entry<Integer, List<Integer>> entry: rawDataCopy.entrySet()) {
            entry.getValue().retainAll(originalFriends);
        }

        List<Map.Entry<Integer, List<Integer>>> list = new LinkedList<>(rawDataCopy.entrySet());
        Collections.sort(list, (o1, o2) -> {
            int firstSize  = o1.getValue().size();
            int secondSize = o2.getValue().size();
            return firstSize > secondSize
                    ? -1 : (firstSize == secondSize ? 0 : 1);
        });

        Map<Integer, List<Integer>> processedData = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : list) {
            processedData.put(entry.getKey(), entry.getValue());
        }

        return processedData;
    }

    private static Map<Integer, List<Integer>> friendsGraph(int userId) {
        List<Integer> friends = getFriends(userId);

        Map<Integer, List<Integer>> friendsGraph = new HashMap<>();
        for (Integer id : friends) {
            List<Integer> friendsOfFriend = getFriends(id);
            if (friendsOfFriend.size() > 1000) {
                continue;
            }

            friendsGraph.put(id, friendsOfFriend);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { /* IGNORE */ }
        }

        return friendsGraph;
    }

    private static List<Integer> getFriends(int userId) {
        try {

            GetResponse response = VK.friends().get().userId(userId).execute();
            return response == null || response.getItems() == null ? Collections.EMPTY_LIST : response.getItems();

        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }

}

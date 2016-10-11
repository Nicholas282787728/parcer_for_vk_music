import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * Created by diez on 5/1/16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        boolean parseGroupsFlag = false;
        boolean mergeListsFlag = true;
        boolean parsePlaylistPartFlag = true;
        boolean analyseFlag = true;
//        Desktop.getDesktop().browse(new URI(
//                "https://oauth.vk.com/authorize?" +
//                        "client_id=5106013&" +
//                        "display=page&" +
//                        "redirect_uri=http://oauth.vk.com/blank.html" +
//                        "&scope=audio" +
//                        "&response_type=code&v=5.52"));

        String code = "7fa263c0b00c68b698";
//
//        Desktop.getDesktop().browse(new URI(
//                "https://oauth.vk.com/access_token?" +
//                        "client_id=5106013&" +
//                        "client_secret=ORX9eVddpsD75Hm2YseK&" +
//                        "redirect_uri=http://oauth.vk.com/blank.html&" +
//                        "code=7fa263c0b00c68b698"));


//        System.out.println("Hello, World " + System.currentTimeMillis());

//        String token = "10c86e74649ccb9ab3ca053f764cdf6da554b2cd5ccf1d93d740b60e2ffeb87a9800019089bb5ca5a126bd49a302f";
        String token = args[0];
        VkApi api = new VkApi(token);

        HashSet<VkUser> userSet = new HashSet<>();
        if (parseGroupsFlag) {
            try (BufferedReader br = new BufferedReader(new FileReader("grouplist.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    HashSet<VkUser> userSubSet = api.getAndFilterGroupMembers(line);
                    userSet.addAll(userSubSet);

                    JSONArray arr = new JSONArray();

                    for (VkUser user : userSubSet) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", user.id);
                        obj.put("first_name", user.firstName);
                        obj.put("last_name", user.lastName);
                        obj.put("city", user.city);
                        arr.put(obj);
                    }

                    Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("outuserlist_" + line + ".txt", false), "UTF-8"));
                    try {
                        out.write(arr.toString());
                    } finally {
                        out.close();
                    }

                }
            }
        }

        if (mergeListsFlag) {
            File dir = new File(".");
            File [] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt") && name.startsWith("outuserlist_");
                }
            });

            for (File jsonFile : files) {
                byte[] encoded = Files.readAllBytes(Paths.get(String.valueOf(jsonFile)));
                JSONArray items = new JSONArray(new String(encoded, "UTF-8"));
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    VkUser user = new VkUser();
                    user.id = item.getLong("id");
                    user.firstName = item.getString("first_name");
                    user.lastName = item.getString("last_name");
                    if (item.has("city"))
                        user.city = item.getString("city");
                    if (!userSet.contains(user))
                        userSet.add(user);
                }

            }
        }
        int processedCounter = 1;
        int skippedCounter = 1;
        if (parsePlaylistPartFlag) {
            int partId = Integer.parseInt(args[1]);
            for (VkUser user : userSet) {
               // if (user.id % 10 != partId)
                 //   continue;
                File f = new File("playlists/" + user.id + ".txt");
                if(f.exists())
                {
                    System.out.println("Skipping user " + user.id + ", skipped: " + skippedCounter++);
                    continue;
                }
                System.out.println("Processing user " + user.id + ", # in run: " + processedCounter++);
                HashSet<Song> songs = api.getUserSongs(user.id);
                Writer out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("playlists/" + user.id + ".txt", false), "UTF-8"));
                try {
                    for (Song song : songs) {
                        out.write(song.artist + "@@@@" + song.title + "\n");
                    }
                } finally {
                    out.close();
                }
            }
        }

        if (analyseFlag) {
            Analyser analyser = new Analyser("targetArtists.txt");
            int userCounter = 0;
            for (VkUser user : userSet) {
                userCounter++;
                if (userCounter % 20 == 0)
                    System.out.println(100 * userCounter / userSet.size() + "% done...");
                File f = new File("playlists/" + user.id + ".txt");
                if(!f.exists())
                    continue;
                int macthCounter = 0;
                try (BufferedReader br = new BufferedReader(new FileReader("playlists/" + user.id + ".txt"))) {
                    String line;
                    int playlistSize = 0;
                    while ((line = br.readLine()) != null) {
                        playlistSize++;
                        String[] splited = line.split("@@@@", 2);
                        if (splited.length != 2) continue;
                        Song song = new Song(splited[0], splited[1]);
                        if (analyser.isClose(song)) {
                            macthCounter++;
                            System.out.println(song.artist + " - " + song.title);
                        }
                    }
                    if (macthCounter == 0)
                        continue;
                    Writer out = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("retval.txt", true), "UTF-8"));
                    try {
                        out.write(
                                "https://vk.com/id" + user.id + "\t" +
                                        user.firstName + " " + user.lastName + "\t" +
                                        macthCounter + "\t" +
                                        playlistSize + "\n"
                        );
                    } finally {
                        out.close();
                    }
                }
            }
        }

        System.out.print(userSet.size());

    }
}

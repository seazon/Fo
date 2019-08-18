package com.seazon.fo;

import com.seazon.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoritesConfig {

    private static String read() {
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(new File(Core.CONFIG_FAVORITES));
            br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(s + "\n");
            }
            String all = sb.toString();
            return all;
        } catch (FileNotFoundException e) {
            LogUtils.error(e);
        } catch (IOException e) {
            LogUtils.error(e);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    LogUtils.error(e);
                }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtils.error(e);
                }
        }
        return null;
    }

    public static List<Favorite> getFavorites() {
        try {
            String data = read();
            if (data == null) {
                return null;
            }

            List<Favorite> list = new ArrayList<Favorite>();
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); ++i) {
                String ff = (String) array.get(i);
                JSONObject aa = new JSONObject(ff);
                Favorite cc = new Favorite();
                cc.name = aa.getString("Name");
                cc.path = aa.getString("Path");
                list.add(cc);
            }
            return list;
        } catch (JSONException e) {
            LogUtils.error(e);
        }
        return null;
    }

    public static void delFavorite(String path) {
        boolean contain = false;

        JSONArray array = new JSONArray();

        List<Favorite> list = getFavorites();
        if (list == null) {
            return;
        }

        for (Favorite f : list) {
            if (f.path.equals(path)) {
                contain = true;
            } else {
                array.put(f);
            }
        }

        if (contain) {
            write(array.toString());
        }
    }

    public static void saveFavorite(Favorite f) {
        try {

            JSONArray array = null;
            String all = read();
            if (Helper.isBlank(all)) {
                array = new JSONArray();
                array.put(f);
            } else {
                array = new JSONArray(all);

                List<Favorite> list = getFavorites();
                if (list == null) {
                    array = new JSONArray();
                    array.put(f);
                } else {
                    boolean contains = false;
                    for (Favorite f2 : list) {
                        if (f.path.equals(f2.path)) {
                            contains = true;
                            break;
                        }
                    }

                    if (!contains) {
                        array.put(f);
                    }
                }
            }

            write(array.toString());

        } catch (JSONException e) {
            LogUtils.error(e);
        }
    }

    private static void write(String data) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;

        try {
            File file = new File(Core.CONFIG_FAVORITES);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(data);

        } catch (FileNotFoundException e) {
            LogUtils.error(e);
        } catch (IOException e) {
            LogUtils.error(e);
        } finally {
            if (bw != null)
                try {
                    bw.close();
                } catch (IOException e) {
                    LogUtils.error(e);
                }
            if (fos != null)
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtils.error(e);
                }
        }
    }
}

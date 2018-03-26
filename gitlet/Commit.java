package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by whw on 2017/7/17.
 */
public class Commit implements Serializable {
    private String ID;
    private String msg;
    private String date;
    private Commit prev;
    private String branch;
    private HashMap<String, String> stagedFiles;

    public Commit(String msg, Commit prev, String branch, HashSet<String> stagedFiles) {
        this.msg = msg;
        this.date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        this.prev = prev;
        this.branch = branch;
        this.stagedFiles = new HashMap<>();
        if (prev != null) {
            ID = Utils.sha1(stagedFiles.toString(), prev.toString(), msg, date);
        } else {
            ID = Utils.sha1(stagedFiles.toString(), msg, date);
        }
        File contentPath = new File(".gitlet/content/" + ID + "/");
        contentPath.mkdirs();
        for (String name : stagedFiles) {
            File source = new File(name);
            File target = new File(".gitlet/content/" + ID + "/" + name);
            try {
                target.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Files.copy(source.toPath(), target.toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.stagedFiles.put(name, ".gitlet/content/" + ID + "/" + name);
        }
    }

    public String getID() {
        return this.ID;
    }

    public String getDate() {
        return this.date;
    }

    public String getMsg() {
        return this.msg;
    }

    public Commit getPrev() {
        return this.prev;
    }

    public String getBranch() {
        return this.branch;
    }

    public HashMap<String, String> getStagedFiles() {
        return this.stagedFiles;
    }

    public String getFilePath(String name) {
        if (stagedFiles.containsKey(name)) {
            return stagedFiles.get(name);
        }
        return null;
    }
}

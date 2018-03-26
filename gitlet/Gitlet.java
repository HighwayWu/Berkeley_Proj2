package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by whw on 2017/7/17.
 */
public class Gitlet implements Serializable {
    private HashMap<String, Commit> commitMap;
    private HashMap<String, Commit> branchMap;
    private HashSet<String> trackFiles;
    //private HashSet<String> untrackFiles;
    private HashSet<String> stagedFiles;
    private HashSet<String> rmFiles;
    private String currentBranch;
    private Commit head;

    public Gitlet() {
        commitMap = new HashMap<>();
        branchMap = new HashMap<>();
        trackFiles = new HashSet<>();
        stagedFiles = new HashSet<>();
        rmFiles = new HashSet<>();
    }

    public void init() {
        File path = new File(".gitlet/");
        if (path.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            path.mkdir();
            File content = new File(".gitlet/content/");
            content.mkdirs();
            File tmp = new File(".gitlet/tmp/");
            tmp.mkdirs();
            File ser = new File(".gitlet/foo.ser");
            try {
                ser.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            currentBranch = "master";
            commit("initial commit");
            branchMap.put(currentBranch, head);
        }
    }

    public void add(String name) {
        File path = new File(name);
        if (path.exists()) {
            if (rmFiles.contains(name)) {
                rmFiles.remove(name);
            }
            if (head.getStagedFiles().keySet().contains(name)) {
                File fb = new File(head.getFilePath(name));
                if (!isEqualFile(path, fb)) {
                    stagedFiles.add(name);
                }
            } else {
                stagedFiles.add(name);
            }
            trackFiles.add(name);
        } else {
            System.out.println("File does not exist.");
        }
    }

    public void commit(String msg) {
        if (head == null) {
            head = new Commit(msg, null, "master", new HashSet<>());
            commitMap.put(head.getID(), head);
        } else if (stagedFiles.isEmpty() && rmFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            Commit nextCommit = new Commit(msg, head, currentBranch, stagedFiles);
            commitMap.put(nextCommit.getID(), nextCommit);
            stagedFiles.clear();
            rmFiles.clear();
            head = nextCommit;
            branchMap.remove(currentBranch);
            branchMap.put(currentBranch, head);
        }
    }

    public void rm(String name) {
        if (!stagedFiles.contains(name) && (head.getStagedFiles().keySet().contains(name)
                || (head.getPrev() != null
                && head.getPrev().getStagedFiles().keySet().contains(name)))) {
            Utils.restrictedDelete(name);
            rmFiles.add(name);
        } else if (!stagedFiles.contains(name) && !head.getStagedFiles().keySet().contains(name)) {
            System.out.println("No reason to remove the file.");
        } else if (stagedFiles.contains(name) && !head.getStagedFiles().keySet().contains(name)) {
            stagedFiles.remove(name);
            trackFiles.remove(name);
        }
        stagedFiles.remove(name);
    }

    public void log() {
        Commit c = head;
        while (c != null) {
            System.out.print("===\nCommit " + c.getID()
                    + "\n" + c.getDate() + "\n" + c.getMsg() + "\n\n");
            c = c.getPrev();
        }
    }

    public void globalLog() {
        for (String name : commitMap.keySet()) {
            Commit c = commitMap.get(name);
            System.out.print("===\nCommit " + c.getID() + "\n"
                    + c.getDate() + "\n" + c.getMsg() + "\n\n");
        }
    }

    public void find(String msg) {
        HashSet<String> ans = new HashSet<>();
        for (String name : commitMap.keySet()) {
            if (commitMap.get(name).getMsg().equals(msg)) {
                ans.add(commitMap.get(name).getID());
            }
        }
        if (ans.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String m : ans) {
                System.out.println(m);
            }
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        Set<String> set = new TreeSet<String>();
        for (String name : branchMap.keySet()) {
            if (!name.equals(currentBranch)) {
                set.add(name);
            }
        }
        for (String name : set) {
            System.out.println(name);
        }
        set.clear();
        System.out.println("\n=== Staged Files ===");
        for (String name : stagedFiles) {
            set.add(name);
        }
        for (String name : set) {
            System.out.println(name);
        }
        set.clear();
        System.out.println("\n=== Removed Files ===");
        for (String name : rmFiles) {
            set.add(name);
        }
        for (String name : set) {
            System.out.println(name);
        }
        set.clear();
        System.out.println("\n=== Modifications Not Staged For Commit ==="
                + "\n\n=== Untracked Files ===\n");
    }

    public void checkout1(String fileName) {
        if (head.getFilePath(fileName) != null) {
            File source = new File(head.getFilePath(fileName));
            File target = new File(fileName);
            Utils.restrictedDelete(target);
            try {
                Files.copy(source.toPath(), target.toPath(),
                        StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                trackFiles.add(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void checkout2(String cID, String name) {
        Commit c = null;
        if (commitMap.containsKey(cID)) {
            c = commitMap.get(cID);
        }
        for (String id : commitMap.keySet()) {
            if (cID.substring(0, 5).equals(id.substring(0, 5))) {
                c = commitMap.get(id);
            }
        }
        if (c != null) {
            if (c.getFilePath(name) != null) {
                File source = new File(c.getFilePath(name));
                File target = new File(name);
                Utils.restrictedDelete(target);
                try {
                    Files.copy(source.toPath(), target.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    public void checkout3(String branchName) {
        if (branchMap.containsKey(branchName)) {
            if (!currentBranch.equals(branchName)) {
                Commit c = branchMap.get(branchName);
                for (String name : c.getStagedFiles().keySet()) {
                    File f = new File(name);
                    if (!trackFiles.contains(name) && f.exists()) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it or add it first.");
                        return;
                    }
                }
                for (String name : trackFiles) {
                    Utils.restrictedDelete(name);
                }
                trackFiles.clear();
                for (String name : c.getStagedFiles().keySet()) {
                    File source = new File(c.getFilePath(name));
                    File target = new File(name);
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
                    trackFiles.add(name);
                }
                head = c;
                currentBranch = branchName;
                stagedFiles.clear();
                rmFiles.clear();
            } else {
                System.out.println("No need to checkout the current branch.");
            }
        } else {
            System.out.println("No such branch exists.");
        }
    }

    public void branch(String name) {
        if (!branchMap.containsKey(name)) {
            Commit c = head;
            branchMap.put(name, c);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    public void rmBranch(String name) {
        if (branchMap.containsKey(name)) {
            if (!head.getBranch().equals(name)) {
                branchMap.remove(name);
            } else {
                System.out.println("Cannot remove the current branch.");
            }
        } else {
            System.out.println("A branch with that name does not exist.");
        }
    }

    public void reset(String cID) {
        Commit c = null;
        if (commitMap.containsKey(cID)) {
            c = commitMap.get(cID);
        }
        for (String id : commitMap.keySet()) {
            if (cID.substring(0, 5).equals(id.substring(0, 5))) {
                c = commitMap.get(id);
            }
        }
        if (c != null) {
            for (String name : c.getStagedFiles().keySet()) {
                File f = new File(name);
                if (!trackFiles.contains(name) && f.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it or add it first.");
                    return;
                }
            }
            for (String name : head.getStagedFiles().keySet()) {
                if (!c.getStagedFiles().containsKey(name)) {
                    Utils.restrictedDelete(name);
                }
            }
            head = c;
            for (String name : branchMap.keySet()) {
                if (head.getID().equals(branchMap.get(name).getID())) {
                    currentBranch = name;
                    break;
                }
            }
            branchMap.remove(currentBranch);
            branchMap.put(currentBranch, head);
            for (String name : head.getStagedFiles().keySet()) {
                checkout1(name);
            }
            stagedFiles.clear();
            rmFiles.clear();
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    public void merge(String name) {
        if (!stagedFiles.isEmpty() || !rmFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
        } else if (!branchMap.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
        } else if (currentBranch.equals(name)) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            Commit c1 = splitPoint(branchMap.get(name), head);
            Commit c2 = head;
            Commit givenCommit = branchMap.get(name);
            Commit splitCommit = c1;
            while (c2 != null) {
                if (givenCommit == c2) {
                    System.out.println("Given branch is an ancestor of the current branch.");
                    return;
                }
                c2 = c2.getPrev();
            }
            if (splitCommit == head) {
                head = givenCommit;
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            HashMap<String, String> givenFiles = givenCommit.getStagedFiles();
            HashMap<String, String> currFiles = head.getStagedFiles();
            HashMap<String, String> splitFiles = splitCommit.getStagedFiles();
            HashSet<String> files = new HashSet<>();
            files.addAll(givenFiles.keySet());
            files.addAll(currFiles.keySet());
            files.addAll(splitFiles.keySet());
            boolean mergeConflict = false;
            for (String pf : files) { // pf means pengding file
                if (!givenFiles.containsKey(pf) && currFiles.containsKey(pf)
                        && !splitFiles.containsKey(pf)) {
                    int hello;
                } else if (givenFiles.containsKey(pf) && !currFiles.containsKey(pf)
                        && !splitFiles.containsKey(pf)) {
                    isUntrackedFile(givenFiles.get(pf), pf);
                    copyFile(givenFiles.get(pf), pf);
                    stagedFiles.add(pf);
                } else if (givenFiles.containsKey(pf) && currFiles.containsKey(pf)) {
                    if ((splitFiles.containsKey(pf)
                            && !isEqualFile(givenFiles.get(pf), currFiles.get(pf))
                            && !isEqualFile(givenFiles.get(pf), splitFiles.get(pf))
                            && !isEqualFile(splitFiles.get(pf), currFiles.get(pf)))
                            || (!splitFiles.containsKey(pf)
                            && !isEqualFile(givenFiles.get(pf), currFiles.get(pf)))) {
                        mergeConflict = true;
                        File source = mergeConflict(currFiles.get(pf), givenFiles.get(pf), pf);
                        copyFile(source, new File(pf));
                    }
                } else if (givenFiles.containsKey(pf) || currFiles.containsKey(pf)) {
                    mergeConflict = true;
                    if (!givenFiles.containsKey(pf)) {
                        File f = new File("tmp");
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        File source = mergeConflict(currFiles.get(pf), "tmp", pf);
                        copyFile(source, new File(pf));
                    }
                } else if (givenFiles.containsKey(pf) && currFiles.containsKey(pf)
                        && splitFiles.containsKey(pf)) {
                    if (!isEqualFile(givenFiles.get(pf), splitFiles.get(pf))
                            && isEqualFile(currFiles.get(pf), splitFiles.get(pf))) {
                        isUntrackedFile(givenFiles.get(pf), pf);
                        copyFile(givenFiles.get(pf), pf);
                        stagedFiles.add(pf);
                    }
                } else if (!givenFiles.containsKey(pf) && currFiles.containsKey(pf)
                        && splitFiles.containsKey(pf)) {
                    if (isEqualFile(currFiles.get(pf), splitFiles.get(pf))) {
                        Utils.restrictedDelete(pf);
                        trackFiles.remove(pf);
                    }
                }
            }
            if (!mergeConflict) {
                Commit nextCommit = new Commit("Merged " + currentBranch
                        + " with " + name + ".", head, currentBranch, stagedFiles);
                commitMap.put(nextCommit.getID(), nextCommit);
                stagedFiles.clear();
                rmFiles.clear();
                head = nextCommit;
                branchMap.remove(currentBranch);
                branchMap.put(currentBranch, head);
            } else {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    private Commit splitPoint(Commit c1, Commit c2) {
        ArrayList<Commit> ac1 = new ArrayList<Commit>();
        ArrayList<Commit> ac2 = new ArrayList<Commit>();
        while (c1 != null) {
            ac1.add(c1);
            c1 = c1.getPrev();
        }
        while (c2 != null) {
            ac2.add(c2);
            c2 = c2.getPrev();
        }
        int i = 0;
        while (i < ac1.size() && !ac2.contains(ac1.get(i))) {
            i++;
        }
        if (i == ac1.size()) {
            c1 = null;
        } else {
            c1 = ac1.get(i);
        }
        return c1;
    }

    private File mergeConflict(String a, String b, String name) {
        File ans = new File(name);
        File f1 = new File(a);
        File f2 = new File(b);
        byte[] c1 = Utils.readContents(f1);
        byte[] c2 = Utils.readContents(f2);
        byte[] s1 = "<<<<<<< HEAD\n".getBytes();
        byte[] s2 = "=======\n".getBytes();
        byte[] s3 = ">>>>>>>\n".getBytes();
        byte[] s = new byte[c1.length + c2.length + s1.length + s2.length + s3.length];
        int len = 0;
        System.arraycopy(s1, 0, s, len, s1.length);
        len += s1.length;
        System.arraycopy(c1, 0, s, len, c1.length);
        len += c1.length;
        System.arraycopy(s2, 0, s, len, s2.length);
        len += s2.length;
        System.arraycopy(c2, 0, s, len, c2.length);
        len += c2.length;
        System.arraycopy(s3, 0, s, len, s3.length);
        Utils.writeContents(ans, s);
        return ans;
    }

    private void isUntrackedFile(String a, String b) {
        File f = new File(b);
        if (!trackFiles.contains(a) && f.exists()) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it or add it first.");
            return;
        }
    }

    private boolean isEqualFile(File a, File b) {
        return Utils.sha1(Utils.readContents(a)).equals(Utils.sha1(Utils.readContents(b)));
    }

    private boolean isEqualFile(String a, String b) {
        File fa = new File(a);
        File fb = new File(b);
        return Utils.sha1(Utils.readContents(fa)).equals(Utils.sha1(Utils.readContents(fb)));
    }

    private void copyFile(String source, String target) {
        File s = new File(source);
        File t = new File(target);
        try {
            Files.copy(s.toPath(), t.toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(File source, File target) {
        try {
            Files.copy(source.toPath(), target.toPath(),
                    StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

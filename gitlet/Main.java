package gitlet;

import java.io.*;

import static java.lang.System.exit;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author
 */

/*
    init:       java gitlet.Main init
    add:        java gitlet.Main add [file name]
    commit:     java gitlet.Main commit [message]
    rm:         java gitlet.Main rm [file name]
    log:        java gitlet.Main log
    find:       java gitlet.Main find [commit message]
    status:     java gitlet.Main status
    checkout:   java gitlet.Main checkout -- [file name]
                java gitlet.Main checkout [commit id] -- [file name]
                java gitlet.Main checkout [branch name]
    branch:     java gitlet.Main branch [branch name]
    rm-branch:  java gitlet.Main rm-branch [branch name]
    reset:      java gitlet.Main reset [commit id]
    merge:      java gitlet.Main merge [branch name]
 */

public class Main {
    private static String command;
    private static String op1 = "", op2 = "", op3 = "";

    public static void check(String... args) {
        for (int i = 0; i < args.length; i++) {
            if (!args[i].equals("")) {
                System.out.println("Incorrect operands.");
                exit(0);
            }
        }
    }

    private static Gitlet load() {
        Gitlet gitlet = null;
        File inFile = new File(".gitlet/foo.ser");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            gitlet = (Gitlet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException e) {
            gitlet = null;
            System.out.println("Not in an initialized gitlet directory.");
            exit(0);
        }
        return gitlet;
    }

    private static void save(Gitlet gitlet) {
        if (gitlet == null) {
            return;
        }
        File outFile = new File(".gitlet/foo.ser");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(gitlet);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sw(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            exit(0);
        }
        command = args[0];
        if (args.length >= 2) {
            op1 = args[1];
        }
        if (args.length >= 3) {
            op2 = args[2];
        }
        if (args.length >= 4) {
            op3 = args[3];
        }
    }

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        // FILL THIS IN
        sw(args);
        Gitlet gitlet = null;
        if (!command.equals("init")) {
            gitlet = load();
        }
        if (gitlet == null) {
            gitlet = new Gitlet();
        }
        switch (command) {
            case "init":
                check(op1, op2, op3);
                gitlet.init();
                break;
            case "add":
                check(op2, op3);
                gitlet.add(op1);
                break;
            case "commit":
                check(op2, op3);
                if (op1.equals("")) {
                    System.out.println("Please enter a commit message.");
                    return;
                }
                gitlet.commit(op1);
                break;
            case "rm":
                check(op2, op3);
                gitlet.rm(op1);
                break;
            case "log":
                check(op1, op2, op3);
                gitlet.log();
                break;
            case "global-log":
                check(op1, op2, op3);
                gitlet.globalLog();
                break;
            case "find":
                check(op2, op3);
                gitlet.find(op1);
                break;
            case "status":
                gitlet.status();
                check(op1, op2, op3);
                break;
            case "checkout":
                if (args.length == 2) {
                    gitlet.checkout3(op1);
                } else if (args.length == 3) {
                    if (op1.equals("--")) {
                        gitlet.checkout1(op2);
                    } else {
                        System.out.println("Incorrect operands.");
                        exit(0);
                    }
                } else if (args.length == 4) {
                    if (op2.equals("--")) {
                        gitlet.checkout2(op1, op3);
                    } else {
                        System.out.println("Incorrect operands.");
                        exit(0);
                    }
                } else {
                    System.out.println("Incorrect operands.");
                    exit(0);
                }
                break;
            case "branch":
                check(op2, op3);
                gitlet.branch(op1);
                break;
            case "rm-branch":
                check(op2, op3);
                gitlet.rmBranch(op1);
                break;
            case "reset":
                check(op2, op3);
                gitlet.reset(op1);
                break;
            case "merge":
                check(op2, op3);
                gitlet.merge(op1);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
        save(gitlet);
    }

}

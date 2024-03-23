import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class CodeGenerater {
    public static void main(String[] args) {
        // 讀取文件
        if (args.length == 0) {
            System.err.println("請輸入檔案名稱");
            return;
        }
        String fileName = args[0];
        System.out.println("File name: " + fileName);

        mermaid_code mermaid = new mermaid_code(fileName);

        // 寫入文件
        try {
            String output = "Example.java";
            String content = "this is going to be written into file";
            File file = new File(output);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(content);
            }
            System.out.println("Java class has been generated: " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class mermaid_code {
    String codeContent = new String();
    ArrayList<class_> codeUse = new ArrayList<class_>();

    public mermaid_code(String fileName) {
        try {
            codeContent = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            System.err.println("無法讀取文件 " + fileName);
            e.printStackTrace();
            return;
        }
        codeSpilt();
    }

    private void codeSpilt() {
        String[] codeSource = codeContent.split("\n");

        for (int i = 1; i < codeSource.length; i++) {
            int l, r;
            // 是function
            if (codeSource[i].indexOf('(') != -1) {
                // System.out.println("is function");
            }
            // 是attribute
            else if (codeSource[i].indexOf('+') != -1 || codeSource[i].indexOf('-') != -1) {
                // System.out.println("is attribute");
            } else if (codeSource[i].indexOf('}') != -1) {
                // System.out.println("is }");
            }

            // 是class
            else if (codeSource[i].indexOf("class ") != -1) {

                for (l = codeSource[i].indexOf("class ") + 6; l < codeSource[i].length()
                        && codeSource[i].charAt(l) == ' '; l++) {
                }

                for (r = l + 1; r < codeSource[i].length() &&
                        codeSource[i].charAt(r) != ' ' &&
                        codeSource[i].charAt(r) != '\n' &&
                        codeSource[i].charAt(r) != '{' &&
                        codeSource[i].charAt(r) != '\0'; r++) {
                }
                class_ new_class = new class_(codeSource[i].substring(l, r));
                codeUse.add(new_class);

                System.out.println(new_class.name);

            } else {
                // System.out.println("is {");
            }

        }
    }

}

class class_ {
    String name = "";
    ArrayList<line> lines = new ArrayList<line>();

    public class_(String name) {
        this.name = name;
    }

    public void add_line(String modifier, String member, String name, String set, String get) {
        line new_line = new line(modifier, member, name, set, get);
        lines.add(new_line);
    }
}

class line {
    String modifier = "";// private or public
    String member = "";// function or attribute
    String name = "";
    String set = "";
    String get = "";

    public line(String modifier, String member, String name, String set, String get) {
        this.modifier = modifier;
        this.member = member;
        this.name = name;
        this.set = set;
        this.get = get;
    }

}
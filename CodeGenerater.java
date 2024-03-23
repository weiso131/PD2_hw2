import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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

        String doc = mermaid.classArray.get(0).writeJava();
        System.out.println(doc);
        doc = mermaid.classArray.get(1).writeJava();
        System.out.println(doc);
        doc = mermaid.classArray.get(2).writeJava();
        System.out.println(doc);
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
    ArrayList<class_> classArray = new ArrayList<class_>();

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

    private String findNameLeft(int start, String codesource) {
        int l, r;

        for (r = start; r > 0 &&
                codesource.charAt(r - 1) == ' '; r--) {
        }
        for (l = r - 1; l > 0 &&
                codesource.charAt(l - 1) != ' ' &&
                codesource.charAt(l - 1) != '\t'; l--) {
        }
        return codesource.substring(l, r);

    }

    private String findNameRight(int start, String codesource, char stop) {
        int l, r;
        for (l = start; l < codesource.length()
                && codesource.charAt(l) == ' '; l++) {
        }

        for (r = l + 1; r < codesource.length() - 1 &&
                codesource.charAt(r - 1) != ')' &&
                codesource.charAt(r - 1) != stop &&
                codesource.charAt(r) != '\n'; r++) {
        }
        return codesource.substring(l, r);
    }

    private void addLine(String className, line newLine) {

        for (int i = 0; i < classArray.size(); i++) {

            if (classArray.get(i).name.equals(className)) {
                classArray.get(i).lines.add(newLine);
                return;
            }
        }
        System.out.println("class not found");

    }

    private void codeSpilt() {
        String[] codeSource = codeContent.split("\n");

        for (int i = 1; i < codeSource.length; i++) {

            String class_name = "";
            String type = "";
            String modifier = "";
            // 是function
            if (codeSource[i].indexOf('(') != -1) {
                String functionName = "";

                if (codeSource[i].indexOf('+') != -1)
                    modifier = "+";
                else
                    modifier = "-";
                if (codeSource[i].indexOf(':') != -1) {
                    class_name = findNameLeft(codeSource[i].indexOf(':') - 1, codeSource[i]);
                    functionName = findNameRight(codeSource[i].indexOf(modifier) + 1, codeSource[i], '\n');
                    type = findNameLeft(codeSource[i].length() - 1, codeSource[i]);

                }
                line newLine = new line(modifier, "function", functionName, type);
                addLine(class_name, newLine);

            }
            // 是attribute
            else if (codeSource[i].indexOf('+') != -1 || codeSource[i].indexOf('-') != -1) {
                String attributeName = "";

                if (codeSource[i].indexOf('+') != -1)
                    modifier = "+";
                else
                    modifier = "-";
                if (codeSource[i].indexOf(':') != -1) {
                    class_name = findNameLeft(codeSource[i].indexOf(':') - 1, codeSource[i]);
                    type = findNameRight(codeSource[i].indexOf(modifier) + 1, codeSource[i], ' ');
                    attributeName = findNameLeft(codeSource[i].length() - 1, codeSource[i]);
                }
                line newLine = new line(modifier, "attribute", attributeName, type);
                addLine(class_name, newLine);
            }

            // is }
            else if (codeSource[i].indexOf('}') != -1) {
                // System.out.println("is }");
            }

            // 是class
            else if (codeSource[i].indexOf("class ") != -1) {

                class_name = findNameRight(codeSource[i].indexOf("class ") + 6, codeSource[i], ' ');
                class_ new_class = new class_(class_name);
                classArray.add(new_class);

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

    public String writeJava() {
        String doc = "class ";
        doc = doc + name + " {\n";
        for (int i = 0; i < lines.size(); i++)
            doc += lines.get(i).write_line();
        doc = doc + "}";
        return doc;
    }

}

class line {
    String modifier = "";// private or public
    String member = "";// function or attribute
    String name = "";
    String type = "";
    String set = "";
    String get = "";

    public line(String modifier, String member, String name, String type) {
        this.modifier = modifier;
        this.member = member;
        this.name = name;
        this.type = type;

        if (member.equals("function") && name.indexOf("set") != -1) {

        }
        if (member.equals("function") && name.indexOf("get") != -1) {

        }

    }

    public String write_line() {
        String doc = "\t";
        if (modifier == "+")
            doc += "public ";
        else
            doc += "private ";
        doc += type + " " + name + ";\n";
        return doc;
    }

}
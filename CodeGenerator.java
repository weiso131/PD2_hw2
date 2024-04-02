import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CodeGenerator {
    public static void main(String[] args) {
        // 讀取文件
        if (args.length == 0) {
            System.err.println("請輸入檔案名稱");
            return;
        }
        String fileName = args[0];
        System.out.println("File name: " + fileName + ".java");

        mermaid_code mermaid = new mermaid_code(fileName);
        String doc = "";
        for (int i = 0; i < mermaid.classArray.size(); i++) {
            doc = mermaid.classArray.get(i).writeJava();

            Utility.write_doc(mermaid.classArray.get(i).name, doc);
        }

        // 寫入文件

    }
}

class Utility {
    public static String findNameRight(char not, char limit, String codesource, int start, int take) {

        // take為1代表取limit的字元
        // take為0代表不取limit字元
        // take為1是為了躲避每個行尾一個怪怪的字元
        int l = findRightNot(not, codesource, start);
        int r = findRightLimit(limit, codesource, l);

        return codesource.substring(l, r + take);
    }

    public static int findRightNot(char not, String codesource, int start) {
        // 回傳第一個不是limit的位置
        int end = start;
        for (; end < codesource.length() && codesource.charAt(end) == not; end++) {
        }
        return end;
    }

    public static int findRightLimit(char limit, String codesource, int start) {
        // 回傳第一個找到的limit位置
        int end = start;
        for (; end < codesource.length() && codesource.charAt(end) != limit; end++) {
        }
        return end;
    }

    public static void write_doc(String class_name, String content) {
        try {

            File file = new File(class_name + ".java");
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(content);
            }
            System.out.println("Java class has been generated: " + class_name);
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

    private void addLine(String className, line newLine) {

        for (int i = 0; i < classArray.size(); i++) {

            if (classArray.get(i).name.equals(className)) {
                classArray.get(i).lines.add(newLine);
                return;
            }
        }
        System.out.println("class not found");

    }

    private int findClassArray(String className) {
        int i = 0;
        for (; i < classArray.size() && classArray.get(i).name.equals(className) != true; i++) {
        }

        return i;
    }

    private void codeSpilt() {
        String[] codeSource = codeContent.split("\n");
        String class_name = "";
        String type = "";
        String modifier = "";
        String last_class = "";
        String define_class = "";
        for (int i = 0; i < codeSource.length; i++) {
            if (codeSource[i].indexOf('+') != -1)
                modifier = "+";
            else
                modifier = "-";
            codeSource[i] = codeSource[i].replace('\t', ' ');
            codeSource[i] = codeSource[i].replace('\r', ' ');

            // 是function
            if (codeSource[i].indexOf('(') != -1) {
                codeSource[i] = codeSource[i].replaceAll("\\s+", " ");
                codeSource[i] = codeSource[i].replace(" (", "(");
                String functionName = "";

                if (codeSource[i].indexOf(':') != -1)
                    class_name = Utility.findNameRight(' ', ' ', codeSource[i], 0, 0);
                else
                    class_name = define_class;
                functionName = Utility.findNameRight(' ', ')',
                        codeSource[i], codeSource[i].indexOf(modifier) + 1, 1);
                type = Utility.findNameRight(' ', ' ', codeSource[i], codeSource[i].indexOf(')') + 1, 0);

                line newLine = new line(modifier, "function", functionName, type);
                addLine(class_name, newLine);

            }
            // 是attribute
            else if (codeSource[i].indexOf('+') != -1 || codeSource[i].indexOf('-') != -1) {
                String attributeName = "";

                if (codeSource[i].indexOf(':') != -1)
                    class_name = Utility.findNameRight(' ', ' ', codeSource[i], 0, 0);
                else
                    class_name = define_class;
                type = Utility.findNameRight(' ', ' ', codeSource[i], codeSource[i].indexOf(modifier) + 1, 0);
                attributeName = Utility.findNameRight(' ', ' ', codeSource[i],
                        codeSource[i].indexOf(type + " ") + type.length(), 0);
                line newLine = new line(modifier, "attribute", attributeName, type);
                addLine(class_name, newLine);
            }

            // is }
            else if (codeSource[i].indexOf('}') != -1) {
                define_class = "";
            }

            // 是class
            else if (codeSource[i].indexOf("class ") != -1) {
                if (codeSource[i].indexOf("{") != -1)
                    class_name = Utility.findNameRight(' ', ' ', codeSource[i],
                            codeSource[i].indexOf("class ") + 6, 0);
                else
                    class_name = Utility.findNameRight(' ', ' ', codeSource[i],
                            codeSource[i].indexOf("class ") + 6, 0);

                class_ new_class = new class_(class_name);
                last_class = class_name;
                if (findClassArray(class_name) == classArray.size())
                    classArray.add(new_class);
            }
            if (codeSource[i].indexOf("{") != -1 && codeSource[i].indexOf("}") == -1) {
                define_class = last_class;
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

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).member.equals("function"))
                lines.get(i).initFunction(lines);
        }

        String doc = "public class ";
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
    String setget = "";
    ArrayList<reference> references = new ArrayList<reference>();
    HashMap<String, String> typeReturn = new HashMap<String, String>();

    public line(String modifier, String member, String name, String type) {
        this.modifier = modifier;
        this.member = member;
        this.name = name;
        this.type = type;
        if (type == "")
            this.type = "void";
        typeReturn.put("void", " {;}\n");

        typeReturn.put("int", " {return 0;}\n");
        typeReturn.put("String", " {return \"\";}\n");
        typeReturn.put("boolean", " {return false;}\n");

    }

    public void initFunction(ArrayList<line> lines) {
        // 處理function的reference
        references = findValue(name);
        String fname = Utility.findNameRight(' ', '(', name, 0, 1);
        for (int i = 0; i < references.size(); i++) {
            fname += references.get(i).write_line();
            if (i != references.size() - 1)
                fname += ", ";
        }
        this.name = fname + ")";
        // System.out.println(this.name);
        char[] attr_c = Utility.findNameRight(' ', '(', name, 3, 0).toCharArray();

        if (attr_c.length != 0 && Character.isUpperCase(attr_c[0])) {
            attr_c[0] = Character.toLowerCase(attr_c[0]);
            String attr = new String(attr_c);
            if (name.substring(0, 3).equals("set"))
                setget = "this." + attr + " = " + references.get(0).name + ";";
            else if (name.substring(0, 3).equals("get")) {
                for (int i = 0; i < lines.size(); i++) {

                    if (lines.get(i).member == "attribute" && lines.get(i).name.equals(attr)) {

                        setget = "return " + attr + ";";
                        return;
                    }
                }
            }

        }

    }

    public String write_line() {
        String doc = "    ";
        if (modifier == "+")
            doc += "public ";
        else
            doc += "private ";

        if (member == "function") {
            if (setget != "") {
                doc += type + " " + name + " {\n";
                doc += "        " + setget + "\n";
                doc += "    }\n";
            } else
                doc += type + " " + name + typeReturn.get(type);
        } else
            doc += type + " " + name + ";\n";
        return doc;
    }

    private ArrayList<reference> findValue(String text) {

        String regex = "[a-zA-Z]+(\\[\\])?\s[a-zA-Z_$][a-zA-Z_$0-9]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        ArrayList<reference> references = new ArrayList<reference>();
        while (matcher.find()) {
            String valueType = Utility.findNameRight(' ', ' ', matcher.group(),
                    0, 0);
            String valueName = Utility.findNameRight(' ', ' ', matcher.group(),
                    Utility.findRightLimit(' ', matcher.group(), 0), 0);
            references.add(new reference(valueName, valueType));
        }
        return references;
    }

}

class reference extends line {
    public reference(String name, String type) {
        super("", "", name, type);
    }

    @Override
    public String write_line() {

        return type + " " + name;
    }
}
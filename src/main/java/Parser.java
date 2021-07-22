import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class Parser {

    private final static String linkTheme = "https://math-ege.sdamgia.ru/test?theme=";
    private final static String linkTask = "https://ege.sdamgia.ru/problem?id=";
    private final static String fileName = "src\\data\\file.html";
    public static HashMap<String, HashMap> examInfo = new HashMap<>();

    public static void getExamInfo() throws IOException {
        Document document = Jsoup.parse(new File(fileName), "UTF-8");
        Elements elementsPart = document.select(".Constructor-PartList .ConstructorForm-Row");
        HashMap<String, HashMap> topics = new HashMap<>();
        String namePart = "";
        for (Element elemPart : elementsPart) {
            if (elemPart.select(".ConstructorForm-Row_label").first() != null) {
                if (!topics.isEmpty()) {
                    examInfo.put(namePart, topics);
                    topics = new HashMap<>();
                }
                namePart = (examInfo.size() + 1) + ". " + elemPart.select(".ConstructorForm-Row_label .ConstructorForm-Topic").text();
            } else {
                String nameTopic = (topics.size() + 1) + ". " + elemPart.select(".ConstructorForm-Topic .Link .ConstructorForm-TopicDesc .Link-U").text();
                Elements elementsTopic = elemPart.select(".ConstructorForm-Topic .ConstructorForm-TopicSubs .Link_wrap");
                HashMap<String, String> topic = new HashMap<>();
                for (Element elemTopic : elementsTopic) {
                    String nameTheme = (topic.size() + 1) + ". " + elemTopic.select(".ConstructorForm-TopicDesc").text().split(" · ")[0];
                    String numTheme = elemTopic.select(".ConstructorForm-TopicDesc a").attr("href").split("=")[1];
                    topic.put(nameTheme, numTheme);
                }
                topics.put(nameTopic, topic);
            }
        }
        examInfo.put(namePart, topics);
    }

    public static int getTask(String theme) throws IOException {
        ArrayList<Integer> tasks = new ArrayList<>();
        Document document = Jsoup.connect(linkTheme + theme + "&print=true").get();
        Elements elements = document.select("#ans_key .prob_answer");
        elements.remove(0);
        for (Element element : elements) {
            Elements elementsTable = element.select("td");
            int taskNum = Integer.parseInt(elementsTable.get(1).text());
            tasks.add(taskNum);
        }
        return tasks.get(new Random().nextInt(tasks.size()));
    }

    public static ArrayList<String> getParts(String text) {
        String regularExpression = "src=\".*?\"|>[^<>]+?<";
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> parts = new ArrayList<>();
        String partsText = "";
        while (matcher.find()) {
            String part = matcher.group();
            if (part.contains("src=")) {
                if (!partsText.equals("")) {
                    parts.add(partsText);
                    partsText = "";
                }
                part = part.split("src=")[1].replaceAll("\"", "");
                parts.add(part);
            } else {
                part = part.replaceAll("[><]|&nbsp;|\n", "");
                if (part.contains("Решение")) {
                    part = part.replaceAll(" {2}", "");
                }
                if (!part.replaceAll(" ", "").equals("")) {
                    partsText += part;
                }
            }
        }
        if (!partsText.equals("")) {
            parts.add(partsText);
        }
        return parts;
    }

    public static ArrayList<String> taskCondition(User user) throws IOException {
        Document document = Jsoup.connect(linkTask + user.getTask()).get();
        Element taskBlock = document.select(".prob_maindiv .pbody").first();
        return getParts(taskBlock.toString());
    }

    public static ArrayList<String> taskSolution(User user) throws IOException {
        Document document = Jsoup.connect(linkTask + user.getTask()).get();
        Element solutionBlock = document.select(".prob_maindiv #sol" + user.getTask()).first();
        ArrayList<String> partsSolution = getParts(solutionBlock.toString());
        ArrayList<String> taskSolution = new ArrayList<>();
        boolean answer = user.isSolution();
        for (String partSolution : partsSolution) {
            if (partSolution.contains("Ответ")) {
                answer = true;
            }
            if (answer) {
                taskSolution.add(partSolution);
            }
        }
        return taskSolution;
    }
}

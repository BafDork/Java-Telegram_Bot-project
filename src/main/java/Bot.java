import org.apache.batik.transcoder.TranscoderException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class Bot extends TelegramLongPollingBot {

    private final HashMap<Long, User> users = new HashMap<>();
    private final static String linkMaim = "https://ege.sdamgia.ru";
    public final static String imagePath = "src\\data\\png_image.png";
    private final static String info = """
            Этот бот найдет для тебя задачку по математике)
               /start - начать работу с ботом;
               /param - изменить параметры поиска задачи;
               /new - найти новую задачку по выбранным параметрам;
               /task - найти задачу по номеру;
               /sol - показывать ли решение задач;
               /num - показывать ли номер задания.""";

    public static void startBot() {
        ApiContextInitializer.init();
        TelegramBotsApi botApi = new TelegramBotsApi();
        try {
            botApi.registerBot(new Bot());
        } catch (TelegramApiRequestException er) {
            er.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "TestProjectAlice_bot";
    }

    @Override
    public String getBotToken() {
        return "1685323176:AAEDecJ9AEibairpwdL2EYi9Z_QZV3ZLJUs";
    }

    private void sendMessage(Message mes, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(mes.getChatId());
        message.setText(text);
        message.enableMarkdown(true);
        try {
            execute(message);
        } catch (TelegramApiException er) {
            er.printStackTrace();
        }
    }

    private void sendPhoto(Message mes, File img) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(mes.getChatId());
        photo.setPhoto(img);
        try {
            execute(photo);
        } catch (TelegramApiException er) {
            er.printStackTrace();
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        System.out.println(text);
        long id = message.getChatId();
        if (text.equals("/start")) {
            sendMessage(message, getParts());
            users.put(id, new User());
            return;
        }
        if (!users.containsKey(id) && !text.equals("/info")) {
            sendMessage(message, "Информация о работе бота: /info");
            return;
        }
        User user = users.get(id);
        if (command_dispatcher(message, user)) return;
        condition_dispatcher(message, user);
    }

    private void condition_dispatcher(Message message, User user) {
        if (user.getCommand() == null) return;
        String text = message.getText();
        String answerError = "Вы ввели что-то не то";
        switch (user.getCommand()) {
            case "part" -> {
                String userPart = null;
                for (String part : Parser.examInfo.keySet()) {
                    String[] name = part.split(". ");
                    if (text.equals(name[0]) || text.equals(name[1])) {
                        userPart = part;
                    }
                }
                if (userPart != null) {
                    user.setCommand("topic");
                    user.setPart(userPart);
                    sendMessage(message, getTopics(user));
                } else {
                    sendMessage(message, answerError);
                }
            }
            case "topic" -> {
                String userTopic = null;
                for (Object topic : Parser.examInfo.get(user.getPart()).keySet()) {
                    String[] name = topic.toString().split(". ");
                    if (text.equals(name[0]) || text.equals(name[1])) {
                        userTopic = topic.toString();
                    }
                }
                if (userTopic != null) {
                    user.setCommand("theme");
                    user.setTopic(userTopic);
                    sendMessage(message, getThemes(user));
                } else {
                    sendMessage(message, answerError);
                }
            }
            case "theme" -> {
                String userTheme = null;
                HashMap<String, String> themes = (HashMap<String, String>) Parser.examInfo.get(user.getPart()).get(user.getTopic());
                for (String theme : themes.keySet()) {
                    String[] name = theme.split(". ");
                    if (text.equals(name[0]) || text.equals(name[1])) {
                        userTheme = themes.get(theme);
                    }
                }
                if (userTheme != null) {
                    user.setCommand("taskCondition");
                    user.setTheme(userTheme);
                    getTask(user);
                    sendTaskPart(message, user);
                } else {
                    sendMessage(message, answerError);
                }
            }
            case "taskCondition" -> {
                user.setCommand("taskSolution");
                sendTaskPart(message, user);
            }
            case "userTask" -> {
                try {
                    user.setCommand("taskCondition");
                    int task = Integer.parseInt(text);
                    user.setTask(task);
                    sendTaskPart(message, user);
                } catch (Exception e) {
                    sendMessage(message, answerError);
                }
            }
        }
    }

    private boolean command_dispatcher(Message message, User user) {
        String text = message.getText();
        if (text.equals("/info")) {
            sendMessage(message, info);
            return true;
        }
        if (text.equals("/new")) {
            user.setCommand("taskCondition");
            getTask(user);
            sendTaskPart(message, user);
            return true;
        }
        if (text.equals("/param")) {
            user.newParameters();
            sendMessage(message, getParts());
            return true;
        }
        if (text.equals("/task")) {
            user.setCommand("userTask");
            sendMessage(message, "Введите номер задания");
            return true;
        }
        if (text.equals("/sol")) {
            user.setSolution(!user.isSolution());
            if (user.isSolution()) {
                sendMessage(message, "Теперь задачи с решениями");
            } else {
                sendMessage(message, "Теперь задачи без решениями");
            }
            return true;
        }
        if (text.equals("/num")) {
            user.setTaskNumber(!user.isTaskNumber());
            if (user.isTaskNumber()) {
                sendMessage(message, "Номер задания виден");
            } else {
                sendMessage(message, "Номер задания скрыт");
            }
            return true;
        }
        return false;
    }

    private String getTaskSelection(Object[] selectionsArr) {
        String message = "";
        HashMap<Integer, String> selections = new HashMap<>();
        for (Object selection : selectionsArr) {
            String[] selectionArr = selection.toString().split("\\. ");
            selections.put(Integer.parseInt(selectionArr[0]), selectionArr[1]);
        }
        for (int i = 1; i <= selections.size(); i++) {
            message += "\n    " + i + ". " + selections.get(i);
        }
        return message;
    }

    private String getParts() {
        String message = "Выберите из какой части вы хотите получить задачу:";
        Object[] parts = Parser.examInfo.keySet().toArray();
        Arrays.sort(parts);
        for (Object part : parts) {
            message += "\n    " + part;
        }
        return message;
    }

    private String getTopics(User user) {
        String message = "Выберите раздел:";
        Object[] topicsArr = Parser.examInfo.get(user.getPart()).keySet().toArray();
        message += getTaskSelection(topicsArr);
        return message;
    }

    private String getThemes(User user) {
        String message = "Выберите тему:";
        HashMap<String, String> themesMap = (HashMap<String, String>) Parser.examInfo.get(user.getPart()).get(user.getTopic());
        Object[] themesArr = themesMap.keySet().toArray();
        message += getTaskSelection(themesArr);
        return message;
    }

    private void getTask(User user) {
        String part;
        String topic;
        String theme;
        if (user.getPart() == null) {
            Object[] partsArr = Parser.examInfo.keySet().toArray();
            int partInd = new Random().nextInt(partsArr.length);
            part = partsArr[partInd].toString();
        } else {
            part = user.getPart();
        }
        if (user.getTopic() == null) {
            Object[] topicsArr = Parser.examInfo.get(part).keySet().toArray();
            int topicInd = new Random().nextInt(topicsArr.length);
            topic = topicsArr[topicInd].toString();
        } else {
            topic = user.getTopic();
        }
        if (user.getTheme() == null) {
            HashMap<String, String> themesMap = (HashMap<String, String>) Parser.examInfo.get(part).get(topic);
            Object[] themesArr = themesMap.keySet().toArray();
            int themeInd = new Random().nextInt(themesArr.length);
            theme = themesMap.get(themesArr[themeInd].toString());
        } else {
            theme = user.getTheme();
        }
        try {
            int task = Parser.getTask(theme);
            System.out.println(task);
            user.setTask(task);
        } catch (IOException er) {
            er.printStackTrace();
        }
    }

    private void sendTaskPart(Message message, User user) {
        ArrayList<String> taskParts = new ArrayList<>();
        try {
            switch (user.getCommand()) {
                case "taskCondition" -> taskParts = Parser.taskCondition(user);
                case "taskSolution" -> taskParts = Parser.taskSolution(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (user.isTaskNumber() && user.getCommand().equals("taskCondition")) {
            sendMessage(message, "Номер: " + user.getTask());
        }
        for (String taskPart : taskParts) {
            String urlImage = null;
            if (taskPart.contains(".svg")) {
                urlImage = taskPart;
            }
            if (taskPart.contains("get_file")) {
                urlImage = linkMaim + taskPart;
            }
            try {
                if (urlImage != null) {
                    SvgConverter.svgToPng(urlImage);
                    File file = new File(imagePath);
                    sendPhoto(message, file);
                } else {
                    sendMessage(message, taskPart);
                }
            } catch (TranscoderException e) {
                try {
                    BufferedImage image;
                    URL url = new URL(urlImage);
                    image = ImageIO.read(url);
                    File file = new File(imagePath);
                    ImageIO.write(image, "png", file);
                    sendPhoto(message, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
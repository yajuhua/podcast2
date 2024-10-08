package io.github.yajuhua.podcast2.bot.telegram;

import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.controller.SubController;
import io.github.yajuhua.podcast2.pojo.dto.AppendItemDTO;
import io.github.yajuhua.podcast2.pojo.dto.GetExtendListDTO;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.vo.ExtendListVO;
import io.github.yajuhua.podcast2.pojo.vo.SubVO;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TG机器人
 */

public class TGBot extends TelegramLongPollingBot {

    private String botToken;
    private String botUsername;
    private SubController subController;
    private static SubVO selectedSubVO;//选择的订阅
    private static Set<AppendItemDTO> appendItemDTOList = new HashSet<>();//存放追加节目
    public static List<Items> downloadCompletedItems = new ArrayList<>();//已经下载完成的
    public static String chatId;//聊天id

    public TGBot(DefaultBotOptions options, String botToken, String botUsername, SubController subController) {
        super(options);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.subController = subController;
        //开启自动推送消息
        autoPushDownloadCompleted();
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            //搜集最新chatId
            chatId = update.getMessage().getChatId().toString();
        }
        showSub(update,false);
        getSelectSub(update);
        showCurrentSub(update);
        appendItemUrl(update);
        appendItem(update);
    }

    /**
     * 展示订阅列表
     * @param update
     * @return
     */
    private void showSub(Update update,boolean show){
        if (update.hasMessage()
                && update.getMessage().hasText()
                && update.getMessage().getText().startsWith("选择订阅") || show){
            //发送订阅列表
            List<SubVO> subVOList = subController.list().getData();
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            for (SubVO vo : subVOList) {
                rowList.add(Arrays.asList(InlineKeyboardButton
                        .builder().text(vo.getTitle())
                        .callbackData(vo.getUuid()).build()));
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboard(rowList).build();
            SendMessage message = SendMessage.builder()
                    .text("选择追加节目的订阅!")
                    .chatId(update.getMessage().getChatId().toString())
                    .replyMarkup(inlineKeyboardMarkup)
                    .build();
            executeMessage(message);
        }
    }

    /**
     * 获取选择订阅
     * @param update
     */
    private void getSelectSub(Update update){
        if (update.hasCallbackQuery()
                && update.getCallbackQuery().getMessage().hasText()
                && update.getCallbackQuery().getMessage().getText().equals("选择追加节目的订阅!")){
            List<SubVO> subVOS = subController.list().getData();
            // 处理回调查询
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();

            List<SubVO> collect = subVOS.stream().filter(new Predicate<SubVO>() {
                @Override
                public boolean test(SubVO subVO) {
                    return callbackData.equals(subVO.getUuid());
                }
            }).collect(Collectors.toList());
            for (SubVO vo : collect) {
                selectedSubVO = vo;
            }
            if (selectedSubVO != null) {

                // 发送回调查询的答复
                SendMessage responseMessage = new SendMessage();
                responseMessage.setChatId(callbackQuery.getMessage().getChatId());
                responseMessage.setText("你订阅选择了: " + selectedSubVO.getTitle());

                executeMessage(responseMessage);
            }
        }
    }

    /**
     * 显示当前订阅
     * @param update
     */
    private void showCurrentSub(Update update){
        if (update.hasMessage() && update.getMessage().hasText()){
            String text = update.getMessage().getText();
            if (text.startsWith("当前订阅")){
                SendMessage responseMessage = new SendMessage();
                if (selectedSubVO != null){
                    responseMessage.setChatId(update.getMessage().getChatId());
                    responseMessage.setText("当前订阅为: " + selectedSubVO.getTitle());
                }else {
                    responseMessage.setChatId(update.getMessage().getChatId());
                    responseMessage.setText("当前未选择订阅");
                }
                executeMessage(responseMessage);
            }
        }
    }

    /**
     * 将扩展选项转换成内联键盘
     * @param extendList
     * @return
     */
    private InlineKeyboardMarkup extendListToInlineKeyboardMarkup(ExtendList extendList){
        //仅支持select选项
        List<Select> selectList = extendList.getSelectList();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // 第一行，类型选择
        List<InlineKeyboardButton> type = new ArrayList<>();
        type.add(InlineKeyboardButton.builder().text("视频(类型)").callbackData("视频(类型)").build());
        type.add(InlineKeyboardButton.builder().text("音频(类型)").callbackData("音频(类型)").build());
        keyboard.add(type);

        //扩展选项
        for (Select select : selectList) {
            String name = select.getName();
            List<String> options = select.getOptions();
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String option : options) {
                row.add(InlineKeyboardButton.builder()
                        .text(option + "(" + name + ")")
                        .callbackData(option + "(" + name + ")").build());
            }
            keyboard.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    /**
     * 创建配置信息
     * @param url 视频链接
     * @return
     */
    private SendMessage createConfigMsg(String url) throws Exception {
        //获取扩展选项
        String secondLevelDomain = Http.getSecondLevelDomain(Http.getHost(url));
        GetExtendListDTO getExtendListDTO = new GetExtendListDTO();
        getExtendListDTO.setType("Video");
        getExtendListDTO.setPlugin(secondLevelDomain);
        getExtendListDTO.setUrl(url);
        Result<ExtendListVO> extendListVOResult = subController.extendList(getExtendListDTO);
        ExtendList extendList = extendListVOResult.getData().getExtendList();
        //扩展选项转换成内联键盘
        InlineKeyboardMarkup inlineKeyboardMarkup = extendListToInlineKeyboardMarkup(extendList);
        //最后一行完成
        List<InlineKeyboardButton> finish = new ArrayList<>();
        finish.add(InlineKeyboardButton.builder().text("完成提交").callbackData("finish").build());
        inlineKeyboardMarkup.getKeyboard().add(finish);
        //文本提示
        SendMessage message = new SendMessage();
        message.setText("配置下载选项\n" + url);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    /**
     * 追加节目链接到列表
     * @param update
     */
    private void appendItemUrl(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText() || !update.getMessage().getText().startsWith("追加节目")) {
            return;
        }

        String chatId = update.getMessage().getChatId().toString();
        if (selectedSubVO == null) {
            sendMessage(chatId, "请先选择订阅");
            return;
        }

        String text = update.getMessage().getText();
        String url = text.trim().replace("追加节目", "");
        try {
            new URL(url);  // 验证 URL 是否有效
            appendItemDTOList.add(AppendItemDTO.builder().url(url).build());

            // 发送配置信息
            SendMessage configMsg = createConfigMsg(url);
            configMsg.setChatId(chatId);
            executeMessage(configMsg);
        } catch (MalformedURLException e) {
            sendMessage(chatId, "该链接不合法: " + url);
        } catch (Exception ex) {
            sendMessage(chatId, "配置失败: " + ex.getMessage());
        }
    }

    /**
     * 追加节目
     * @param update
     */
    private void appendItem(Update update){
        if (update.hasCallbackQuery()
                && update.getCallbackQuery().getMessage().hasText()
                && update.getCallbackQuery().getMessage().getText().startsWith("配置下载选项\n")){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            //获取链接
            String url = callbackQuery.getMessage()
                    .getText().trim().replace("配置下载选项\n", "");
            if (callbackQuery.getData().equalsIgnoreCase("finish")){

                List<AppendItemDTO> collect = appendItemDTOList.stream().filter(new Predicate<AppendItemDTO>() {
                    @Override
                    public boolean test(AppendItemDTO appendItemDTO) {
                        return appendItemDTO.getUrl().equals(url);
                    }
                }).collect(Collectors.toList());
                SendMessage message = new SendMessage();
                message.setChatId(update.getCallbackQuery().getMessage().getChatId());
                if (collect.isEmpty()){
                    message.setText("提交失败: 找不到该配置");
                }else {
                    //使用默认配置
                    AppendItemDTO appendItemDTO = collect.get(0);
                    if (appendItemDTO.getType() == null){
                        appendItemDTO.setType("Audio");
                    }
                    if (appendItemDTO.getInputAndSelectDataList() == null){
                        appendItemDTO.setInputAndSelectDataList(new ArrayList<>());
                    }
                    if (selectedSubVO == null){
                        message.setText("请先选择订阅");
                    }else {
                        appendItemDTO.setChannelUuid(selectedSubVO.getUuid());
                        Result result = subController.appendItem(appendItemDTO);
                        if (result.getCode() == 1){
                            //说明已经配置完成了
                            message.setText("已追加至 " + selectedSubVO.getTitle() + "，准备下载\n" + url);
//                            appendItemDTOList.remove(appendItemDTO);//移除掉
                        }else {
                            message.setText("追加节目失败: " + result.getMsg());
                        }
                    }
                }
                executeMessage(message);
            }else {
                //继续添加配置
                List<AppendItemDTO> collect = appendItemDTOList.stream().filter(new Predicate<AppendItemDTO>() {
                    @Override
                    public boolean test(AppendItemDTO appendItemDTO) {
                        return url.equals(appendItemDTO.getUrl());
                    }
                }).collect(Collectors.toList());
                if (!collect.isEmpty()){
                    AppendItemDTO appendItemDTO = collect.get(0);
                    String data = callbackQuery.getData();
                    if (data.equals("视频(类型)")){
                        appendItemDTO.setType("Video");
                    }else if (data.equals("音频(类型)")){
                        appendItemDTO.setType("Audio");
                    }else {
                        //扩展选项
                        int index = data.lastIndexOf("(");
                        //扩展选项名称
                        String name = data.substring(index + 1,data.length() - 1);
                        //扩展选项值
                        String content = data.substring(0,index);
                        if (appendItemDTO.getInputAndSelectDataList() == null){
                            appendItemDTO.setInputAndSelectDataList(new ArrayList<>());
                        }
                        //封装扩展选项
                        InputAndSelectData inputAndSelectData = new InputAndSelectData();
                        inputAndSelectData.setName(name);
                        inputAndSelectData.setContent(content);
                        appendItemDTO.getInputAndSelectDataList().add(inputAndSelectData);
                    }
                }else {
                    //如果没有该链接就新创建一个
                    AppendItemDTO appendItemDTO = new AppendItemDTO();
                    appendItemDTO.setUrl(url);
                    appendItemDTOList.add(appendItemDTO);
                }
            }
        }
    }

    /**
     * 推送下载完成的
     */
    public void pushDownloadCompleted(){
        if (!downloadCompletedItems.isEmpty() && chatId != null){
            Items items = downloadCompletedItems.get(0);
            String text = "下载完成 " + items.getLink();
            sendMessage(chatId,text);
            downloadCompletedItems.remove(items);
        }
    }

    /**
     * 每分钟推送一次下载消息
     */
    private void autoPushDownloadCompleted(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pushDownloadCompleted();
            }
        },0,1, TimeUnit.MINUTES);
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }
}

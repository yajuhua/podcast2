package io.github.yajuhua.podcast2.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.common.utils.ExtendListUtil;
import io.github.yajuhua.podcast2.common.utils.Http;
import io.github.yajuhua.podcast2.mapper.ExtendMapper;
import io.github.yajuhua.podcast2.mapper.ItemsMapper;
import io.github.yajuhua.podcast2.mapper.SubMapper;
import io.github.yajuhua.podcast2.plugin.PluginManager;
import io.github.yajuhua.podcast2.pojo.dto.GetExtendListDTO;
import io.github.yajuhua.podcast2.pojo.entity.Extend;
import io.github.yajuhua.podcast2.pojo.entity.Items;
import io.github.yajuhua.podcast2.pojo.entity.Sub;
import io.github.yajuhua.podcast2.pojo.vo.DownloadConfVO;
import io.github.yajuhua.podcast2.pojo.vo.EditSubVO;
import io.github.yajuhua.podcast2.pojo.vo.ExtendListVO;
import io.github.yajuhua.podcast2.service.DownloadService;
import io.github.yajuhua.podcast2.service.SubService;
import io.github.yajuhua.podcast2API.Params;
import io.github.yajuhua.podcast2API.Podcast2;
import io.github.yajuhua.podcast2API.extension.build.ExtendList;
import io.github.yajuhua.podcast2API.extension.build.Input;
import io.github.yajuhua.podcast2API.extension.build.Select;
import io.github.yajuhua.podcast2API.extension.reception.InputAndSelectData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {
    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private SubMapper subMapper;
    @Autowired
    private Gson gson;
    @Autowired
    private SubService subService;
    @Autowired
    private ExtendMapper extendMapper;
    @Autowired
    private PluginManager pluginManager;

    public Result<DownloadConfVO> getDownloadConf(String uuid) throws Exception {
        Items items = itemsMapper.selectByUuid(uuid);
        Sub sub = subMapper.selectByUuid(items.getChannelUuid());
        if (sub != null && sub.getSubType().equalsIgnoreCase("empty")){
            return Result.error("空订阅不支持更新下载配置！");
        }
        EditSubVO editSubVO = getEditSubInfo(items.getChannelUuid()).getData();

        DownloadConfVO downloadConfVO = new DownloadConfVO();
        //items表中的只有inputAndSelectDataList数据，无法直接区分Input和Select
        List<InputAndSelectData> itemInputDataListData = new ArrayList<>();
        List<InputAndSelectData> itemSelectDataListData = new ArrayList<>();
        Type listType = new TypeToken<List<InputAndSelectData>>(){}.getType();
        if (items.getInputAndSelectDataList() != null && !items.getInputAndSelectDataList().isEmpty()){
            List<InputAndSelectData> itemInputAndSelectDataList = gson.fromJson(items.getInputAndSelectDataList(),listType);
            for (Input input : editSubVO.getExtendList().getInputList()) {
                for (InputAndSelectData data : itemInputAndSelectDataList) {
                    if (input.getName().equalsIgnoreCase(data.getName())){
                        itemInputDataListData.add(data);
                    }
                }
            }

            for (Select select : editSubVO.getExtendList().getSelectList()) {
                for (InputAndSelectData data : itemInputAndSelectDataList) {
                    if (select.getName().equalsIgnoreCase(data.getName())){
                        itemSelectDataListData.add(data);
                    }
                }
            }
            downloadConfVO.setSelectListData(itemSelectDataListData.stream().filter(inputAndSelectData -> inputAndSelectData.getName() != null).collect(Collectors.toList()));
            downloadConfVO.setInputListData(itemInputDataListData.stream().filter(inputAndSelectData -> inputAndSelectData.getName() != null).collect(Collectors.toList()));
        }else {
            downloadConfVO.setSelectListData(editSubVO.getSelectListData().stream().filter(inputAndSelectData -> inputAndSelectData.getName() != null).collect(Collectors.toList()));
            downloadConfVO.setInputListData(editSubVO.getInputListData().stream().filter(inputAndSelectData -> inputAndSelectData.getName() != null).collect(Collectors.toList()));
        }

        downloadConfVO.setType(items.getType());
        downloadConfVO.setIsExtend(editSubVO.getIsExtend());
        downloadConfVO.setExtendList(editSubVO.getExtendList());

        return Result.success(downloadConfVO);
    }

    public Result<EditSubVO> getEditSubInfo(@PathVariable String uuid)throws Exception{
        //1.获取sub
        Sub sub = subService.selectByUuid(uuid);
        if (sub.getSubType().equalsIgnoreCase("plugin")){
            EditSubVO editSubVO = new EditSubVO();
            BeanUtils.copyProperties(sub,editSubVO);

            //2.将titleKeywords和descKeywords字符串转换成List集合
            List<String> titleKeywords = new ArrayList<>();
            if (sub.getTitleKeywords() != null){
                titleKeywords = Arrays.asList(sub.getTitleKeywords().split(","));
            }
            List<String> descKeywords = new ArrayList<>();
            if (sub.getDescKeywords() != null){
                titleKeywords = Arrays.asList(sub.getDescKeywords().split(","));
            }

            //3.获取插件的扩展选项
            GetExtendListDTO getExtendListDTO = new GetExtendListDTO();
            BeanUtils.copyProperties(sub,getExtendListDTO);
            getExtendListDTO.setUrl(sub.getLink());
            ExtendListVO extendListVO = extendList(getExtendListDTO).getData();
            editSubVO.setExtendList(extendListVO.getExtendList());
            editSubVO.setInputListData(extendListVO.getInputListData());
            editSubVO.setSelectListData(extendListVO.getSelectListData());

            //4.获取用户插件扩展选项数据
            List<Extend> anExtends = extendMapper.selectByUuid(uuid);
            List<InputAndSelectData> inputListData = new ArrayList<>();
            List<InputAndSelectData> selectListData = new ArrayList<>();

            //获取扩展选项时仅使用用户设置的,兼容旧版选项
            //input类型
            List<Input> filterInputDataList = editSubVO.getExtendList().getInputList().stream().filter(new Predicate<Input>() {
                @Override
                public boolean test(Input input) {
                    for (Extend extend : anExtends) {
                        if (input.getName().equals(extend.getName())) {
                            return true;
                        }
                    }
                    return false;
                }
            }).collect(Collectors.toList());
            editSubVO.getExtendList().setInputList(filterInputDataList);

            //select类型
            List<Select> filterSelectDataList = editSubVO.getExtendList().getSelectList().stream().filter(new Predicate<Select>() {
                @Override
                public boolean test(Select select) {
                    for (Extend extend : anExtends) {
                        if (select.getName().equals(extend.getName())) {
                            return true;
                        }
                    }
                    return false;
                }
            }).collect(Collectors.toList());
            editSubVO.getExtendList().setSelectList(filterSelectDataList);

            //4.将selectList和inputList封装成VO
            for (Select select : extendListVO.getExtendList().getSelectList()) {
                for (Extend extend : anExtends) {
                    if (extend.getName().equals(select.getName())){
                        selectListData.add(new InputAndSelectData(extend.getName(),extend.getContent()));
                        break;
                    }
                }
            }

            //避免传入null
            if (extendListVO.getExtendList().getInputList() == null){
                List<Input> inputList = new ArrayList<>();
                extendListVO.getExtendList().setInputList(inputList);
            }

            if (extendListVO.getExtendList().getSelectList() == null){
                List<Select> selectList = new ArrayList<>();
                extendListVO.getExtendList().setSelectList(selectList);
            }

            for (Input input : extendListVO.getExtendList().getInputList()) {
                for (Extend extend : anExtends) {
                    if (extend.getName().equals(input.getName())){
                        inputListData.add(new InputAndSelectData(extend.getName(),extend.getContent()));
                        break;
                    }
                }
            }

            //设置属性
            editSubVO.setTitleKeywords(titleKeywords);
            editSubVO.setDescKeywords(descKeywords);
            editSubVO.setSelectListData(selectListData);
            editSubVO.setInputListData(inputListData);
            editSubVO.setUuid(uuid);

            //返回VO
            return Result.success(editSubVO);
        }
        else if (sub.getSubType().equals("empty")) {
            EditSubVO editSubVO = new EditSubVO();
            BeanUtils.copyProperties(sub,editSubVO);
            return Result.success(editSubVO);
        }else {
            return Result.error("未找到订阅类型");
        }
    }

    /**
     * 用于将扩展数据写入数据库
     * @param inputAndSelectDataList
     * @param plugin
     * @param uuid
     * @param isExtend
     * @return
     */
    private List<Extend> extendList(List<InputAndSelectData> inputAndSelectDataList,String plugin,
                                    String uuid,Integer isExtend,String url,String type) throws Exception{
        List<Extend> extendList = new ArrayList<>();
        GetExtendListDTO getExtendListDTO = new GetExtendListDTO();
        getExtendListDTO.setUrl(url);
        getExtendListDTO.setPlugin(plugin);
        getExtendListDTO.setType(type);

        //如果数据长度为0，或扩展关闭
        if (inputAndSelectDataList.size() == 0 || isExtend == 0){
            ExtendList extendList1 = extendList(getExtendListDTO).getData().getExtendList();
            for (Input input : extendList1.getInputList()) {
                Extend extend = new Extend();
                extend.setName(input.getName());
                extend.setChannelUuid(uuid);
                extend.setPlugin(plugin);
                extendList.add(extend);
            }

            for (Select select : extendList1.getSelectList()) {
                Extend extend = new Extend();
                extend.setName(select.getName());
                extend.setPlugin(plugin);
                extend.setChannelUuid(uuid);
                extendList.add(extend);
            }
        }else {
            for (InputAndSelectData inputAndSelectData : inputAndSelectDataList) {
                Extend extend = new Extend();
                BeanUtils.copyProperties(inputAndSelectData,extend);
                extend.setPlugin(plugin);
                extend.setChannelUuid(uuid);
                extendList.add(extend);
            }
        }

        return extendList;

    }

    public Result<ExtendListVO> extendList(GetExtendListDTO getExtendListDTO) throws Exception {
        log.info("getExtendListDTO:{}",getExtendListDTO);
        Params params = new Params();
        params.setUrl(getExtendListDTO.getUrl());
        String secondLevelDomain = Http.getSecondLevelDomain(getExtendListDTO.getPlugin());

        //获取插件扩展信息
/*        Class aClass = PluginLoader.selectByName(Http.getSecondLevelDomain(getExtendListDTO.getPlugin()), dataPathProperties).get(0);
        Constructor constructor = aClass.getConstructor(String.class);
        Object o = constructor.newInstance(gson.toJson(params));
        String extendListStr = gson.toJson(aClass.getMethod(ReflectionMethodName.GET_EXTENSION).invoke(o));
        ExtendList extendList = gson.fromJson(extendListStr, ExtendList.class);
        PluginLoader.close(aClass);*/

        Podcast2 instance = pluginManager.getPluginInstanceByDomainName(secondLevelDomain, params);
        ExtendList extendList = instance.getExtensions();

        return Result.success(ExtendListUtil.buildExtendListVO(extendList));
    }
}

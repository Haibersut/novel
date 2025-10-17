package com.wtl.novel.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.UntilInfo;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.translator.Novelpia;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private Novelpia novelpia;
    @Autowired
    private DictionaryService dictionaryService;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Dictionary> getAllDictionaries() {
        return dictionaryRepository.findAll();
    }

    public Dictionary getDictionaryByKey(String keyField) {
        return dictionaryRepository.getDictionaryByKeyField(keyField);
    }

    public Dictionary addCookie(Dictionary dictionary) {
        return dictionaryRepository.save(dictionary);
    }

    public Dictionary saveDictionary(Dictionary dictionary) {
        return dictionaryRepository.save(dictionary);
    }

    public void deleteDictionary(Long id) {
        dictionaryRepository.deleteById(id);
    }

    public List<Dictionary> findByKeyFieldLikeAndIsDeletedFalse(String keyFieldPattern) {
        return dictionaryRepository.findByKeyFieldLikeAndIsDeletedFalse(keyFieldPattern);
    }


    public List<UntilInfo> findCookie() {
//        String oneCookie = getOneCookie();
        return dictionaryRepository.findByKeyFieldLikeAndIsDeletedFalse("novelPiaCookie%")
                .stream()
                .map(d -> {
                    try {
                        UntilInfo untilInfo = MAPPER.readValue(d.getDescription(), UntilInfo.class);
                        untilInfo.setCreatedAt(d.getCreatedAt());
                        return untilInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<UntilInfo> findCookieByUserId(Long userId) {
//        String oneCookie = getOneCookie();
        return dictionaryRepository.findByKeyFieldLikeAndIsDeletedFalse("novelPiaCookie%")
                .stream()
                .map(d -> {
                    try {
                        UntilInfo untilInfo = MAPPER.readValue(d.getDescription(), UntilInfo.class);
                        untilInfo.setCreatedAt(d.getCreatedAt());
                        untilInfo.setDicId(d.getId());
                        return untilInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(u -> Objects.equals(u.getUserId(), userId))
                .toList();
    }

    @Transactional
    public String getOneCookie() {
        List<Dictionary> dictionaryList = dictionaryRepository.findByKeyFieldLikeAndIsDeletedFalse("novelPiaCookie%");
        refreshCookie(dictionaryList);

        List<Dictionary> removeDic = new ArrayList<>();
        List<Dictionary> saveDic = new ArrayList<>();
        List<UntilInfo> valid = new ArrayList<>();
        List<UntilInfo> invalid = new ArrayList<>();

        List<UntilInfo> source = dictionaryList.stream()
                .map(d -> {
                    try {
                        UntilInfo untilInfo = MAPPER.readValue(d.getDescription(), UntilInfo.class);
                        untilInfo.setDicId(d.getId());
                        saveDic.add(d);
                        return untilInfo;
                    } catch (Exception e) {
                        removeDic.add(d);
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();


        for (UntilInfo u : source) {
            if (u.isStillValid()) {
                valid.add(u);
            } else {
                invalid.add(u);
            }
        }

        // 对 valid 组排序：先 all=true 再 all=false
        valid.sort((a, b) -> Boolean.compare(b.isAll(), a.isAll()));
        dictionaryRepository.saveAll(saveDic);
//        dictionaryRepository.deleteAll(removeDic);
//        List<Long> list = invalid.stream().map(UntilInfo::getDicId).toList();
//        dictionaryRepository.deleteAllByIdInBatch(list);
        if (!valid.isEmpty()) {
            Long dicId = valid.get(0).getDicId();
            return dictionaryRepository.getReferenceById(dicId).getValueField();
        } else {
            return null;
        }
    }

    private void refreshCookie(List<Dictionary> dictionaryList) {
        for (Dictionary dictionary : dictionaryList) {
            String valueField = dictionary.getValueField();
            try {
                UntilInfo untilInfo = MAPPER.readValue(dictionary.getDescription(), UntilInfo.class);
                UntilInfo untilInfo1 = novelpia.getUntilInfo(valueField, untilInfo.getUsername(), untilInfo.getUserId());
                dictionary.setDescription(MAPPER.writeValueAsString(untilInfo1));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public String addCookie(User userByToken, UntilInfo untilInfo) {
        UntilInfo untilInfo1 = novelpia.getUntilInfo(untilInfo.getDeadline(), userByToken.getEmail(), userByToken.getId());
        if (untilInfo1.getDeadline().isBlank()) {
            return "添加失败，当前Cookie不可用";
        }
        if (!untilInfo1.isStillValid()) {
            return "添加失败，当前Cookie会员已过期";
        }
        Dictionary dictionary = new Dictionary();
        dictionary.setKeyField("novelPiaCookie_" + userByToken.getId() + "_" + System.currentTimeMillis());
        dictionary.setCreatedAt(LocalDateTime.now());
        dictionary.setUpdatedAt(LocalDateTime.now());
        dictionary.setValueField(untilInfo.getDeadline());
        try {
            dictionary.setDescription(MAPPER.writeValueAsString(untilInfo1));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        dictionaryService.saveDictionary(dictionary);
        if (!untilInfo1.isAll()) {
            return "添加成功，但未实名或者未开启全年龄";
        }
        return "添加成功";
    }

    public boolean deleteCookie(Long dicId, Long userId) {
        Dictionary referenceById = dictionaryRepository.getReferenceById(dicId);
        if (referenceById.getKeyField().contains("novelPiaCookie_" + userId)) {
            dictionaryRepository.delete(referenceById);
            return true;
        }
        return false;
    }
}
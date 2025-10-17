package com.wtl.novel.Service;

import com.wtl.novel.CDO.FavoriteCTO;
import com.wtl.novel.CDO.ReadingRecordCTO;
import com.wtl.novel.entity.*;

import java.util.List;

import com.wtl.novel.repository.FavoriteGroupsRepository;
import com.wtl.novel.repository.FavoriteRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.ReadingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService{

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteGroupsRepository favoriteGroupsRepository;

    @Autowired
    private ReadingRecordRepository readingRecordRepository;

    public List<Favorite> getAllFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }


    public String getFavoriteGroupNameByObjectId(Long userId, Long objectId) {
        // Find the Favorite object using userId and objectId
        Optional<Favorite> favoriteOptional = favoriteRepository.findByUserIdAndObjectId(userId, objectId);

        // If the Favorite object exists, get its groupId
        if (favoriteOptional.isPresent()) {
            Long groupId = favoriteOptional.get().getGroupId();

            // Use the groupId to find the corresponding FavoriteGroups object
            if (groupId != null) {
                Optional<FavoriteGroups> favoriteGroupsOptional = favoriteGroupsRepository.findById(groupId);

                // If the FavoriteGroups object exists, return its name
                if (favoriteGroupsOptional.isPresent()) {
                    return favoriteGroupsOptional.get().getName();
                }
            }
        }

        // Return null if no favorite or group is found
        return null;
    }

    public boolean existsByUserIdAndObjectIdAndFavoriteType(Long userId, Long objectId, String favoriteType) {
        return favoriteRepository.existsByUserIdAndObjectIdAndFavoriteType(userId, objectId, favoriteType);
    }

    public List<ReadingRecordCTO> getHistory(User favoriteUser) {
        Long userId = favoriteUser.getId();
        List<ReadingRecord> readingRecordList = readingRecordRepository.findTop50ByUserIdOrderByUpdateTimeDesc(userId);
        List<Long> idList = readingRecordList.stream().map(ReadingRecord::getNovelId).toList();
        List<Novel> novelList = novelRepository.findAllByIdIn(idList);
        Map<Long, Novel> novelMap = novelList.stream()
                .collect(Collectors.toMap(Novel::getId, novel -> novel));

        return readingRecordList.stream()
                .map(record -> {
                    Novel novel = novelMap.get(record.getNovelId());
                    String title = (novel != null) ? novel.getTitle() : "未知小说";
                    return new ReadingRecordCTO(
                            title,                   // 来自Novel的title
                            record.getLastChapter(), // 来自ReadingRecord
                            record.getNovelId(),
                            record.getUpdateTime()     // 来自ReadingRecord的updateTime作为readDate
                    );
                })
                .toList();
    }

    public List<FavoriteCTO> getFavoritesByUserIdAndGroup(Long groupId,User favoriteUser) {
        List<Favorite> byUserIdAndFavoriteType = favoriteRepository.findByUserIdAndGroupId(favoriteUser.getId(), groupId);
        List<Long> objectIdList = byUserIdAndFavoriteType.stream().map(Favorite::getObjectId).toList();
        List<ReadingRecord> byUserIdAndNovelIdIn = readingRecordRepository.findByUserIdAndNovelIdIn(favoriteUser.getId(), objectIdList);
        Map<Long, ReadingRecord> readingRecordMap = byUserIdAndNovelIdIn.stream()
                .collect(Collectors.toMap(
                        ReadingRecord::getNovelId,
                        record -> record
                ));

        // 遍历 Favorite 列表，构造 FavoriteCTO
        return byUserIdAndFavoriteType.stream()
                .map(favorite -> {
                    // 获取对应的 ReadingRecord
                    ReadingRecord readingRecord = readingRecordMap.get(favorite.getObjectId());

                    // 如果没有对应的 ReadingRecord，则 lastChapter 为 null 或默认值
                    Long lastChapter = (readingRecord != null) ? readingRecord.getLastChapter() : null;

                    // 构造 FavoriteCTO
                    return new FavoriteCTO(
                            favorite,
                            lastChapter
                    );
                })
                .collect(Collectors.toList());
    }

    public List<FavoriteCTO> getFavoritesByUserIdAndType(String favoriteType, User favoriteUser) {
        List<Favorite> byUserIdAndFavoriteType = favoriteRepository.findByUserIdAndFavoriteType(favoriteUser.getId(), favoriteType);
        List<Long> objectIdList = byUserIdAndFavoriteType.stream().map(Favorite::getObjectId).toList();
        List<ReadingRecord> byUserIdAndNovelIdIn = readingRecordRepository.findByUserIdAndNovelIdIn(favoriteUser.getId(), objectIdList);
        Map<Long, ReadingRecord> readingRecordMap = byUserIdAndNovelIdIn.stream()
                .collect(Collectors.toMap(
                        ReadingRecord::getNovelId,
                        record -> record
                ));

        // 遍历 Favorite 列表，构造 FavoriteCTO
        return byUserIdAndFavoriteType.stream()
                .map(favorite -> {
                    // 获取对应的 ReadingRecord
                    ReadingRecord readingRecord = readingRecordMap.get(favorite.getObjectId());

                    // 如果没有对应的 ReadingRecord，则 lastChapter 为 null 或默认值
                    Long lastChapter = (readingRecord != null) ? readingRecord.getLastChapter() : null;

                    // 构造 FavoriteCTO
                    return new FavoriteCTO(
                            favorite,
                            lastChapter
                    );
                })
                .collect(Collectors.toList());
    }

}
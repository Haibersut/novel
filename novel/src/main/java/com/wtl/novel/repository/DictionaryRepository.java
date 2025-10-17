package com.wtl.novel.repository;


import com.wtl.novel.entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DictionaryRepository  extends JpaRepository<Dictionary, Long> {

    Dictionary getDictionaryByKeyField(String keyField);


    Dictionary findDictionaryByKeyFieldAndIsDeletedFalse(String keyField);
    // 添加模糊匹配的方法
    List<Dictionary> findByKeyFieldLikeAndIsDeletedFalse(String keyFieldPattern);
}
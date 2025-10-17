package com.wtl.novel.Service;

import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.RequestLog;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.RequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class RequestLogService {
    @Autowired
    private RequestLogRepository requestLogRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private List<String> limitUrl;

    // 编译一个正则表达式模式，用于匹配 /chapters/ 后跟一个数字 ID
    public static final Pattern CHAPTERS_ID_PATTERN = Pattern.compile("^/api/chapters/\\d+$");
    // 匹配 /api/chapters/getChapterByVersion/{id}/{userId}
    public static final Pattern CHAPTERS_ID_PATTERN1 =
            Pattern.compile("^/api/chapters/getChapterByVersion/\\d+/\\d+$");
    public static final Pattern CHAPTERS_ID_PATTERN3 =
            Pattern.compile("^/api/chapters/findAllContentVersion/\\d+/\\d+$");

    // 匹配 /api/chapters/getChapterByIdApi/{id}
    public static final Pattern CHAPTERS_ID_PATTERN2 =
            Pattern.compile("^/api/chapters/getChapterByIdApi/\\d+$");
    public static final Pattern CHAPTERS_ID_PATTERN_AND_GET_ID = Pattern.compile("^/api/chapters/(\\d+)$");
    private static final Pattern Favorite = Pattern.compile("^/api/novels/(\\d+)/(up|down)/([a-zA-Z]+)$");

    public boolean checkRequestLimit(Credential credential, String requestURI) {
        int limitRequest = Integer.parseInt(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("limitRequest").getValueField());
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        RequestLog requestLog = requestLogRepository.findByCredentialIdAndRequestTimeAfter(
                credential.getId(),
                todayStart
        );

        if (requestLog == null) {
            synchronized (this) {
                RequestLog reRequestLog = requestLogRepository.findByCredentialIdAndRequestTimeAfter(
                        credential.getId(),
                        todayStart
                );
                if (reRequestLog == null) {
                    requestLog = new RequestLog();
                    requestLog.setCredential(credential);
                    requestLog.setRequestTime(LocalDateTime.now());
                    requestLog.setCount(1);
                    System.out.println("添加凭证");
                    requestLogRepository.save(requestLog);
                }
            }
            return true;
        } else if ((CHAPTERS_ID_PATTERN.matcher(requestURI).matches() ||  CHAPTERS_ID_PATTERN1.matcher(requestURI).matches()  ||  CHAPTERS_ID_PATTERN3.matcher(requestURI).matches() || Favorite.matcher(requestURI).matches() || limitUrl.contains(requestURI)) && requestLog.getCount() >= limitRequest) {
            return false;
        }
        if (CHAPTERS_ID_PATTERN.matcher(requestURI).matches() || CHAPTERS_ID_PATTERN1.matcher(requestURI).matches() || CHAPTERS_ID_PATTERN3.matcher(requestURI).matches() || CHAPTERS_ID_PATTERN2.matcher(requestURI).matches() || Favorite.matcher(requestURI).matches()  || limitUrl.contains(requestURI)) {
            requestLog.setCount(requestLog.getCount() + 1);
            requestLogRepository.save(requestLog);
        }
        return true;
    }
}
package com.wtl.novel.Service;

import com.wtl.novel.CDO.ImageInfo;
import com.wtl.novel.CDO.UploadMetadata;
import com.wtl.novel.CDO.UploadNovelDTO;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UploadService {
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private TagService tagService;
    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private NovelTagService novelTagService;
    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChapterSyncService chapterSyncService;
    @Autowired
    private ChapterImageLinkRepository chapterImageLinkRepository;
    @Autowired
    private UserNovelRelationService userNovelRelationService;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CloudflareR2Util cloudflareR2Util;

    public Long newChapter(Long novelId, Integer type) {
        Optional<Integer> optional = chapterRepository.findMaxChapterNumberByNovelId(novelId);
        int chapterNumber = optional.map(integer -> integer + 1).orElse(1);
        if (type == 1) {
            Chapter chapter = new Chapter();
            chapter.setNovelId(novelId);
            chapter.setChapterNumber(chapterNumber);
            chapter.setTitle("第" + chapterNumber + "章");
            chapter.setTrueId(novelId + "_" + chapterNumber);
            chapter.setOwnPhoto(false);
            Chapter save = chapterRepository.save(chapter);
            return save.getId();
        } else {
//            ChapterExecute chapter = new ChapterExecute();
//            chapter.setNovelId(novelId);
//            chapter.setChapterNumber(chapterNumber);
//            chapter.setTitle("第" + chapterNumber + "章");
//            chapter.setTrueId(novelId + "_" + chapterNumber);
//            chapter.setOwnPhoto(false);
//            ChapterExecute save = chapterExecuteRepository.save(chapter);
            return null;
        }
    }


    @Transactional
    public void saveChapter(Long userId, Long id, String title, String content, List<ImageInfo> imgInfo) {
        try {
            String uploadImg = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImg").getValueField();
            String uploadImgTag = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImgTag").getValueField();
            Chapter chapter = chapterRepository.findByIdAndIsDeletedFalse(id);
            List<ChapterImageLink> chapterImageLinks = new ArrayList<>();
            chapterImageLinkRepository.deleteByChapterTrueId(chapter.getTrueId());
            String saveDir = cloudflareR2Util.getTempDirectory() + "chapter_" + chapter.getTrueId();
            AtomicInteger index = new AtomicInteger(1);
            chapter.setContent(content);
            chapter.setTitle(title);
            chapterRepository.save(chapter);
            chapterSyncService.save(chapter);
            if (imgInfo != null && !imgInfo.isEmpty()) {
                chapter.setOwnPhoto(true);
            }
            imgInfo.forEach(imageInfo -> {
                String imageUrl = imageInfo.getUrl();
                String position = imageInfo.getPosition();
                if (imageUrl.contains(uploadImg)) {
                    ChapterImageLink chapterImageLink = new ChapterImageLink(chapter.getTrueId(), uploadImgTag.replaceAll("uploadImg", imageUrl), position);
                    chapterImageLink.setCf(true);
                    chapterImageLinks.add(chapterImageLink);
                } else {
                    try {
                        String savedImagePath;
                        if (ImageHandler.isBase64Image(imageUrl)) {
                            savedImagePath = ImageHandler.handleBase64Image(imageUrl, saveDir);
                        } else {
                            savedImagePath = ImageHandler.handleUrlImage(imageUrl, saveDir);
                        }
                        String fileExtension = FileUtil.getFileExtension(savedImagePath);
                        String s = "chapter_" + chapter.getTrueId() + "_" + index.get() + "_" + UUID.randomUUID().toString() + "." + fileExtension;
                        cloudflareR2Util.uploadImageToCloudflareR2(savedImagePath, s);
                        ChapterImageLink chapterImageLink = new ChapterImageLink(chapter.getTrueId(), uploadImgTag.replaceAll("uploadImg", uploadImg + s), position);
                        chapterImageLink.setCf(true);
                        chapterImageLinks.add(chapterImageLink);
                        // 将图片信息保存到数据库
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                index.addAndGet(1);
            });
            chapterImageLinkRepository.saveAll(chapterImageLinks);
            FileUtil.deleteFolder(new File(saveDir));
        } finally {
            userService.updateUploadByUserId(userId, false);
        }
    }

    public UploadNovelDTO getNovelDetail(Long novelId,Long userId) {
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(novelId, "upload", userId);
        if (upload.isEmpty()) {
            return null;
        }
        List<Tag> tagsByNovelId = novelService.getTagsByNovelId(novelId);
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId);
        return new UploadNovelDTO(novel, tagsByNovelId);
    }

    @Transactional
    public UploadNovelDTO updateNovelDetail(MultipartFile cover,UploadMetadata metadata,Long novelId,Long userId) {
        try {
            List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(novelId, "upload", userId);
            if (upload.isEmpty()) {
                return null;
            }
            List<Tag> tagsByNovelId = novelService.getTagsByNovelId(novelId);
            Novel novel = novelRepository.findByIdAndIsDeletedFalse(novelId);
            if (!metadata.getOriginalTitle().trim().isEmpty()) {
                novel.setTrueName(metadata.getOriginalTitle().trim());
                novel.setTitle(metadata.getWorkTitle().trim().isEmpty() ? metadata.getOriginalTitle().trim() : metadata.getWorkTitle().trim());
            }
            if (cover != null) {
                String uploadImg = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImg").getValueField();
                String fileName = cover.getOriginalFilename();
                String fileExtension = FileUtil.getFileExtension(fileName);
                String path = cloudflareR2Util.saveToFileSystem(cover, cloudflareR2Util.getTempDirectory() + novelId + "/cover");
                fileName = "novel_" + novel.getId() + "_" + UUID.randomUUID().toString() + "." + fileExtension;
                String novelCoverPath = cloudflareR2Util.uploadImageToCloudflareR2(new File(path), fileName);
                novel.setPhotoUrl(uploadImg + fileName);
            }
//            List<Tag> uploadTag = metadata.getTags().stream().map(tag -> {
//                Tag t = new Tag();
//                t.setName(tag);
//                t.setTrueName(tag);
//                t.setPlatform("upload");
//                return t;
//            }).toList();
//            List<Tag> saveTag = tagService.save(uploadTag);
//            List<NovelTag> novelTagList = saveTag.stream().map(tag -> new NovelTag(novel.getId(), tag.getId())).toList();

            novelTagRepository.deleteByNovelId(novelId);
            for (String tag : metadata.getTags()) {
                Tag tagToSave = new Tag(tag, "upload", tag);
                Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName("upload",tagToSave.getTrueName());
                if (existingTag.isEmpty()) {
                    Tag syosetuTag = tagRepository.save(tagToSave);
                    novelTagRepository.save(new NovelTag(novel.getId(), syosetuTag.getId()));
                } else {
                    Tag tag1 = existingTag.get();
                    novelTagRepository.save(new NovelTag(novel.getId(), tag1.getId()));
                }
            }

//            novelTagService.save(novelTagList);
            FileUtil.deleteFolder(new File(cloudflareR2Util.getTempDirectory() + novelId + "/cover"));
            return new UploadNovelDTO(novel, tagsByNovelId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            userService.updateUploadByUserId(userId, true);
        }
    }

    public List<Novel> getUpload(Long userId) {
        List<UserNovelRelation> upload = userNovelRelationService.getUserNovelRelations(userId, "upload");
        List<Long> list = upload.stream().map(UserNovelRelation::getNovelId).toList();
        return novelRepository.findByIdInAndIsDeletedFalse(list);
    }


    public void update(MultipartFile file, Long novelId, String type, Long userId) {
        // 解析章节
        try {
            Novel saveNovel = novelRepository.findByIdAndIsDeletedFalse(novelId);
            String path = cloudflareR2Util.saveToFileSystem(file, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "file");
            String extension = FileUtil.getFileExtension(file.getOriginalFilename());
            // 根据文件类型进行不同处理
            switch (Objects.requireNonNull(extension).toLowerCase()) {
                case "txt":
                    updateTxtFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "updateUploadTxtContent" + UUID.randomUUID().toString() , type, saveNovel);
                    break;
                case "zip":
                    updateSaveZipFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "updateSaveZipFile"  + UUID.randomUUID().toString() , type, saveNovel);
                    break;
                case "epub":
                    updateEpubFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "updateUploadEpubContent" + UUID.randomUUID().toString() , type, saveNovel);
                    break;
                default:
                    break;
            }
            FileUtil.deleteFolder(new File(cloudflareR2Util.getTempDirectory() + saveNovel.getId()));
        }catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            userService.updateUploadByUserId(userId, true);
        }
    }

    public void upload(MultipartFile file, MultipartFile cover,UploadMetadata metadata, Long userId) {
        try {
            String uploadImg = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImg").getValueField();
//            List<Tag> uploadTag = metadata.getTags().stream().map(tag -> {
//                Tag t = new Tag();
//                t.setName(tag);
//                t.setTrueName(tag);
//                t.setPlatform("upload");
//                return t;
//            }).toList();
//            List<Tag> saveTag = tagService.save(uploadTag);




            Novel novel = new Novel();
            novel.setTrueName(metadata.getOriginalTitle());
            novel.setTitle(metadata.getWorkTitle());
            novel.setUp(0L);
            novel.setNovelLike(0L);
            novel.setNovelRead(0L);
            novel.setRecommend(0L);
            novel.setAuthorName("");
            novel.setAuthorId("");
            novel.setFontNumber(0L);
            novel.setPlatform("upload");
            novel.setPhotoUrl(null);// 这里要改一下
            Novel saveNovel = novelRepository.save(novel);
            saveNovel.setTrueId("upload" + saveNovel.getId());
            saveNovel = novelRepository.save(saveNovel);
            // 添加汉化记录
            UserNovelRelation relation = new UserNovelRelation();
            relation.setUserId(userId);
            relation.setNovelId(saveNovel.getId());
            relation.setPlatform("upload");
            userNovelRelationService.saveUserNovelRelation(relation);

            try {
                String fileName = cover.getOriginalFilename();
                String fileExtension = FileUtil.getFileExtension(fileName);
                String path = cloudflareR2Util.saveToFileSystem(cover, cloudflareR2Util.getTempDirectory() + novel.getId() + "/uploadCoverImg" + UUID.randomUUID().toString());
                fileName = "novel_" + saveNovel.getId() + "." + fileExtension;
                String novelCoverPath = cloudflareR2Util.uploadImageToCloudflareR2(new File(path), fileName);
                saveNovel.setPhotoUrl(uploadImg + fileName);
                novelRepository.save(saveNovel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Novel finalSaveNovel = saveNovel;
            for (String tag : metadata.getTags()) {
                Tag tagToSave = new Tag(tag, "upload", tag);
                Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName("upload",tagToSave.getTrueName());
                if (existingTag.isEmpty()) {
                    Tag syosetuTag = tagRepository.save(tagToSave);
                    novelTagRepository.save(new NovelTag(finalSaveNovel.getId(), syosetuTag.getId()));
                } else {
                    Tag tag1 = existingTag.get();
                    novelTagRepository.save(new NovelTag(finalSaveNovel.getId(), tag1.getId()));
                }
            }
//            List<NovelTag> novelTagList = saveTag.stream().map(tag -> new NovelTag(finalSaveNovel.getId(), tag.getId())).toList();
//            novelTagService.save(novelTagList);
            if (metadata.isUploadFile()) {
                // 解析章节
                try {
                    String path = cloudflareR2Util.saveToFileSystem(file, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "file");
                    String extension = FileUtil.getFileExtension(file.getOriginalFilename());
                    // 根据文件类型进行不同处理
                    switch (Objects.requireNonNull(extension).toLowerCase()) {
                        case "txt":
                            saveTxtFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "uploadTxtContent" + UUID.randomUUID().toString() , metadata.getType(), saveNovel);
                            break;
                        case "zip":
                        saveZipFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "uploadZipContent" + UUID.randomUUID().toString() , metadata.getType(), saveNovel);
                            break;
                        case "epub":
                            saveEpubFile(path, cloudflareR2Util.getTempDirectory() + saveNovel.getId() + File.separator + "uploadEpubContent" + UUID.randomUUID().toString() , metadata.getType(), saveNovel);
                            break;
                        default:
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            FileUtil.deleteFolder(new File(cloudflareR2Util.getTempDirectory() + novel.getId()));
        } finally {
            userService.updateUploadByUserId(userId, true);
        }
    }

    protected void updateEpubFile(String path, String saveDir, String type, Novel novel) throws IOException {
        Optional<Integer> optional1 = chapterRepository.findMaxChapterNumberByNovelId(novel.getId());
        Optional<Integer> optional2 = chapterExecuteRepository.findMaxChapterNumberByNovelId(novel.getId());
        int chapterNumber = Math.max(optional1.orElse(1), optional2.orElse(1));
        try {
            String uploadImgTag = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImgTag").getValueField();
            String uploadImg = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImg").getValueField();
            EpubTools.run(path, saveDir);
            int epNum = 1;
            String epFormat = "EP%s";
            if (type.equals("original")) {
                List<ChapterExecute> chapterExecuteList = new ArrayList<>();
                List<ChapterImageLink> chapterImageLinkList = new ArrayList<>();
                String uploadChapterPath = saveDir + File.separator + "uploadChapter";
                String uploadImgPath = saveDir + File.separator + "uploadImg";
                File uploadChapterDir = new File(uploadChapterPath);
                if (uploadChapterDir.exists() && uploadChapterDir.isDirectory()) {
                    File[] txtFiles = uploadChapterDir.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
                    if (txtFiles != null && txtFiles.length > 0) {
                        File uploadImgFile = new File(uploadImgPath);
                        File[] uploadImgChildren = uploadImgFile.listFiles(File::isFile);
                        for (File txtFile : txtFiles) {
                            List<String> chunks = splitFileByContentLength(txtFile.getAbsolutePath(), 30000, 8000);
                            boolean found = true;
                            for (String chunk : chunks) {
                                String episodeFile = String.format(epFormat, String.format("%04d", (epNum + chapterNumber)));
                                ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile, (epNum + chapterNumber), chunk, 0, novel.getId() + "_" + (epNum + chapterNumber), false);
                                chapterExecuteList.add(chapterExecute);
                                epNum += 1;

                                if (found) {
                                    String name = txtFile.getName();
                                    String extractPrefix = FileUtil.extractPrefix(name);
                                    List<File> imgFiles = FileUtil.filterImagesByPrefix(uploadImgChildren, extractPrefix);
                                    for (File imgFile : imgFiles) {
                                        chapterExecute.setOwnPhoto(true);

                                        String s = novel.getId() + "_" + (epNum + chapterNumber) + "_" + imgFile.getName();
                                        String cfImgUrl = cloudflareR2Util.uploadImageToCloudflareR2(imgFile, s);
                                        String format = uploadImgTag.replaceAll("uploadImg",uploadImg + s);
                                        ChapterImageLink chapterImageLink = new ChapterImageLink(novel.getId() + "_" + (epNum + chapterNumber), format, FileUtil.processFileName(imgFile.getName()));
                                        chapterImageLink.setCf(true);
                                        chapterImageLinkList.add(chapterImageLink);
                                    }
                                    found = false;
                                }

                            }
                        }
                        chapterExecuteRepository.saveAll(chapterExecuteList);
                        chapterImageLinkRepository.saveAll(chapterImageLinkList);
                    }
                }

            }
            else {
                long fontSize = 0L;
                List<Chapter> chapterList = new ArrayList<>();
                List<ChapterImageLink> chapterImageLinkList = new ArrayList<>();
                String uploadChapterPath = saveDir + File.separator + "uploadChapter";
                String uploadImgPath = saveDir + File.separator + "uploadImg";
                File uploadChapterDir = new File(uploadChapterPath);
                if (uploadChapterDir.exists() && uploadChapterDir.isDirectory()) {
                    File[] txtFiles = uploadChapterDir.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
                    if (txtFiles != null && txtFiles.length > 0) {
                        File uploadImgFile = new File(uploadImgPath);
                        File[] uploadImgChildren = uploadImgFile.listFiles(File::isFile);
                        for (File txtFile : txtFiles) {
                            List<String> chunks = splitFileByContentLength(txtFile.getAbsolutePath(), 30000, 8000);
                            boolean found = true;
                            for (String chunk : chunks) {
                                String episodeFile = String.format(epFormat, String.format("%04d", (epNum + chapterNumber)));
                                Chapter chapter = new Chapter();
                                chapter.setTrueId(novel.getId() + "_" + (epNum + chapterNumber));
                                chapter.setOwnPhoto(false);
                                chapter.setTitle(episodeFile);
                                chapter.setNovelId(novel.getId());
                                chapter.setChapterNumber((epNum + chapterNumber));
                                chapter.setContent(chunk);
                                chapterList.add(chapter);
                                fontSize += chunk.length();
                                epNum += 1;

                                if (found) {
                                    String name = txtFile.getName();
                                    String extractPrefix = FileUtil.extractPrefix(name);
                                    List<File> imgFiles = FileUtil.filterImagesByPrefix(uploadImgChildren, extractPrefix);
                                    for (File imgFile : imgFiles) {
                                        chapter.setOwnPhoto(true);
                                        String s = novel.getId() + "_" + chapter.getChapterNumber() + "_" + imgFile.getName();
                                        String cfImgUrl = cloudflareR2Util.uploadImageToCloudflareR2(imgFile, s);
                                        String format = uploadImgTag.replaceAll("uploadImg",uploadImg + s);
                                        ChapterImageLink chapterImageLink = new ChapterImageLink(novel.getId() + "_" + chapter.getChapterNumber(), format, FileUtil.processFileName(imgFile.getName()));
                                        chapterImageLink.setCf(true);
                                        chapterImageLinkList.add(chapterImageLink);
                                    }
                                    found = false;
                                }

                            }
                        }
                        chapterRepository.saveAll(chapterList);
                        chapterSyncService.saveAll(chapterList);
                        chapterImageLinkRepository.saveAll(chapterImageLinkList);
                    }
                }
                novel.setFontNumber(novel.getFontNumber() + fontSize);
                novelRepository.save(novel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSaveZipFile(String path, String saveDir, String type, Novel novel) throws IOException {
        Optional<Integer> optional1 = chapterRepository.findMaxChapterNumberByNovelId(novel.getId());
        Optional<Integer> optional2 = chapterExecuteRepository.findMaxChapterNumberByNovelId(novel.getId());
        int chapterNumber = Math.max(optional1.orElse(1), optional2.orElse(1));
        boolean b = ZipExtractor.extractZip(path, saveDir);
        if (b) {
            File[] sortedTxtFiles = FileUtil.getSortedTxtFiles(saveDir);
            int epNum = 1;
            String epFormat = "EP%s";
            if (type.equals("original")) {
                List<ChapterExecute> chapterExecuteList = new ArrayList<>();
                for (File sortedTxtFile : sortedTxtFiles) {
                    try {
                        List<String> chunks = splitFileByContentLength(sortedTxtFile.getAbsolutePath(), 30000, 8000);
                        for (String chunk : chunks) {
                            String episodeFile = String.format(epFormat, String.format("%04d", epNum + chapterNumber));
                            ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile + sortedTxtFile.getName(), epNum + chapterNumber, chunk, 0, novel.getId() + "_" + (epNum + chapterNumber), false);
                            chapterExecuteList.add(chapterExecute);
                            epNum += 1;
                        }
                        sortedTxtFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                chapterExecuteRepository.saveAll(chapterExecuteList);
            } else {
                Long fontNumber = 0L;
                List<Chapter> chapterList = new ArrayList<>();
                for (File sortedTxtFile : sortedTxtFiles) {
                    try {
                        List<String> chunks = splitFileByContentLength(sortedTxtFile.getAbsolutePath(), 30000, 8000);
                        for (String chunk : chunks) {
                            String episodeFile = String.format(epFormat, String.format("%04d", epNum + chapterNumber));
                            Chapter chapter = new Chapter();
                            chapter.setTrueId(novel.getId() + "_" + (epNum + chapterNumber));
                            chapter.setOwnPhoto(false);
                            chapter.setTitle(episodeFile + sortedTxtFile.getName());
                            chapter.setNovelId(novel.getId());
                            chapter.setChapterNumber(epNum + chapterNumber);
                            chapter.setContent(chunk);
                            chapterList.add(chapter);
                            epNum += 1;
                            fontNumber += chunk.length();
                        }
                        sortedTxtFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                novel.setFontNumber(fontNumber);
                novelRepository.save(novel);
                chapterRepository.saveAll(chapterList);
                chapterSyncService.saveAll(chapterList);
            }
        }
    }

    protected void saveEpubFile(String path, String saveDir, String type, Novel novel) throws IOException {
        try {
            String uploadImgTag = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImgTag").getValueField();
            String uploadImg = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("uploadImg").getValueField();
            EpubTools.run(path, saveDir);
            int epNum = 1;
            String epFormat = "EP%s";
            if (type.equals("original")) {
                List<ChapterExecute> chapterExecuteList = new ArrayList<>();
                List<ChapterImageLink> chapterImageLinkList = new ArrayList<>();
                String uploadChapterPath = saveDir + File.separator + "uploadChapter";
                String uploadImgPath = saveDir + File.separator + "uploadImg";
                File uploadChapterDir = new File(uploadChapterPath);
                if (uploadChapterDir.exists() && uploadChapterDir.isDirectory()) {
                    File[] txtFiles = uploadChapterDir.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
                    assert txtFiles != null;
                    Arrays.sort(txtFiles, (f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        return name1.compareTo(name2); // 替换为自然排序算法
                    });
                    for (File txtFile : txtFiles) {
                        System.out.println(txtFile.getAbsolutePath());
                    }
                    if (txtFiles.length > 0) {
                        File uploadImgFile = new File(uploadImgPath);
                        File[] uploadImgChildren = uploadImgFile.listFiles(File::isFile);
                        for (File txtFile : txtFiles) {
                            List<String> chunks = splitFileByContentLength(txtFile.getAbsolutePath(), 30000, 8000);
                            boolean found = true;
                            for (String chunk : chunks) {
                                String episodeFile = String.format(epFormat, String.format("%04d", epNum));
                                ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile, epNum, chunk, 0, novel.getId() + "_" + epNum, false);
                                chapterExecuteList.add(chapterExecute);
                                epNum += 1;

                                if (found) {
                                    String name = txtFile.getName();
                                    String extractPrefix = FileUtil.extractPrefix(name);
                                    List<File> imgFiles = FileUtil.filterImagesByPrefix(uploadImgChildren, extractPrefix);
                                    for (File imgFile : imgFiles) {
                                        chapterExecute.setOwnPhoto(true);
                                        String s = novel.getId() + "_" + epNum + "_" + imgFile.getName();
                                        String cfImgUrl = cloudflareR2Util.uploadImageToCloudflareR2(imgFile, s);
                                        String format = uploadImgTag.replaceAll("uploadImg",uploadImg + s);
                                        ChapterImageLink chapterImageLink = new ChapterImageLink(novel.getId() + "_" + epNum, format, FileUtil.processFileName(imgFile.getName()));
                                        chapterImageLink.setCf(true);
                                        chapterImageLinkList.add(chapterImageLink);
                                    }
                                    found = false;
                                }

                            }
                        }
                        chapterExecuteRepository.saveAll(chapterExecuteList);
                        chapterImageLinkRepository.saveAll(chapterImageLinkList);
                    }
                }

            }
            else {
                long fontSize = 0L;
                List<Chapter> chapterList = new ArrayList<>();
                List<ChapterImageLink> chapterImageLinkList = new ArrayList<>();
                String uploadChapterPath = saveDir + File.separator + "uploadChapter";
                String uploadImgPath = saveDir + File.separator + "uploadImg";
                File uploadChapterDir = new File(uploadChapterPath);
                if (uploadChapterDir.exists() && uploadChapterDir.isDirectory()) {
                    File[] txtFiles = uploadChapterDir.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
                    if (txtFiles != null && txtFiles.length > 0) {
                        File uploadImgFile = new File(uploadImgPath);
                        File[] uploadImgChildren = uploadImgFile.listFiles(File::isFile);
                        for (File txtFile : txtFiles) {
                            List<String> chunks = splitFileByContentLength(txtFile.getAbsolutePath(), 30000, 8000);
                            boolean found = true;
                            for (String chunk : chunks) {
                                String episodeFile = String.format(epFormat, String.format("%04d", epNum));
                                Chapter chapter = new Chapter();
                                chapter.setTrueId(novel.getId() + "_" + epNum);
                                chapter.setOwnPhoto(false);
                                chapter.setTitle(episodeFile);
                                chapter.setNovelId(novel.getId());
                                chapter.setChapterNumber(epNum);
                                chapter.setContent(chunk);
                                chapterList.add(chapter);
                                fontSize += chunk.length();
                                epNum += 1;

                                if (found) {
                                    String name = txtFile.getName();
                                    String extractPrefix = FileUtil.extractPrefix(name);
                                    List<File> imgFiles = FileUtil.filterImagesByPrefix(uploadImgChildren, extractPrefix);
                                    for (File imgFile : imgFiles) {
                                        chapter.setOwnPhoto(true);
                                        String s = novel.getId() + "_" + chapter.getChapterNumber() + "_" + imgFile.getName();
                                        String cfImgUrl = cloudflareR2Util.uploadImageToCloudflareR2(imgFile, s);
                                        String format = uploadImgTag.replaceAll("uploadImg",uploadImg + s);
                                        ChapterImageLink chapterImageLink = new ChapterImageLink(novel.getId() + "_" + chapter.getChapterNumber(), format, FileUtil.processFileName(imgFile.getName()));
                                        chapterImageLink.setCf(true);
                                        chapterImageLinkList.add(chapterImageLink);
                                    }
                                    found = false;
                                }

                            }
                        }
                        chapterRepository.saveAll(chapterList);
                        chapterSyncService.saveAll(chapterList);
                        chapterImageLinkRepository.saveAll(chapterImageLinkList);
                    }
                }
                novel.setFontNumber(fontSize);
                novelRepository.save(novel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveZipFile(String path, String saveDir, String type, Novel novel) throws IOException {
        boolean b = ZipExtractor.extractZip(path, saveDir);
        if (b) {
            File[] sortedTxtFiles = FileUtil.getSortedTxtFiles(saveDir);
            int epNum = 1;
            String epFormat = "EP%s";
            if (type.equals("original")) {
                List<ChapterExecute> chapterExecuteList = new ArrayList<>();
                for (File sortedTxtFile : sortedTxtFiles) {
                    try {
                        List<String> chunks = splitFileByContentLength(sortedTxtFile.getAbsolutePath(), 30000, 8000);
                        for (String chunk : chunks) {
                            String episodeFile = String.format(epFormat, String.format("%04d", epNum));
                            ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile + sortedTxtFile.getName(), epNum, chunk, 0, novel.getId() + "_" + epNum, false);
                            chapterExecuteList.add(chapterExecute);
                            epNum += 1;
                        }
                        sortedTxtFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                chapterExecuteRepository.saveAll(chapterExecuteList);
            } else {
                Long fontNumber = 0L;
                List<Chapter> chapterList = new ArrayList<>();
                for (File sortedTxtFile : sortedTxtFiles) {
                    try {
                        List<String> chunks = splitFileByContentLength(sortedTxtFile.getAbsolutePath(), 30000, 8000);
                        for (String chunk : chunks) {
                            String episodeFile = String.format(epFormat, String.format("%04d", epNum));
                            Chapter chapter = new Chapter();
                            chapter.setTrueId(novel.getId() + "_" + epNum);
                            chapter.setOwnPhoto(false);
                            chapter.setTitle(episodeFile + sortedTxtFile.getName());
                            chapter.setNovelId(novel.getId());
                            chapter.setChapterNumber(epNum);
                            chapter.setContent(chunk);
                            chapterList.add(chapter);
                            epNum += 1;
                            fontNumber += chunk.length();
                        }
                        sortedTxtFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                novel.setFontNumber(fontNumber);
                novelRepository.save(novel);
                chapterRepository.saveAll(chapterList);
                chapterSyncService.saveAll(chapterList);
            }
        }
    }

    protected void updateTxtFile(String path, String saveDir, String type, Novel novel)  throws IOException {
        Optional<Integer> optional1 = chapterRepository.findMaxChapterNumberByNovelId(novel.getId());
        Optional<Integer> optional2 = chapterExecuteRepository.findMaxChapterNumberByNovelId(novel.getId());
        int chapterNumber = Math.max(optional1.orElse(1), optional2.orElse(1));
        List<String> fileList = ChapterSplitterUtil.splitAndSaveChapters(path, saveDir);
        String epFormat = "EP%s";
        if (type.equals("original")) {
            List<ChapterExecute> chapterExecuteList = new ArrayList<>();
            for (String file : fileList) {
                try {
                    FileInfo fileInfo = extractFileInfo(file);
                    int i = fileInfo.getExtractedNumber() + chapterNumber;
                    String episodeFile = String.format(epFormat, String.format("%04d", i));
                    String fileContent = FileUtil.readContent(file);
                    ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile + fileInfo.getChapterTitle(), i, fileContent, 0, novel.getId() + "_" + i, false);
                    chapterExecuteList.add(chapterExecute);
                    new File(file).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            chapterExecuteRepository.saveAll(chapterExecuteList);
        } else {
            Long fontNumber = 0L;
            List<Chapter> chapterList = new ArrayList<>();
            for (String file : fileList) {
                try {
                    FileInfo fileInfo = extractFileInfo(file);
                    int i = fileInfo.getExtractedNumber() + chapterNumber;
                    String episodeFile = String.format(epFormat, String.format("%04d", i));
                    String fileContent = FileUtil.readContent(file);
                    Chapter chapter = new Chapter();
                    chapter.setTrueId(novel.getId() + "_" + i);
                    chapter.setOwnPhoto(false);
                    chapter.setTitle(episodeFile + fileInfo.getChapterTitle());
                    chapter.setNovelId(novel.getId());
                    chapter.setChapterNumber(i);
                    chapter.setContent(fileContent);
                    chapterList.add(chapter);
                    new File(file).delete();
                    fontNumber += fileContent.length();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            novel.setFontNumber(novel.getFontNumber() + fontNumber);
            novelRepository.save(novel);
            chapterRepository.saveAll(chapterList);
            chapterSyncService.saveAll(chapterList);

        }
    }


    protected void saveTxtFile(String path, String saveDir, String type, Novel novel) throws IOException {
        List<String> fileList = ChapterSplitterUtil.splitAndSaveChapters(path, saveDir);
        String epFormat = "EP%s";
        if (type.equals("original")) {
            List<ChapterExecute> chapterExecuteList = new ArrayList<>();
            for (String file : fileList) {
                try {
                    FileInfo fileInfo = extractFileInfo(file);
                    String episodeFile = String.format(epFormat, String.format("%04d", fileInfo.getExtractedNumber()));
                    String fileContent = FileUtil.readContent(file);
                    ChapterExecute chapterExecute = new ChapterExecute(novel.getId(), episodeFile + fileInfo.getChapterTitle(), fileInfo.getExtractedNumber(), fileContent, 0, novel.getId() + "_" + fileInfo.getExtractedNumber(), false);
                    chapterExecuteList.add(chapterExecute);
                    new File(file).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            chapterExecuteRepository.saveAll(chapterExecuteList);
        } else {
            Long fontNumber = 0L;
            List<Chapter> chapterList = new ArrayList<>();
            for (String file : fileList) {
                try {
                    FileInfo fileInfo = extractFileInfo(file);
                    String episodeFile = String.format(epFormat, String.format("%04d", fileInfo.getExtractedNumber()));
                    String fileContent = FileUtil.readContent(file);
                    Chapter chapter = new Chapter();
                    chapter.setTrueId(novel.getId() + "_" + fileInfo.getExtractedNumber());
                    chapter.setOwnPhoto(false);
                    chapter.setTitle(episodeFile + fileInfo.getChapterTitle());
                    chapter.setNovelId(novel.getId());
                    chapter.setChapterNumber(fileInfo.getExtractedNumber());
                    chapter.setContent(fileContent);
                    chapterList.add(chapter);
                    new File(file).delete();
                    fontNumber += fileContent.length();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            novel.setFontNumber(fontNumber);
            novelRepository.save(novel);
            chapterRepository.saveAll(chapterList);
            chapterSyncService.saveAll(chapterList);

        }
    }


    private static List<String> splitFileByContentLength(String filePath, int maxChunkLength, int targetChunkLength) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int lineLength = line.length();
                if (currentLength + lineLength + 1 > maxChunkLength) { // +1 是为了考虑换行符
                    // 如果当前块已经超过目标长度，则分割
                    if (!currentChunk.isEmpty()) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                        currentLength = 0;
                    }
                }
                if (lineLength > maxChunkLength) {
                    // 如果单行超过最大块长度，则将该行分割
                    while (lineLength > maxChunkLength) {
                        chunks.add(line.substring(0, maxChunkLength));
                        line = line.substring(maxChunkLength);
                        lineLength = line.length();
                    }
                }
                if (currentLength + lineLength + 1 > targetChunkLength) {
                    // 如果添加当前行后超过目标块长度，则分割
                    if (!currentChunk.isEmpty()) {
                        chunks.add(currentChunk.toString());
                        currentChunk.setLength(0);
                        currentLength = 0;
                    }
                }
                currentChunk.append(line).append("\n");
                currentLength += lineLength + 1; // +1 是为了考虑换行符
            }
        }

        // 添加最后一个块
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }


    public FileInfo extractFileInfo(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        // 使用正则表达式匹配文件名格式
        String regex = "EP(\\d{4})_gaga分隔符_(.+?)\\.txt";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            // 提取EP编号和章节标题
            String epNumber = matcher.group(1); // "0001"
            String chapterTitle = matcher.group(2); // "第1章"

            // 从EP编号中提取数字部分
            int extractedNumber = Integer.parseInt(epNumber);

            return new FileInfo(epNumber, extractedNumber, chapterTitle);
        } else {
            System.out.println("文件名不符合预期格式: " + fileName);
            return null;
        }
    }
    
}

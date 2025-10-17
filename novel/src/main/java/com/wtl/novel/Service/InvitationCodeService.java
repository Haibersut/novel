package com.wtl.novel.Service;

import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.InvitationCodeRepository;
import com.wtl.novel.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InvitationCodeService {
    @Autowired
    private InvitationCodeRepository invitationCodeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final Date date = new Date();
    private final ConcurrentMap<Long, Object> userLocks = new ConcurrentHashMap<>();

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成并保存一个新的邀请码
     * @return 保存后的邀请码实体
     */
    @Transactional
    public InvitationCode createInvitationCode(Credential credential) {
        int credentialLocalDateTime = Integer.parseInt(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("credentialLocalDateTime").getValueField());
        int existsByUserIdAndCreatedAtBetween = Integer.parseInt(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("existsByUserIdAndCreatedAtBetween").getValueField());
        if (credential == null || credential.getCreatedAt() == null ||
                ChronoUnit.DAYS.between(credential.getCreatedAt(), LocalDateTime.now()) < credentialLocalDateTime) {
            throw new RuntimeException("注册时间未满" + credentialLocalDateTime + "天");
        }
        User user = credential.getUser();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(existsByUserIdAndCreatedAtBetween);
        boolean hasCreated = invitationCodeRepository
                .existsByUserIdAndCreatedAtBetween(user.getId(), start, end);

        if (hasCreated) {
            throw new RuntimeException(existsByUserIdAndCreatedAtBetween+ "天内已经兑换过了");
        }

        Object lock = userLocks.putIfAbsent(user.getId(), new Object());
        if (lock != null) {
            return null; // A request for this user is already being processed.
        }

        try {
            Integer createInvitationCode = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("createInvitationCode").getValueField());
            if (user.getPoint() < createInvitationCode) {
                return null;
            }
            user.setPoint(user.getPoint() - createInvitationCode);
            InvitationCode code = new InvitationCode();
            code.setUserId(user.getId());
            // 生成唯一邀请码，可根据需要调整格式
            code.setCode(generateRandomString(16) + System.currentTimeMillis() + "_" + user.getId());
            userRepository.save(user);
            return invitationCodeRepository.save(code);
        } finally {
            // Always release the lock when the process is done, regardless of success or failure.
            userLocks.remove(user.getId());
        }
    }


    public InvitationCode findByCode(String code) {
        return invitationCodeRepository.findByCode(code);
    }

    public void bindToEmail(InvitationCode invitationCode, String email) {
        invitationCode.setUsed(true);
        invitationCode.setBoundEmail(email);
//        Long userId = invitationCode.getUserId();
//        userRepository.increasePointByUserId(userId);
        invitationCodeRepository.save(invitationCode);
    }
    /**
     * 创建或获取邀请码
     * @param userId 用户 ID
     * @return 邀请码对象
     */
//    public InvitationCode createOrGetInvitationCode(Long userId) {
//        // 检查用户是否已经存在未使用的邀请码
//        Optional<InvitationCode> existingCode = invitationCodeRepository.findByUserIdAndUsed(userId, false);
//        return existingCode.orElseGet(() -> generateNewInvitationCode(userId));
//    }

    public List<InvitationCode> getInvitationCode(Long userId) {
        // 检查用户是否已经存在未使用的邀请码
        return invitationCodeRepository.findByUserIdAndUsed(userId, false);
    }

    /**
     * 生成新的邀请码
     * @param userId 用户 ID
     * @return 新的邀请码对象
     */
    private InvitationCode generateNewInvitationCode(Long userId) {
        // 生成随机邀请码
        String code = generateUniqueCode();

        // 创建新的邀请码对象
        InvitationCode newCode = new InvitationCode();
        newCode.setCode(code);
        newCode.setUsed(false);
        newCode.setUserId(userId);

        // 保存到数据库
        return invitationCodeRepository.save(newCode);
    }

    /**
     * 生成唯一的邀请码（包含时间戳）
     * @return 唯一的邀请码
     * @throws RuntimeException 如果经过多次重试仍无法生成唯一的邀请码
     */
    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZqwertyuioplkjhgfdsazxcvbnm0123456789";
        StringBuilder code = new StringBuilder();
        final int MAX_RETRIES = 3; // 最大重试次数
        int retryCount = 0; // 当前重试次数

        // 生成随机字符部分
        for (int i = 0; i < 26; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }

        // 添加时间戳部分
        String timestamp = String.valueOf(System.currentTimeMillis());
        code.append(timestamp);

        // 检查生成的邀请码是否已存在，如果存在则重新生成
        while (invitationCodeRepository.findByCode(code.toString()) != null) {
            retryCount++;
            if (retryCount > MAX_RETRIES) {
                throw new RuntimeException("经过多次重试仍无法生成唯一的邀请码");
            }
            code.setLength(0);
            for (int i = 0; i < 26; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code.append(timestamp);
        }

        return code.toString();
    }
}
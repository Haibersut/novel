import CryptoJS, {HmacSHA256, lib} from 'crypto-js';

/**
 * 生成签名
 * @param {Object} config - Axios 请求配置
 * @param SECRET_KEY
 * @returns {Object} 包含签名、时间戳和随机数的 headers
 */
export function generateSignature(config, SECRET_KEY) {
    const timestamp = Date.now();
    const nonce = lib.WordArray.random(16).toString();

    // 确保 URL 是完整的
    let url = config.url;
    if (!url.startsWith('http')) {
        const baseURL = config.baseURL || window.location.origin;
        url = new URL(url, baseURL).href;
    }

    // 标准化请求要素
    const method = config.method.toUpperCase();
    const path = new URL(url).pathname;
    let urlParams = {};
    if (config.url.includes('?')) {
        const queryStr = config.url.split('?')[1];
        const queryParams = new URLSearchParams(queryStr);
        for (const [key, value] of queryParams.entries()) {
            urlParams[key] = value;
        }
    }

    // 将 config.params 中的所有值转换为字符串
    const stringifiedParams = config.params ? Object.entries(config.params).reduce((acc, [key, value]) => {
        acc[key] = String(value);
        return acc;
    }, {}) : {};

    // 合并 URL 中的查询参数和 config.params 中的参数
    const mergedParams = {
        ...urlParams,
        ...stringifiedParams
    };
    const params = sortObject(mergedParams);
    const data = config.data ? sortObject(config.data) : {};

    // 构造签名字符串
    const signStr = [
        method,
        path,
        JSON.stringify(params),
        JSON.stringify(data),
        timestamp,
        nonce
    ].join('|');
    // HMAC-SHA256加密
    const signature = HmacSHA256(signStr, SECRET_KEY).toString();

    return `;${signature};${timestamp};${nonce}`;
}

/**
 * 对象按 key 排序（防止顺序不同导致签名不一致）
 * @param {Object} obj - 需要排序的对象
 * @returns {Object} 排序后的对象
 */
function sortObject(obj) {
    if (typeof obj !== 'object' || obj === null) return obj;
    if (Array.isArray(obj)) return obj.map(sortObject);
    return Object.keys(obj).sort().reduce((acc, key) => {
        acc[key] = sortObject(obj[key]);
        return acc;
    }, {});
}






export const cryptoUtils = {
    encrypt(plaintext, keyStr) {
        // 时间戳生成（UTC时间）
        const currentMinute = Math.floor(Date.now() / 60000) * 60000;
        const combinedKey = keyStr + currentMinute;

        // 正确生成密钥
        const key = CryptoJS.SHA256(combinedKey);

        // 生成IV（16字节）
        const iv = CryptoJS.lib.WordArray.random(16);

        // 加密配置
        const encrypted = CryptoJS.AES.encrypt(plaintext, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });

        // 合并IV+密文（二进制模式）
        const combined = iv.concat(encrypted.ciphertext);
        return combined.toString(CryptoJS.enc.Base64);
    },

    decrypt(ciphertextBase64, keyStr) {
        try {
            // 解析Base64
            const combined = CryptoJS.enc.Base64.parse(ciphertextBase64);

            // 分离IV（前16字节）和密文
            const iv = CryptoJS.lib.WordArray.create(combined.words.slice(0, 4)); // 16 bytes
            const encrypted = CryptoJS.lib.WordArray.create(combined.words.slice(4));

            // 尝试两套时间戳
            const timestamps = [
                Math.floor(Date.now() / 60000) * 60000,       // 当前分钟（UTC）
                Math.floor(Date.now() / 60000) * 60000 - 60000 // 前一分钟
            ];

            for (const timestamp of timestamps) {
                try {
                    // 生成密钥（保持二进制）
                    const combinedKey = keyStr + timestamp;
                    const key = CryptoJS.SHA256(combinedKey);

                    // 解密配置
                    const decrypted = CryptoJS.AES.decrypt(
                        { ciphertext: encrypted },
                        key,
                        { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 }
                    );

                    // 验证解密结果
                    const result = decrypted.toString(CryptoJS.enc.Utf8);
                    if (result && result.length > 0) return result;
                } catch (e) {
                    console.warn(`时间戳 ${timestamp} 解密失败`, e);
                }
            }
            throw new Error('所有密钥尝试失败');
        } catch (e) {
            throw new Error(`解密过程错误: ${e.message}`);
        }
    }
};
/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.constant.GlobalConstants;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.enums.FileUploadProgressStatusEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.model.resp.file.FileStatisticsResp;
import top.continew.admin.system.model.resp.file.FileUploadProgressResp;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.StorageService;
import top.continew.admin.system.util.FileNameGenerator;
import top.continew.admin.system.util.StoragePathUtils;
import top.continew.admin.system.util.StoragePathValidator;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.storage.core.FileStorageService;
import top.continew.starter.storage.core.UploadPretreatment;
import top.continew.starter.storage.domain.file.EnhancedMultipartFile;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件业务实现
 *
 * @author Charles7c
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl
    extends BaseServiceImpl<FileMapper, FileDO, FileResp, FileResp, FileQuery, FileReq>
    implements FileService {

    private static final String FILE_UPLOAD_PROGRESS_PREFIX = "file:upload:progress:";
    private static final Duration FILE_UPLOAD_PROGRESS_EXPIRE = Duration.ofHours(1);
    private static final int STORAGE_PATH_QUERY_BATCH_SIZE = 500;

    private final FileStorageService fileStorageService;
    @Lazy
    @Resource
    private StorageService storageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<FileDO> fileList = baseMapper.lambdaQuery().in(FileDO::getId, ids).list();
        if (CollUtil.isEmpty(fileList)) {
            return;
        }
        // 批量获取存储配置
        Map<Long, List<FileDO>> fileListGroup =
            fileList.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
        List<StorageDO> storageList = storageService.listByIds(fileListGroup.keySet());
        Map<Long, StorageDO> storageGroup = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, Function.identity(),
                (existing, replacement) -> existing));
        // 删除记录
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            List<Long> idList = CollUtils.mapToList(entry.getValue(), FileDO::getId);
            if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
                baseMapper.deleteByIds(idList);
            } else {
                baseMapper.deleteWithoutRecycleBin(idList, UserContextHolder
                    .getUserId(), LocalDateTime.now(GlobalConstants.DEFAULT_ZONE_ID));
            }
        }
        // 物理删除前预查缩略图引用：本批记录已逻辑删除，查询结果只包含仍然有效的记录
        Map<Long, Set<String>> referencedThumbnailPaths = new HashMap<>();
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            referencedThumbnailPaths.put(entry.getKey(), this
                .findReferencedThumbnailPaths(entry.getKey(), entry.getValue()));
        }
        // 删除实际文件
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            Set<String> protectedPaths = referencedThumbnailPaths.getOrDefault(entry
                .getKey(), Set.of());
            // 同一批记录之间也可能出现缩略图路径与主文件路径重合，先收集本批主路径并
            // 记录已经处理过的物理路径，保证每个源路径只移动/删除一次
            Set<String> batchMainPaths = entry.getValue()
                .stream()
                .map(FileDO::getPath)
                .collect(Collectors.toSet());
            Set<String> handledPaths = new HashSet<>();
            entry.getValue().forEach(file -> this.deleteFile(file, storage, protectedPaths,
                batchMainPaths, handledPaths));
        }
    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode) {
        return this.upload(file, parentPath, storageCode, null);
    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode,
        String uploadTaskId) {
        return this.doUpload(file, parentPath, storageCode, FileNameUtil.extName(file
            .getOriginalFilename()), uploadTaskId);
    }

    @Override
    public FileInfo upload(File file, String parentPath, String storageCode) {
        return this.doUpload(file, parentPath, storageCode, FileNameUtil.extName(file.getName()),
            null);
    }

    @Override
    public Long createDir(FileReq req) {
        String parentPath = req.getParentPath();
        // 校验上级目录路径与名称，防止通过 ../ 等路径穿越在存储根目录之外创建目录
        StoragePathValidator.validate(parentPath);
        StoragePathValidator.validateName(req.getOriginalName());
        FileDO file = baseMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getName, req.getOriginalName())
            .eq(FileDO::getType, FileTypeEnum.DIR)
            .one();
        CheckUtils.throwIfNotNull(file, "文件夹已存在");
        // 存储引擎需要一致
        StorageDO storage = storageService.getDefaultStorage();
        if (!StringConstants.SLASH.equals(parentPath)) {
            FileDO parentFile = baseMapper.lambdaQuery()
                .eq(FileDO::getPath, parentPath)
                .eq(FileDO::getType, FileTypeEnum.DIR)
                .one();
            CheckUtils.throwIfNull(parentFile, "父级文件夹不存在");
            CheckUtils.throwIfNotEqual(parentFile.getStorageId(), storage.getId(),
                "文件夹和父级文件夹存储引擎不一致");
        }
        // 创建文件夹
        FileDO dirFile = new FileDO();
        String originalName = req.getOriginalName();
        dirFile.setName(originalName);
        dirFile.setOriginalName(originalName);
        dirFile.setParentPath(parentPath);
        dirFile.setType(FileTypeEnum.DIR);
        dirFile.setStorageId(storage.getId());
        baseMapper.insert(dirFile);
        return dirFile.getId();
    }

    @Override
    public void beforeUpdate(FileReq req, Long id) {
        // 名称和上级目录都会参与物理路径拼接（见 FileDO#setParentPath），
        // 需与新建入口做同样的路径穿越校验，避免通过修改接口改写出存储根目录之外的路径
        StoragePathValidator.validate(req.getParentPath());
        StoragePathValidator.validateName(req.getOriginalName());
    }

    @Override
    public FileStatisticsResp statistics() {
        FileStatisticsResp resp = new FileStatisticsResp();
        List<FileStatisticsResp> statisticsList = baseMapper.statistics();
        if (CollUtil.isEmpty(statisticsList)) {
            return resp;
        }
        resp.setData(statisticsList);
        resp.setSize(statisticsList.stream().mapToLong(FileStatisticsResp::getSize).sum());
        resp.setNumber(statisticsList.stream().mapToLong(FileStatisticsResp::getNumber).sum());
        return resp;
    }

    @Override
    public FileResp check(String fileHash) {
        FileDO file = baseMapper.lambdaQuery().eq(FileDO::getSha256, fileHash).one();
        if (file != null) {
            return get(file.getId());
        }
        return null;
    }

    @Override
    public Long calcDirSize(Long id) {
        FileDO dirFile = super.getById(id);
        ValidationUtils.throwIfNotEqual(dirFile.getType(), FileTypeEnum.DIR,
            "ID 为 [{}] 的不是文件夹，不支持计算大小", id);
        // 查询当前文件夹下的所有子文件和子文件夹
        List<FileDO> children =
            baseMapper.lambdaQuery().eq(FileDO::getParentPath, dirFile.getPath()).list();
        if (CollUtil.isEmpty(children)) {
            return 0L;
        }
        // 累加子文件大小和递归计算子文件夹大小
        return children.stream().mapToLong(child -> {
            if (FileTypeEnum.DIR.equals(child.getType())) {
                // 递归计算子文件夹大小
                return calcDirSize(child.getId());
            } else {
                return child.getSize();
            }
        }).sum();
    }

    @Override
    public FileUploadProgressResp getUploadProgress(String uploadTaskId) {
        ValidationUtils.throwIfBlank(uploadTaskId, "上传任务 ID 不能为空");
        String key = FILE_UPLOAD_PROGRESS_PREFIX + uploadTaskId;
        Object value = RedisUtils.get(key);
        if (value == null) {
            FileUploadProgressResp resp = new FileUploadProgressResp();
            resp.setUploadTaskId(uploadTaskId);
            resp.setStatus(FileUploadProgressStatusEnum.NOT_FOUND);
            resp.setPercentage(0);
            resp.setBytesRead(0L);
            resp.setTotalBytes(0L);
            return resp;
        }
        FileUploadProgressResp resp =
            JSONUtil.toBean(value.toString(), FileUploadProgressResp.class);
        if (StrUtil.isBlank(resp.getUploadTaskId())) {
            resp.setUploadTaskId(uploadTaskId);
        }
        return resp;
    }

    @Override
    public Long countByStorageIds(List<Long> storageIds) {
        if (CollUtil.isEmpty(storageIds)) {
            return 0L;
        }
        return baseMapper.lambdaQuery().in(FileDO::getStorageId, storageIds).count();
    }

    @Override
    public String resolveUrl(String path) {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        Map<String, String> urlMap = this.resolveUrls(List.of(path));
        return urlMap.getOrDefault(path, path);
    }

    @Override
    public Map<String, String> resolveUrls(Collection<String> paths) {
        Map<String, String> urlMap = new HashMap<>();
        if (CollUtil.isEmpty(paths)) {
            return urlMap;
        }
        // 头像等业务字段允许两种形态：http(s) 完整链接（第三方或升级前遗留）原样透传，本系统
        // 库内路径按文件记录所属存储装配。默认存储可被切换且不会迁移旧文件，若统一按当前默认
        // 存储装配，切换到其他存储后上传到原存储的历史头像将失效。仅在确实存在库内路径时才
        // 加载存储配置与文件记录，全外链场景不产生任何查询。
        Map<String, String> dbPathByInput = new HashMap<>();
        for (String path : paths) {
            if (StrUtil.isBlank(path)) {
                continue;
            }
            if (this.isStoredPath(path)) {
                dbPathByInput.put(path, StoragePathUtils.toDbPath(path));
            } else {
                // 完整链接等非本系统存储值原样返回
                urlMap.put(path, path);
            }
        }
        if (dbPathByInput.isEmpty()) {
            return urlMap;
        }
        // 批量加载存储配置（单存储系统不存在归属歧义，无需再查文件记录）
        List<StorageDO> storageList = storageService.list();
        Map<Long, StorageDO> storageGroup = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, Function.identity(),
                (existing, replacement) -> existing));
        Long defaultStorageId = storageList.stream()
            .filter(storage -> Boolean.TRUE.equals(storage.getIsDefault()))
            .map(StorageDO::getId)
            .findFirst()
            .orElse(null);
        Map<String, Long> storageIdByPath = this.resolveStorageIdByPath(dbPathByInput.values(),
            storageGroup.size(), defaultStorageId);
        for (Map.Entry<String, String> entry : dbPathByInput.entrySet()) {
            String dbPath = entry.getValue();
            String prefix = this.resolvePrefix(dbPath, storageIdByPath, storageGroup,
                defaultStorageId);
            urlMap.put(entry.getKey(), StrUtil.isBlank(prefix) ? entry.getKey()
                : URLUtil.normalize(prefix + dbPath, false, true));
        }
        return urlMap;
    }

    /**
     * 批量查询库内路径对应的文件记录所属存储
     *
     * <p>同一路径可能存在于多个存储（文件名唯一性按存储隔离），优先归属默认存储（上传
     * 入口即默认存储）；无默认存储命中时取首个匹配记录。单存储系统不存在归属歧义，
     * 跳过查询，由调用方回退默认存储。</p>
     *
     * @param dbPaths          库内路径集合
     * @param storageCount     存储配置数量
     * @param defaultStorageId 默认存储 ID
     * @return 库内路径 → 存储 ID
     */
    private Map<String, Long> resolveStorageIdByPath(Collection<String> dbPaths, int storageCount,
        Long defaultStorageId) {
        if (storageCount <= 1 || CollUtil.isEmpty(dbPaths)) {
            return Map.of();
        }
        Map<String, Long> storageIdByPath = new HashMap<>();
        List<String> distinctPaths = dbPaths.stream().distinct().toList();
        for (int i = 0; i < distinctPaths.size(); i += STORAGE_PATH_QUERY_BATCH_SIZE) {
            List<String> batch = distinctPaths.subList(i,
                Math.min(i + STORAGE_PATH_QUERY_BATCH_SIZE, distinctPaths.size()));
            baseMapper.lambdaQuery()
                .in(FileDO::getPath, batch)
                .list()
                .forEach(record -> {
                    Long storageId = record.getStorageId();
                    Long existing = storageIdByPath.get(record.getPath());
                    // 默认存储优先，避免同路径跨存储时装配到非上传存储
                    if (existing == null
                        || (defaultStorageId != null && defaultStorageId.equals(storageId))) {
                        storageIdByPath.put(record.getPath(), storageId);
                    }
                });
        }
        return storageIdByPath;
    }

    /**
     * 解析库内路径的访问 URL 前缀
     *
     * <p>优先使用文件记录所属存储的前缀；无文件记录（如记录已删除）或所属存储未配置
     * 前缀时，回退默认存储前缀。</p>
     *
     * @param dbPath           库内路径
     * @param storageIdByPath  库内路径 → 存储 ID
     * @param storageGroup     存储配置（ID → 配置）
     * @param defaultStorageId 默认存储 ID
     * @return URL 前缀；无可用前缀时返回 null
     */
    private String resolvePrefix(String dbPath, Map<String, Long> storageIdByPath,
        Map<Long, StorageDO> storageGroup, Long defaultStorageId) {
        Long storageId = storageIdByPath.get(dbPath);
        if (storageId != null) {
            StorageDO storage = storageGroup.get(storageId);
            if (storage != null && StrUtil.isNotBlank(storage.getUrlPrefix())) {
                return storage.getUrlPrefix();
            }
        }
        StorageDO defaultStorage = defaultStorageId == null ? null : storageGroup
            .get(defaultStorageId);
        return defaultStorage == null ? null : defaultStorage.getUrlPrefix();
    }

    /**
     * 是否为本系统库内路径
     *
     * <p>库内路径形如 {@code /user/avatar/x.png}（无前导斜杠的相对写法同样兼容），按文件
     * 记录所属存储装配访问 URL；http(s) 完整链接与 data: 等外部地址不属于本系统存储管辖
     * 范围。</p>
     *
     * @param path 路径
     * @return 是否为本系统库内路径
     */
    private boolean isStoredPath(String path) {
        return !this.isHttpPath(path) && !this.isExternalPath(path);
    }

    /**
     * 是否为 HTTP(S) 地址
     *
     * @param path 路径或 URL
     * @return 是否为 HTTP(S) 地址
     */
    private boolean isHttpPath(String path) {
        return StrUtil.startWithAnyIgnoreCase(path, "http://", "https://");
    }

    /**
     * 是否为非 HTTP(S) 的外部地址
     *
     * @param path 路径或 URL
     * @return 是否为外部地址
     */
    private boolean isExternalPath(String path) {
        return path.startsWith("//") || path.contains("://") || StrUtil.startWithAny(path
            .toLowerCase(Locale.ROOT), "data:", "file:", "ftp:", "blob:");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByPath(String path) {
        if (StrUtil.isBlank(path)) {
            return false;
        }
        // 头像等业务字段允许两种形态：库内相对路径直接按路径匹配文件记录；http(s) 完整链接
        // 需先按存储 URL 前缀识别是否为本系统历史 URL（升级前上传入口保存的是
        // fileInfo.getUrl()），识别为内部 URL 时同样按文件记录删除，仅确认属于第三方时才跳过
        FileDO file = this.findDeletableFile(path);
        if (file == null) {
            log.warn("路径 [{}] 未匹配到可删除的本系统文件记录，跳过文件删除", path);
            return false;
        }
        this.delete(List.of(file.getId()));
        return true;
    }

    /**
     * 查询待删除的文件记录
     *
     * @param path 库内路径或完整 URL
     * @return 文件记录；无匹配或匹配多条时返回 null
     */
    private FileDO findDeletableFile(String path) {
        if (!this.isStoredPath(path)) {
            // data: 等非 HTTP(S) 外部地址不属于本系统存储；http(s) 完整链接按存储前缀反查
            return this.isHttpPath(path) ? this.findFileByInternalUrl(path) : null;
        }
        return this.findUniqueFileByPath(StoragePathUtils.toDbPath(path));
    }

    /**
     * 按完整 URL 反查本系统文件记录
     *
     * <p>升级前上传入口保存的是存储 urlPrefix 拼接的完整 URL，按各存储的 URL 前缀反解
     * 库内路径后查询记录；无法匹配任何存储前缀时视为第三方链接，不做删除处理。</p>
     *
     * @param url 完整 URL
     * @return 文件记录；非本系统 URL 或匹配多条时返回 null
     */
    private FileDO findFileByInternalUrl(String url) {
        String normalizedUrl = URLUtil.normalize(url, false, true);
        StorageDO matchedStorage = null;
        String matchedPrefix = null;
        for (StorageDO storage : storageService.list()) {
            String prefix = storage.getUrlPrefix();
            if (StrUtil.isBlank(prefix) || !normalizedUrl.startsWith(prefix)) {
                continue;
            }
            // 多个存储前缀均可匹配时取最长前缀（最精确）
            if (matchedPrefix == null || prefix.length() > matchedPrefix.length()) {
                matchedStorage = storage;
                matchedPrefix = prefix;
            }
        }
        if (matchedStorage == null) {
            return null;
        }
        String dbPath = StoragePathUtils.toDbPath(normalizedUrl.substring(matchedPrefix.length()));
        List<FileDO> files = baseMapper.lambdaQuery()
            .eq(FileDO::getPath, dbPath)
            .eq(FileDO::getStorageId, matchedStorage.getId())
            .list();
        if (files.size() > 1) {
            log.warn("URL [{}] 在存储 [{}] 下对应多条文件记录，跳过删除以避免误删", url,
                matchedStorage.getCode());
            return null;
        }
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * 按库内路径查询唯一文件记录
     *
     * @param dbPath 库内路径
     * @return 文件记录；无匹配或匹配多条时返回 null
     */
    private FileDO findUniqueFileByPath(String dbPath) {
        List<FileDO> files = baseMapper.lambdaQuery().eq(FileDO::getPath, dbPath).list();
        if (files.size() > 1) {
            log.warn("路径为 [{}] 的文件记录存在多条，跳过删除以避免误删", dbPath);
            return null;
        }
        return files.isEmpty() ? null : files.get(0);
    }

    @Override
    protected void fill(Object obj) {
        super.fill(obj);
        if (obj instanceof FileResp fileResp) {
            StorageDO storage = storageService.getById(fileResp.getStorageId());
            String prefix = storage.getUrlPrefix();
            String url = URLUtil.normalize(prefix + fileResp.getPath(), false, true);
            fileResp.setUrl(url);
            String thumbnailUrl = StrUtils.blankToDefault(fileResp.getThumbnailName(), url,
                thName -> URLUtil.normalize(prefix + StoragePathUtils.normalize(StoragePathUtils
                    .thumbnailPath(fileResp.getParentPath(), thName)), false, true));
            fileResp.setThumbnailUrl(thumbnailUrl);
            fileResp.setStorageName("%s (%s)".formatted(storage.getName(), storage.getCode()));
        }
    }

    /**
     * 上传文件并返回上传后的文件信息
     *
     * @param file         文件
     * @param parentPath   上级目录
     * @param storageCode  存储引擎编码
     * @param extName      文件扩展名
     * @param uploadTaskId 上传任务 ID
     * @return 文件信息
     */
    private FileInfo doUpload(Object file, String parentPath, String storageCode, String extName,
        String uploadTaskId) {
        // 校验上级目录路径与文件名，防止通过 ../ 等路径穿越将文件写入存储根目录之外
        StoragePathValidator.validate(parentPath);
        String originalFileName = getOriginalFileName(file);
        StoragePathValidator.validateName(originalFileName);
        // 按扩展名归类一次（内部大小写不敏感），白名单与缩略图判定共用
        FileTypeEnum fileType = FileTypeEnum.getByExtension(extName);
        List<String> allExtensions = FileTypeEnum.getAllExtensions();
        CheckUtils.throwIf(fileType == FileTypeEnum.UNKNOWN, "不支持的文件类型，仅支持 {} 格式的文件", String
            .join(StringConstants.COMMA, allExtensions));
        // 构建上传预处理对象
        StorageDO storage = storageService.getByCode(storageCode);
        CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(storage.getStatus()), "请先启用存储 [{}]",
            storage.getCode());

        // 创建父级目录
        this.createParentDir(parentPath, storage);

        // 生成唯一文件名（处理重名情况）
        String uniqueFileName =
            FileNameGenerator.generateUniqueName(originalFileName, parentPath, storage
                .getId(), baseMapper);
        String sha256 = calculateSha256(file);
        String normalizedUploadTaskId = StrUtil.emptyToNull(StrUtil.trim(uploadTaskId));
        long totalBytes = this.getFileSize(file);
        if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
            this.saveUploadProgress(normalizedUploadTaskId, FileUploadProgressStatusEnum.INIT, 0L,
                totalBytes, 0, null);
        }

        UploadPretreatment uploadPretreatment = fileStorageService.of(normalizeUploadSource(file))
            .platform(storage.getCode())
            .path(this.pretreatmentPath(parentPath))
            .fileName(uniqueFileName)
            .metadata("sha256", sha256)
            .metadata("storageCode", storage.getCode())
            .metadata("storageId", String.valueOf(storage.getId()));
        // 图片文件生成缩略图
        if (FileTypeEnum.IMAGE == fileType) {
            uploadPretreatment.thumbnail(100, 100);
        }
        uploadPretreatment.onProgress((progressSize, allSize, percentage) -> {
            log.info("文件 [{}] 已上传 [{}]，总大小 [{}]，进度 [{}%]", uniqueFileName, progressSize, allSize,
                percentage);
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                FileUploadProgressStatusEnum status =
                    (allSize > 0 && progressSize >= allSize) || percentage >= 100
                        ? FileUploadProgressStatusEnum.FINALIZING
                        : FileUploadProgressStatusEnum.UPLOADING;
                this.saveUploadProgress(normalizedUploadTaskId, status, progressSize, allSize,
                    percentage, null);
            }
        });
        try {
            // 上传
            log.info("开始上传文件: {}", uniqueFileName);
            FileInfo fileInfo = uploadPretreatment.upload();
            log.info("文件 [{}] 上传完成", uniqueFileName);
            FileInfo result = this.postProcessUploadResult(fileInfo, storage);
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                long completedSize = result.getSize() == null ? totalBytes : result.getSize();
                this.saveUploadProgress(normalizedUploadTaskId,
                    FileUploadProgressStatusEnum.COMPLETED, completedSize, totalBytes, 100,
                    r -> {
                        r.setFileId(result.getFileId());
                        r.setUrl(result.getUrl());
                    });
            }
            return result;
        } catch (RuntimeException e) {
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                this.saveUploadProgress(normalizedUploadTaskId, FileUploadProgressStatusEnum.FAILED,
                    0L, totalBytes, 0, r -> r.setMessage(e.getMessage()));
            }
            throw e;
        }
    }

    /**
     * 获取原始文件名
     *
     * @param file 文件对象（MultipartFile 或 File）
     * @return 原始文件名
     */
    private String getOriginalFileName(Object file) {
        if (file instanceof MultipartFile multipartFile) {
            return multipartFile.getOriginalFilename();
        } else if (file instanceof File ioFile) {
            return ioFile.getName();
        }
        return "unknown";
    }

    /**
     * 规范化上传源对象。
     *
     * <p>
     * 如果传入的是 {@link File}，则先读取文件内容并包装为 {@link EnhancedMultipartFile}，
     * 避免 starter-storage 将其按普通对象走 JSON 分支处理；其他类型保持原样返回。
     * </p>
     *
     * @param file 上传源对象
     * @return 可交给 starter-storage 处理的上传对象
     */
    private static Object normalizeUploadSource(Object file) {
        if (!(file instanceof File ioFile)) {
            return file;
        }
        try {
            String contentType = Files.probeContentType(ioFile.toPath());
            if (StrUtil.isBlank(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return EnhancedMultipartFile.create(ioFile.getName(), ioFile.getName(), contentType,
                Files
                    .readAllBytes(ioFile.toPath()));
        } catch (IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        }
    }

    /**
     * 处理路径
     *
     * <p>
     * 1.如果 path 为 {@code /}，则保持为 {@code /}（避免触发自动日期目录） <br />
     * 2.如果 path 不以 {@code /} 结尾，则添加后缀 {@code /} <br />
     * 3.如果 path 以 {@code /} 开头，则移除前缀 {@code /} <br />
     * 示例：yyyy/MM/dd/
     * </p>
     *
     * @param path 路径
     * @return 处理路径
     */
    private String pretreatmentPath(String path) {
        if (StringConstants.SLASH.equals(path)) {
            // 传递根路径本身，避免 starter 将空路径识别为“自动日期目录”
            return StringConstants.SLASH;
        }
        return StrUtil.appendIfMissing(StrUtil.removePrefix(path, StringConstants.SLASH),
            StringConstants.SLASH);
    }

    /**
     * 创建上级文件夹（支持多级）
     *
     * <p>
     * user/avatar/ => user（path：/user）、avatar（path：/user/avatar）
     * </p>
     *
     * @param parentPath 上级目录
     * @param storage    存储配置
     */
    @Override
    public void createParentDir(String parentPath, StorageDO storage) {
        String lockKey = StrUtil.format("Lock:{}:{}", storage.getCode(), parentPath);
        try (RedisLockUtils lock = RedisLockUtils.tryLock(lockKey)) {
            if (!lock.isLocked()) {
                return; // 获取锁失败，直接返回
            }
            if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
                return;
            }
            // user/avatar/ => user、avatar
            String[] parentPathParts = StrUtil.split(parentPath, StringConstants.SLASH, false, true)
                .toArray(String[]::new);
            String lastPath = StringConstants.SLASH;
            StringBuilder currentPathBuilder = new StringBuilder();
            for (int i = 0; i < parentPathParts.length; i++) {
                String parentPathPart = parentPathParts[i];
                if (i > 0) {
                    lastPath = currentPathBuilder.toString();
                }
                // /user、/user/avatar
                currentPathBuilder.append(StringConstants.SLASH).append(parentPathPart);
                String currentPath = currentPathBuilder.toString();
                // 文件夹和文件存储引擎需要一致
                FileDO dirFile = baseMapper.lambdaQuery()
                    .eq(FileDO::getPath, currentPath)
                    .eq(FileDO::getType, FileTypeEnum.DIR)
                    .one();
                if (dirFile != null) {
                    CheckUtils.throwIfNotEqual(dirFile.getStorageId(), storage.getId(),
                        "文件夹和上传文件存储引擎不一致");
                    continue;
                }
                FileDO file = new FileDO();
                file.setName(parentPathPart);
                file.setOriginalName(parentPathPart);
                file.setPath(currentPath);
                file.setParentPath(lastPath);
                file.setType(FileTypeEnum.DIR);
                file.setStorageId(storage.getId());
                baseMapper.insert(file);
            }
        }
    }

    /**
     * 预查本批文件缩略图是否仍被其他有效记录引用
     *
     * <p>本批记录在调用前已逻辑删除，因此查询结果只包含仍然有效的记录。除主文件路径外，
     * 还需要处理“其他记录的缩略图路径恰好等于本批缩略图路径”的重名碰撞场景。</p>
     *
     * @param storageId 存储 ID
     * @param files     本批文件
     * @return 被其他有效记录引用的缩略图库内路径
     */
    private Set<String> findReferencedThumbnailPaths(Long storageId, Collection<FileDO> files) {
        Set<String> thumbnailPaths = files.stream()
            .map(FileDO::getThumbnailPath)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());
        if (CollUtil.isEmpty(thumbnailPaths)) {
            return Set.of();
        }
        Set<String> referencedPaths = new HashSet<>();
        // 其他记录的主文件路径命中
        baseMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storageId)
            .in(FileDO::getPath, thumbnailPaths)
            .list()
            .forEach(file -> referencedPaths.add(file.getPath()));
        // 其他记录的缩略图路径命中（按名称缩小后在内存中校验精确路径）
        Set<String> thumbnailNames = thumbnailPaths.stream()
            .map(FileNameUtil::getName)
            .collect(Collectors.toSet());
        baseMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storageId)
            .in(FileDO::getThumbnailName, thumbnailNames)
            .list()
            .forEach(file -> {
                String thumbnailPath = file.getThumbnailPath();
                if (thumbnailPath != null && thumbnailPaths.contains(thumbnailPath)) {
                    referencedPaths.add(thumbnailPath);
                }
            });
        return referencedPaths;
    }

    /**
     * 删除实际文件
     *
     * @param file                     文件
     * @param storage                  存储配置
     * @param referencedThumbnailPaths 被其他有效记录引用的缩略图库内路径
     * @param batchMainPaths           本批记录的主文件路径
     * @param handledPaths             本批已处理的物理路径
     */
    private void deleteFile(FileDO file, StorageDO storage,
        Set<String> referencedThumbnailPaths, Set<String> batchMainPaths,
        Set<String> handledPaths) {
        Long storageId = storage.getId();
        if (FileTypeEnum.DIR.equals(file.getType())) {
            // 不允许删除非空文件夹
            boolean exists = baseMapper.lambdaQuery()
                .eq(FileDO::getParentPath, file.getPath())
                .eq(FileDO::getStorageId, storageId)
                .exists();
            CheckUtils.throwIf(exists, "文件夹 [{}] 不为空，请先删除文件夹下的内容", file.getName());
            return;
        }
        String dbPath = file.getPath();
        String sourcePath = normalizeStoragePath(dbPath);
        boolean mainPathHandled = handledPaths.add(dbPath);
        if (!mainPathHandled) {
            log.warn("主文件路径 [{}] 在本批删除中重复，跳过重复的物理操作", dbPath);
        } else if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
            // 移动到回收站目录
            String targetPath = normalizeStoragePath(storage.getRecycleBinPath() + sourcePath);
            fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
                .getBucketName(), sourcePath, targetPath);
        } else {
            // 删除文件
            fileStorageService.delete(storage.getCode(), storage.getBucketName(), sourcePath);
        }
        // 缩略图随主文件一并处理；若缩略图路径就是本批某条记录的主路径，交给主文件操作，
        // 避免先被当作缩略图移走/删除后主文件操作失败
        String thumbnailDbPath = file.getThumbnailPath();
        if (thumbnailDbPath == null) {
            return;
        }
        if (batchMainPaths.contains(thumbnailDbPath)) {
            log.warn("缩略图路径 [{}] 是本批其他文件的主路径，交由主文件处理", thumbnailDbPath);
            return;
        }
        if (!handledPaths.add(thumbnailDbPath)) {
            log.warn("缩略图路径 [{}] 在本批删除中重复，跳过重复的物理操作", thumbnailDbPath);
            return;
        }
        if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
            this.moveThumbnailToRecycleBin(file, storage, referencedThumbnailPaths);
        } else {
            this.deleteThumbnail(file, storage, referencedThumbnailPaths);
        }
    }

    /**
     * 将缩略图移入回收站目录
     *
     * <p>缩略图为附属文件，探测与移动的失败均仅记录告警，不阻断主文件的回收站流程。缩略图
     * 路径仍被其他有效记录引用时跳过，避免移动其他文件的数据。</p>
     *
     * @param file                     文件
     * @param storage                  存储配置
     * @param referencedThumbnailPaths 被其他有效记录引用的缩略图库内路径
     */
    private void moveThumbnailToRecycleBin(FileDO file, StorageDO storage,
        Set<String> referencedThumbnailPaths) {
        String thumbnailDbPath = file.getThumbnailPath();
        if (thumbnailDbPath == null) {
            return;
        }
        if (referencedThumbnailPaths.contains(thumbnailDbPath)) {
            log.warn("缩略图 [{}] 仍被其他文件引用，跳过移入回收站", thumbnailDbPath);
            return;
        }
        String thumbnailPath = normalizeStoragePath(thumbnailDbPath);
        String targetPath = normalizeStoragePath(storage.getRecycleBinPath() + thumbnailPath);
        try {
            if (fileStorageService
                .exists(storage.getCode(), storage.getBucketName(), thumbnailPath)) {
                fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
                    .getBucketName(), thumbnailPath, targetPath);
            }
        } catch (Exception e) {
            log.warn("缩略图 [{}] 移入回收站失败", thumbnailPath, e);
        }
    }

    /**
     * 删除原目录下的缩略图
     *
     * <p>缩略图为附属文件，删除失败仅记录告警，不阻断主文件删除。缩略图路径仍被其他有效
     * 记录引用时跳过，避免删除其他文件的数据。</p>
     *
     * @param file                     文件
     * @param storage                  存储配置
     * @param referencedThumbnailPaths 被其他有效记录引用的缩略图库内路径
     */
    private void deleteThumbnail(FileDO file, StorageDO storage,
        Set<String> referencedThumbnailPaths) {
        String thumbnailDbPath = file.getThumbnailPath();
        if (thumbnailDbPath == null) {
            return;
        }
        if (referencedThumbnailPaths.contains(thumbnailDbPath)) {
            log.warn("缩略图 [{}] 仍被其他文件引用，跳过删除", thumbnailDbPath);
            return;
        }
        String thumbnailPath = normalizeStoragePath(thumbnailDbPath);
        try {
            fileStorageService.delete(storage.getCode(), storage.getBucketName(), thumbnailPath);
        } catch (Exception e) {
            log.warn("缩略图 [{}] 删除失败", thumbnailPath, e);
        }
    }

    private String calculateSha256(Object file) {
        try {
            if (file instanceof MultipartFile multipartFile) {
                return DigestUtil.sha256Hex(multipartFile.getInputStream());
            } else if (file instanceof File ioFile) {
                return DigestUtil.sha256Hex(ioFile);
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("计算文件 SHA256 失败", e);
        }
    }

    private FileInfo postProcessUploadResult(FileInfo fileInfo, StorageDO storage) {
        if (fileInfo.getMetadata() == null) {
            fileInfo.setMetadata(new HashMap<>());
        }
        fileInfo.setUrl(URLUtil.normalize(storage.getUrlPrefix() + normalizeStoragePath(fileInfo
            .getPath()), false, true));
        if (StrUtil.isNotBlank(fileInfo.getThumbnailPath()) && !StrUtil.startWithAny(fileInfo
            .getThumbnailPath(), "http://", "https://")) {
            fileInfo.setThumbnailPath(
                URLUtil.normalize(storage.getUrlPrefix() + normalizeStoragePath(fileInfo
                    .getThumbnailPath()), false, true));
        }
        return fileInfo;
    }

    private String normalizeStoragePath(String path) {
        return StoragePathUtils.normalize(path);
    }

    private long getFileSize(Object file) {
        if (file instanceof MultipartFile multipartFile) {
            return multipartFile.getSize();
        }
        if (file instanceof File ioFile) {
            return ioFile.length();
        }
        return 0L;
    }

    /**
     * 保存文件上传进度到 Redis
     *
     * <p>
     * 进度过程中 message/fileId/url 沿用上一次的值；终态（成功/失败）通过 customizer 写入结果信息。
     * </p>
     *
     * @param uploadTaskId 上传任务 ID
     * @param status       进度状态
     * @param bytesRead    已上传字节数
     * @param totalBytes   总字节数
     * @param percentage   进度百分比
     * @param customizer   终态结果定制器（可为 {@code null}），用于写入文件 ID、URL 或失败原因
     */
    private void saveUploadProgress(String uploadTaskId,
        FileUploadProgressStatusEnum status,
        long bytesRead,
        long totalBytes,
        int percentage,
        Consumer<FileUploadProgressResp> customizer) {
        String key = FILE_UPLOAD_PROGRESS_PREFIX + uploadTaskId;
        FileUploadProgressResp current = null;
        Object value = RedisUtils.get(key);
        if (value != null) {
            current = JSONUtil.toBean(value.toString(), FileUploadProgressResp.class);
        }
        if (current != null && isFinalStatus(current.getStatus()) && !isFinalStatus(status)) {
            return;
        }
        if (!isFinalStatus(status) && current != null) {
            bytesRead =
                Math.max(bytesRead, current.getBytesRead() == null ? 0L : current.getBytesRead());
            percentage =
                Math.max(percentage, current.getPercentage() == null ? 0 : current.getPercentage());
        }

        FileUploadProgressResp resp = new FileUploadProgressResp();
        resp.setUploadTaskId(uploadTaskId);
        resp.setStatus(status);
        resp.setBytesRead(bytesRead);
        resp.setTotalBytes(totalBytes);
        resp.setPercentage(percentage);
        // 进度过程沿用上一次的结果信息，终态由 customizer 覆盖写入
        resp.setMessage(current == null ? null : current.getMessage());
        resp.setFileId(current == null ? null : current.getFileId());
        resp.setUrl(current == null ? null : current.getUrl());
        if (customizer != null) {
            customizer.accept(resp);
        }
        RedisUtils.set(key, JSONUtil.toJsonStr(resp), FILE_UPLOAD_PROGRESS_EXPIRE);
    }

    private boolean isFinalStatus(String status) {
        return StrUtil.equalsAnyIgnoreCase(status, FileUploadProgressStatusEnum.COMPLETED
            .getValue(), FileUploadProgressStatusEnum.FAILED.getValue());
    }

    private boolean isFinalStatus(FileUploadProgressStatusEnum status) {
        return status == FileUploadProgressStatusEnum.COMPLETED
            || status == FileUploadProgressStatusEnum.FAILED;
    }
}

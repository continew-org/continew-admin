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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.storage.core.FileStorageService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件业务实现测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class FileServiceImplTest {

    @Test
    void shouldNotMoveBatchMainFileAsThumbnail() throws Exception {
        FileStorageService storageService = mock(FileStorageService.class);
        FileServiceImpl fileService = new FileServiceImpl(storageService);
        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        storage.setBucketName("bucket");
        storage.setRecycleBinEnabled(true);
        storage.setRecycleBinPath("/recycle");

        // A 的缩略图路径恰好等于 B 的主文件路径
        FileDO first = file(1L, "/user/avatar/a.png", "/user/avatar", "b.png");
        FileDO second = file(2L, "/user/avatar/b.png", "/user/avatar", "b.thumb.png");
        assertEquals("/user/avatar/a.png", first.getPath());
        assertEquals("/user/avatar/b.png", second.getPath());
        Set<String> batchMainPaths = Set.of(first.getPath(), second.getPath());
        Set<String> handledPaths = new HashSet<>();
        Method deleteFile = FileServiceImpl.class.getDeclaredMethod("deleteFile", FileDO.class,
            StorageDO.class, Set.class, Set.class, Set.class);
        deleteFile.setAccessible(true);

        deleteFile.invoke(fileService, first, storage, Set.of(), batchMainPaths, handledPaths);
        deleteFile.invoke(fileService, second, storage, Set.of(), batchMainPaths, handledPaths);

        ArgumentCaptor<String> sourcePaths = ArgumentCaptor.forClass(String.class);
        verify(storageService, times(2)).move(anyString(), anyString(), anyString(), sourcePaths
            .capture(), anyString());
        assertEquals(List.of("user/avatar/a.png", "user/avatar/b.png"), sourcePaths.getAllValues());
    }

    @Test
    void shouldResolveMixedAvatarFormsWithDefaultStorage() throws Exception {
        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        storage.setIsDefault(true);
        storage.setDomain("http://localhost:8080/file");
        StorageService storageService = mock(StorageService.class);
        when(storageService.list()).thenReturn(List.of(storage));
        FileServiceImpl fileService = newFileService(storageService);

        Map<String, String> urlMap =
            fileService.resolveUrls(List.of("http://third.party/avatar.png",
                "/user/avatar/x.png", "data:image/png;base64,AAA"));

        // http(s) 完整链接与 data: 等外部地址原样返回，库内路径按默认存储前缀装配
        assertEquals("http://third.party/avatar.png", urlMap.get("http://third.party/avatar.png"));
        assertEquals("http://localhost:8080/file/user/avatar/x.png", urlMap
            .get("/user/avatar/x.png"));
        assertEquals("data:image/png;base64,AAA", urlMap.get("data:image/png;base64,AAA"));
        verify(storageService, times(1)).list();
    }

    @Test
    void shouldNotLoadStorageConfigWhenAllAvatarsAreExternal() throws Exception {
        StorageService storageService = mock(StorageService.class);
        FileServiceImpl fileService = newFileService(storageService);

        Map<String, String> urlMap = fileService.resolveUrls(List.of("https://third.party/a.png",
            "http://third.party/b.png", "data:image/png;base64,AAA"));

        // 全外链场景不产生任何存储配置查询，空值不计入结果
        assertEquals(3, urlMap.size());
        verify(storageService, never()).list();
    }

    @Test
    void shouldReturnRawPathWhenNoDefaultStorageConfigured() throws Exception {
        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setIsDefault(false);
        StorageService storageService = mock(StorageService.class);
        when(storageService.list()).thenReturn(List.of(storage));
        FileServiceImpl fileService = newFileService(storageService);

        // 未配置默认存储时返回原路径，不报错
        assertEquals("/user/avatar/x.png", fileService.resolveUrl("/user/avatar/x.png"));
    }

    @Test
    void shouldSkipDeleteForExternalAvatarLink() throws Exception {
        FileServiceImpl fileService = newFileService(mock(StorageService.class));

        // 第三方完整链接不落入本系统存储，不做删除处理
        assertFalse(fileService.deleteByPath("http://third.party/avatar.png"));
        assertFalse(fileService.deleteByPath("https://third.party/avatar.png"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteStoredAvatarByExactPath() throws Exception {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileMapper baseMapper = mock(FileMapper.class);
        StorageService storageService = mock(StorageService.class);
        FileServiceImpl fileService = newFileService(fileStorageService, storageService);
        injectBaseMapper(fileService, baseMapper);

        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        storage.setBucketName("bucket");
        storage.setRecycleBinEnabled(false);

        FileDO avatar = new FileDO();
        avatar.setId(10L);
        // setParentPath 会按 name 重算 path，先设置 name
        avatar.setName("x.png");
        avatar.setParentPath("/user/avatar");
        avatar.setType(FileTypeEnum.IMAGE);
        avatar.setStorageId(1L);

        LambdaQueryChainWrapper<FileDO> wrapper = mock(LambdaQueryChainWrapper.class);
        when(baseMapper.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), any())).thenReturn(wrapper);
        when(wrapper.in(any(), anyList())).thenReturn(wrapper);
        when(wrapper.list()).thenReturn(List.of(avatar));
        when(storageService.listByIds(any())).thenReturn(List.of(storage));

        assertTrue(fileService.deleteByPath("/user/avatar/x.png"));

        // 库内路径精确命中记录，物理文件按不带前导斜杠的形态删除
        verify(fileStorageService).delete("local", "bucket", "user/avatar/x.png");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSkipDeleteWhenStoredPathNotFound() throws Exception {
        FileMapper baseMapper = mock(FileMapper.class);
        FileServiceImpl fileService = newFileService(mock(StorageService.class));
        injectBaseMapper(fileService, baseMapper);
        LambdaQueryChainWrapper<FileDO> wrapper = mock(LambdaQueryChainWrapper.class);
        when(baseMapper.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), any())).thenReturn(wrapper);
        when(wrapper.list()).thenReturn(List.of());

        // 记录不存在时跳过删除，避免误删
        assertFalse(fileService.deleteByPath("/user/avatar/missing.png"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSkipDeleteWhenStoredPathDuplicated() throws Exception {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileMapper baseMapper = mock(FileMapper.class);
        FileServiceImpl fileService =
            newFileService(fileStorageService, mock(StorageService.class));
        injectBaseMapper(fileService, baseMapper);
        LambdaQueryChainWrapper<FileDO> wrapper = mock(LambdaQueryChainWrapper.class);
        when(baseMapper.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), any())).thenReturn(wrapper);
        when(wrapper.list()).thenReturn(List.of(file(1L, "/user/avatar/x.png", "/user/avatar",
            null), file(2L, "/user/avatar/x.png", "/user/avatar", null)));

        // 同路径存在多条记录（如跨存储）时跳过删除，避免误删
        assertFalse(fileService.deleteByPath("/user/avatar/x.png"));
        verify(fileStorageService, never()).delete(any(), any(), any());
    }

    private FileServiceImpl newFileService(StorageService storageService) throws Exception {
        return this.newFileService(mock(FileStorageService.class), storageService);
    }

    private FileServiceImpl newFileService(FileStorageService fileStorageService,
        StorageService storageService) throws Exception {
        FileServiceImpl fileService = new FileServiceImpl(fileStorageService);
        Field field = FileServiceImpl.class.getDeclaredField("storageService");
        field.setAccessible(true);
        field.set(fileService, storageService);
        return fileService;
    }

    /**
     * 向基类注入 Mapper mock
     *
     * <p>baseMapper 声明在 Starter 的 ServiceImpl 中，需沿继承链向上查找。</p>
     *
     * @param fileService 文件服务
     * @param baseMapper  Mapper mock
     */
    private static void injectBaseMapper(FileServiceImpl fileService, FileMapper baseMapper)
        throws Exception {
        Class<?> type = fileService.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("baseMapper");
                field.setAccessible(true);
                field.set(fileService, baseMapper);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        throw new IllegalStateException("未找到 baseMapper 字段");
    }

    private FileDO file(Long id, String path, String parentPath, String thumbnailName) {
        FileDO file = new FileDO();
        file.setId(id);
        // setParentPath 会按 name 重算 path，先设置 parentPath 再显式覆盖 path
        file.setParentPath(parentPath);
        file.setPath(path);
        file.setThumbnailName(thumbnailName);
        file.setType(FileTypeEnum.IMAGE);
        return file;
    }
}
